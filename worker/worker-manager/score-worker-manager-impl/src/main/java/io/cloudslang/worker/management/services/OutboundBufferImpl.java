/*
 * Copyright © 2014-2017 EntIT Software LLC, a Micro Focus company (L.P.)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.cloudslang.worker.management.services;

import io.cloudslang.engine.queue.entities.ExecutionMessage;
import io.cloudslang.orchestrator.entities.Message;
import io.cloudslang.orchestrator.services.OrchestratorDispatcherService;
import io.cloudslang.worker.management.ExecutionsActivityListener;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static ch.lambdaj.Lambda.extract;
import static ch.lambdaj.Lambda.on;
import static java.util.Collections.addAll;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

public class OutboundBufferImpl implements OutboundBuffer, WorkerRecoveryListener {

    private static final Logger logger = Logger.getLogger(OutboundBufferImpl.class);
    private static long GB = 900000000; //there is JVM overhead, so i will take 10% buffer...

    @Autowired
    private RetryTemplate retryTemplate;

    @Autowired
    private WorkerRecoveryManager recoveryManager;

    @Autowired
    private OrchestratorDispatcherService dispatcherService;

    @Resource
    private String workerUuid;

    @Autowired
    private SynchronizationManager syncManager;

    @Autowired(required = false)
    private ExecutionsActivityListener executionsActivityListener;

    private List<Message> buffer = new ArrayList<>();

    private int currentWeight;
    private int maxBufferWeight = Integer.getInteger("out.buffer.max.buffer.weight", 30000);
    private int maxBulkWeight = Integer.getInteger("out.buffer.max.bulk.weight", 1500);
    private int retryAmount = Integer.getInteger("out.buffer.retry.number", 5);
    private long retryDelay = Long.getLong("out.buffer.retry.delay", 5000);

    @PostConstruct
    public void init() {
        maxBufferWeight = Integer.getInteger("out.buffer.max.buffer.weight", defaultBufferCapacity());
        logger.info("maxBufferWeight = " + maxBufferWeight);
    }

    @Override
    public void put(final Message... messages) throws InterruptedException {
        final Message messageToAdd = validateAndGetMessageToPut(messages);
        final int messageToAddWeight = messageToAdd.getWeight();
        syncManager.startPutMessages();
        try {
            // We need to check if the current thread was interrupted while waiting for the lock (ExecutionThread or InBufferThread in ackMessages)
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException("Thread was interrupted while waiting on the lock! Exiting...");
            }
            while (currentWeight >= maxBufferWeight) {
                logger.info("Outbound buffer is full. Waiting...");
                syncManager.waitForDrain();
                logger.info("Outbound buffer drained. Finished waiting.");
            }
            // Put message into the buffer
            buffer.add(messageToAdd);
            currentWeight += messageToAddWeight;
        } catch (InterruptedException ex) {
            logger.warn("Buffer put action was interrupted", ex);
            throw ex;
        } finally {
            syncManager.finishPutMessages();
        }
    }

    private Message validateAndGetMessageToPut(Message[] messages) {
        Message retVal;
        if (ArrayUtils.isEmpty(messages)) { // No messages
            throw new IllegalArgumentException("messages is null or empty");
        } else if (messages.length == 1) { // Single message
            retVal = messages[0];
        } else { // At least 2 messages -> use one compound message such that it will be processed in one transaction
            retVal = new CompoundMessage(messages);
        }
        return retVal;
    }

    @Override
    public void drain() {
        List<Message> bufferToDrain;
        syncManager.startDrain();
        try {
            while (buffer.isEmpty()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("buffer is empty. Waiting to drain...");
                }
                syncManager.waitForMessages();
            }

            if (logger.isDebugEnabled()) {
                logger.debug("buffer is going to be drained. " + getStatus());
            }

            bufferToDrain = buffer;
            buffer = new ArrayList<>();
            currentWeight = 0;
        } catch (InterruptedException e) {
            logger.warn("Drain outgoing buffer was interrupted while waiting for messages on the buffer");
            return;
        } finally {
            syncManager.finishDrain();
        }

        drainInternal(bufferToDrain);
    }

    private void drainInternal(List<Message> bufferToDrain) {
        List<Message> bulk = new ArrayList<>();
        int bulkWeight = 0;
        try {
            for (Message message : bufferToDrain) {
                if (message.getClass().equals(CompoundMessage.class)) {
                    ((CompoundMessage) message).drainTo(bulk);
                } else {
                    bulk.add(message);
                }
                bulkWeight += message.getWeight();

                if (bulkWeight > maxBulkWeight) {
                    drainBulk(bulk);
                    bulk.clear();
                    bulkWeight = 0;
                }
            }
            // drain the last bulk
            drainBulk(bulk);
        } catch (Exception ex) {
            logger.error("Failed to drain buffer, invoking worker internal recovery... ", ex);
            recoveryManager.doRecovery();
        }
    }

    private List<Message> optimize(List<Message> messages) {
        long t = System.currentTimeMillis();

        Map<String, List<Message>> messagesByExecutionIdMap = messages.stream()
                .collect(groupingBy(Message::getId, LinkedHashMap::new, toList()));

        List<Message> result = new ArrayList<>();
        for (List<Message> value : messagesByExecutionIdMap.values()) {
            result.addAll(value.get(0).shrink(value));
        }

        if (logger.isDebugEnabled()) {
            logger.debug("bulk optimization result: " + messages.size() + " -> " + result.size() + " in " + (
                    System.currentTimeMillis() - t) + " ms");
        }
        return result;
    }

    private void drainBulk(List<Message> bulkToDrain) {
        long t = System.currentTimeMillis();
        final List<Message> optimizedBulk = optimize(bulkToDrain);
        //Bulk number is the same for all retries! This is done to prevent duplications when we insert with retries
        final String bulkNumber = UUID.randomUUID().toString();

        retryTemplate.retry(retryAmount, retryDelay, new RetryTemplate.RetryCallback() {
            @Override
            public void tryOnce() {
                String wrv = recoveryManager.getWRV();
                if (logger.isDebugEnabled()) {
                    logger.debug("Dispatch start with bulk number: " + bulkNumber);
                }
                dispatcherService.dispatch(optimizedBulk, bulkNumber, wrv, workerUuid);
                if (executionsActivityListener != null) {
                    executionsActivityListener
                            .onHalt(extract(optimizedBulk, on(ExecutionMessage.class).getExecStateId()));
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("Dispatch end with bulk number: " + bulkNumber);
                }
            }
        });
        if (logger.isDebugEnabled()) {
            logger.debug("bulk was drained in " + (System.currentTimeMillis() - t) + " ms");
        }
    }

    @Override
    public int getSize() {
        return buffer.size();
    }

    @Override
    public int getWeight() {
        return currentWeight;
    }

    @Override
    public int getCapacity() {
        return maxBufferWeight;
    }

    @Override
    public String getStatus() {
        return "Buffer status: [W:" + currentWeight + '/' + maxBufferWeight + ",S:" + buffer.size() + "]";
    }

    @Override
    public void doRecovery() {
        if (logger.isDebugEnabled()) {
            logger.debug("OutboundBuffer is in recovery, clearing buffer.");
        }
        buffer.clear();
        currentWeight = 0;
    }

    private class CompoundMessage implements Message {

        private Message[] messages;

        public CompoundMessage(Message[] messages) {
            this.messages = messages;
        }

        @Override
        public int getWeight() {
            int weight = 0;
            for (Message message : messages) {
                weight += message.getWeight();
            }
            return weight;
        }

        public void drainTo(List<Message> drainResult) {
            addAll(drainResult, messages);
        }

        @Override
        public String getId() {
            return null;
        }

        @Override
        public List<Message> shrink(List<Message> messages) {
            return messages; // do nothing
        }
    }


    private int defaultBufferCapacity() {
        Long maxMemory = Runtime.getRuntime().maxMemory();
        if (maxMemory < 0.5 * GB) {
            return 10000;
        }
        if (maxMemory < 1 * GB) {
            return 15000;
        }
        if (maxMemory < 2 * GB) {
            return 30000;
        }
        return 60000;
    }
}
