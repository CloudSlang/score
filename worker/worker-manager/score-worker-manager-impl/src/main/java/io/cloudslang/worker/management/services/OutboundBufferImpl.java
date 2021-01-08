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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static ch.lambdaj.Lambda.extract;
import static ch.lambdaj.Lambda.on;
import static java.lang.Integer.getInteger;
import static java.lang.Long.getLong;
import static java.util.Collections.addAll;

public class OutboundBufferImpl implements OutboundBuffer, WorkerRecoveryListener {

    private static final Logger logger = LogManager.getLogger(OutboundBufferImpl.class);
    private static final long GB = 900000000; //there is JVM overhead, so i will take 10% buffer...

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

    @Autowired
    @Qualifier("numberOfExecutionThreads")
    private Integer numberOfThreads;

    private int currentWeight;
    private int bufferMapCapacity;
    private HashMap<String, ArrayList<Message>> buffer;

    private final int maxBufferWeight;
    private final int maxBulkWeight;
    private final int retryAmount;
    private final long retryDelay;
    private final int arrayAllocationCapacity;

    public OutboundBufferImpl() {
        this.currentWeight = 0;
        this.bufferMapCapacity = getMapCapacity(20);
        this.buffer = getInitialBuffer();

        this.maxBufferWeight = getInteger("out.buffer.max.buffer.weight", defaultBufferCapacity());
        this.maxBulkWeight = getInteger("out.buffer.max.bulk.weight", 1500);
        this.retryAmount = getInteger("out.buffer.retry.number", 5);
        this.retryDelay = getLong("out.buffer.retry.delay", 5000);
        this.arrayAllocationCapacity = getInteger("out.buffer.arrayAllocationCapacity", 100);

        logger.info("maxBufferWeight = " + maxBufferWeight);
    }

    @PostConstruct
    public void initialize() {
        // Compute the buffer map capacity based on the number of threads as default, such as to prevent rehashing
        this.bufferMapCapacity = getInteger("out.buffer.entries", getMapCapacity(numberOfThreads));
        this.buffer = getInitialBuffer();
    }

    private int getMapCapacity(int expectedSize) {
        if (expectedSize < 3) {
            return expectedSize + 1;
        } else {
            return expectedSize < 1073741824 ? expectedSize + expectedSize / 3 : 2147483647;
        }
    }

    private HashMap<String, ArrayList<Message>> getInitialBuffer() {
        return new HashMap<>(bufferMapCapacity);
    }

    @Override
    public void put(final Message... messages) throws InterruptedException {
        final Message messageToAdd = validateAndGetMessageToPut(messages);
        final int messageToAddWeight = messageToAdd.getWeight();
        final String executionId = messageToAdd.getId();
        try {
            syncManager.startPutMessages();
            // We need to check if the current thread was interrupted while waiting for the lock (ExecutionThread or InBufferThread in ackMessages)
            while (currentWeight >= maxBufferWeight) {
                logger.info("Outbound buffer is full. Waiting...");
                syncManager.waitForDrain();
                logger.info("Outbound buffer drained. Finished waiting.");
            }

            // Put message into the buffer, intentionally not using merge function because of extra if
            ArrayList<Message> oldValue = buffer.get(executionId);
            if (oldValue != null) {
                oldValue.add(messageToAdd);
            } else {
                buffer.put(executionId, getMutableListOf(messageToAdd));
            }
            currentWeight += messageToAddWeight;
        } catch (InterruptedException ex) {
            logger.warn("Buffer put action was interrupted", ex);
            throw ex;
        } finally {
            syncManager.finishPutMessages();
        }
    }

    private ArrayList<Message> getMutableListOf(Message messageToAdd) {
        ArrayList<Message> newValue = new ArrayList<>(arrayAllocationCapacity);
        newValue.add(messageToAdd);
        return newValue;
    }

    private Message validateAndGetMessageToPut(final Message[] messages) {
        Message retVal;
        if ((messages != null) && (messages.length == 1)) {
            // Single message
            retVal = messages[0];
        } else if ((messages != null) && (messages.length > 1)) {
            // At least 2 messages -> use one compound message such that it will be processed in one transaction
            retVal = new CompoundMessage(messages);
        } else {
            throw new IllegalArgumentException("messages is null or empty");
        }
        return retVal;
    }

    @Override
    public void drain() {
        HashMap<String, ArrayList<Message>> bufferToDrain;
        try {
            syncManager.startDrain();
            while (buffer.isEmpty()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("buffer is empty. Waiting to drain...");
                }
                syncManager.waitForMessages();
            }
            // Switch to the new buffer and drain old buffer on the same scheduled threadpool thread
            bufferToDrain = buffer;
            buffer = getInitialBuffer();
            currentWeight = 0;
        } catch (InterruptedException e) {
            logger.warn("Drain outgoing buffer was interrupted while waiting for messages on the buffer");
            return;
        } finally {
            syncManager.finishDrain();
        }

        drainInternal(bufferToDrain);
    }

    private void drainInternal(HashMap<String, ArrayList<Message>> bufferToDrain) {
        int bulkWeight = 0;
        List<Message> bulk = new LinkedList<>();
        try {
            for (ArrayList<Message> value : bufferToDrain.values()) {
                List<Message> convertedList = expandCompoundMessages(value);
                List<Message> optimizedList = convertedList.get(0).shrink(convertedList);
                int optimizedWeight = optimizedList.stream().mapToInt(Message::getWeight).sum();

                bulk.addAll(optimizedList);
                bulkWeight += optimizedWeight;

                if (bulkWeight > maxBulkWeight) {
                    drainBulk(bulk);
                    bulk.clear();
                    bulkWeight = 0;
                }
            }
            // Drain last bulk if required
            if (!bulk.isEmpty()) {
                drainBulk(bulk);
            }
        } catch (Exception ex) {
            logger.error("Failed to drain buffer, invoking worker internal recovery... ", ex);
            recoveryManager.doRecovery();
        }
    }

    private List<Message> expandCompoundMessages(ArrayList<Message> value) {
        int compoundMessages = 0, compoundMessageSize = 0;
        for (Message crt : value) {
            if (crt instanceof CompoundMessage) {
                compoundMessages++;
                compoundMessageSize += ((CompoundMessage) crt).getNumberOfMessages();
            }
        }
        if (compoundMessages == 0) {
            return value;
        } else {
            List<Message> convertedList = new ArrayList<>(value.size() + compoundMessageSize - compoundMessages);
            for (Message crt : value) {
                if (crt instanceof CompoundMessage) {
                    ((CompoundMessage) crt).drainTo(convertedList);
                } else {
                    convertedList.add(crt);
                }
            }
            return convertedList;
        }
    }

    private void drainBulk(final List<Message> bulkToDrain) {
        // Bulk number is the same for all retries! This is done to prevent duplications when we insert with retries
        final String bulkNumber = UUID.randomUUID().toString();

        retryTemplate.retry(retryAmount, retryDelay, new RetryTemplate.RetryCallback() {
            @Override
            public void tryOnce() {
                String wrv = recoveryManager.getWRV();
                if (logger.isDebugEnabled()) {
                    logger.debug("Dispatch start with bulk number: " + bulkNumber);
                }
                dispatcherService.dispatch(bulkToDrain, bulkNumber, wrv, workerUuid);
                if (executionsActivityListener != null) {
                    executionsActivityListener
                            .onHalt(extract(bulkToDrain, on(ExecutionMessage.class).getExecStateId()));
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("Dispatch end with bulk number: " + bulkNumber);
                }
            }
        });
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

        public int getNumberOfMessages() {
            return messages.length;
        }

        @Override
        public String getId() {
            return messages[0].getId();
        }

        @Override
        public List<Message> shrink(List<Message> messages) {
            return messages; // do nothing
        }
    }


    private int defaultBufferCapacity() {
        long maxMemory = Runtime.getRuntime().maxMemory();
        if (maxMemory < 0.5 * GB) {
            return 10_000;
        }
        if (maxMemory < GB) {
            return 15_000;
        }
        if (maxMemory < 2 * GB) {
            return 30_000;
        }
        return 60_000;
    }
}
