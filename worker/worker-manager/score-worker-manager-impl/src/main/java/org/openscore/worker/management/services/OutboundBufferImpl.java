/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package org.openscore.worker.management.services;

import ch.lambdaj.group.Group;
import io.cloudslang.orchestrator.entities.Message;
import io.cloudslang.orchestrator.services.OrchestratorDispatcherService;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static ch.lambdaj.Lambda.*;

public class OutboundBufferImpl implements OutboundBuffer, WorkerRecoveryListener {
	private final Logger logger = Logger.getLogger(this.getClass());

    private static long GB = 900000000;//there is JVM overhead, so i will take 10% buffer...

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

	private List<Message> buffer = new ArrayList<>();

	private int currentWeight;

	private int maxBufferWeight = Integer.getInteger("out.buffer.max.buffer.weight", 30000);
	private int maxBulkWeight = Integer.getInteger("out.buffer.max.bulk.weight", 1500);
	private int retryAmount = Integer.getInteger("out.buffer.retry.number", 5);
	private long retryDelay = Long.getLong("out.buffer.retry.delay", 5000);

    @PostConstruct
    public void init(){
        maxBufferWeight = Integer.getInteger("out.buffer.max.buffer.weight", defaultBufferCapacity());
        logger.info("maxBufferWeight = " + maxBufferWeight);
    }

	@Override
	public void put(final Message... messages) throws InterruptedException{
		Validate.notEmpty(messages, "The array of messages is null or empty");
        try{
            syncManager.startPutMessages();

            //We need to check if the current thread was interrupted while waiting for the lock (ExecutionThread or InBufferThread in ackMessages)
            if(Thread.currentThread().isInterrupted()){
                throw new InterruptedException("Thread was interrupted while waiting on the lock! Exiting...");
            }

            while (currentWeight >= maxBufferWeight){
                logger.warn("Outbound buffer is full. Waiting...");
                syncManager.waitForDrain();
            }

            // in case of multiple messages create a single compound message
            // to make sure that it will be processed in a single transaction
            Message message = messages.length==1? messages[0]: new CompoundMessage(messages);

            //put message into the buffer
            buffer.add(message);

            currentWeight += message.getWeight();
			if (logger.isTraceEnabled()) logger.trace(message.getClass().getSimpleName() + " added to the buffer. " + getStatus());
		} catch (InterruptedException ex) {
			logger.warn("Buffer put action was interrupted", ex);
            throw ex;
		} finally {
			syncManager.finishPutMessages();
		}
	}

	@Override
	public void drain() {
		List<Message> bufferToDrain;
		try{
            syncManager.startDrain();
			while (buffer.isEmpty()){
				if (logger.isDebugEnabled()){
                    logger.debug("buffer is empty. Waiting to drain...");
                }
				syncManager.waitForMessages();
			}

			if (logger.isDebugEnabled()) logger.debug("buffer is going to be drained. " + getStatus());

			bufferToDrain = buffer;
			buffer = new ArrayList<>();
			currentWeight = 0;
		} catch (InterruptedException e) {
			logger.warn("Drain outgoing buffer was interrupted while waiting for messages on the buffer");
			return;
		} finally{
			syncManager.finishDrain();
		}

		drainInternal(bufferToDrain);
	}

	private void drainInternal(List<Message> bufferToDrain){
		List<Message> bulk = new ArrayList<>();
		int bulkWeight = 0;
		Map<String,AtomicInteger> logMap = new HashMap<>();
		try {
			for (Message message : bufferToDrain) {
				if (message.getClass().equals(CompoundMessage.class)){
					bulk.addAll(((CompoundMessage)message).asList());
				} else {
					bulk.add(message);
				}
				bulkWeight += message.getWeight();

				if (logger.isDebugEnabled()){
					if (logMap.get(message.getClass().getSimpleName()) == null) logMap.put(message.getClass().getSimpleName(), new AtomicInteger(1));
					else logMap.get(message.getClass().getSimpleName()).incrementAndGet();
				}

				if (bulkWeight > maxBulkWeight){
					if (logger.isDebugEnabled()) logger.debug("trying to drain bulk: " + logMap.toString() + ", W:" + bulkWeight);
					drainBulk(bulk);
					bulk.clear();
					bulkWeight = 0;
					logMap.clear();
				}
			}
			// drain the last bulk
			if (logger.isDebugEnabled()) logger.debug("trying to drain bulk: " + logMap.toString() + ", " + getStatus());
			drainBulk(bulk);
		} catch (Exception ex) {
			logger.error("Failed to drain buffer, invoking worker internal recovery... ", ex);
			recoveryManager.doRecovery();
		}
	}

	private List<Message> optimize(List<Message> messages){
		long t = System.currentTimeMillis();
		List<Message> result = new ArrayList<>();

		Group<Message> groups = group(messages, by(on(Message.class).getId()));
		for (Group<Message> group :groups.subgroups()){
			result.addAll(group.first().shrink(group.findAll()));
		}

		if (logger.isDebugEnabled()) logger.debug("bulk optimization result: " + messages.size() + " -> " + result.size() + " in " + (System.currentTimeMillis()-t) + " ms");

		return result;
	}

	private void drainBulk(List<Message> bulkToDrain){
		long t = System.currentTimeMillis();
		final List<Message> optimizedBulk = optimize(bulkToDrain);
        //Bulk number is the same for all retries! This is done to prevent duplications when we insert with retries
        final String bulkNumber = UUID.randomUUID().toString();

		retryTemplate.retry(retryAmount, retryDelay, new RetryTemplate.RetryCallback() {
			@Override
			public void tryOnce() {
                String wrv = recoveryManager.getWRV();
                if (logger.isDebugEnabled()) logger.debug("Dispatch start with bulk number: " + bulkNumber);
				dispatcherService.dispatch(optimizedBulk, bulkNumber, wrv, workerUuid);
                if (logger.isDebugEnabled()) logger.debug("Dispatch end with bulk number: " + bulkNumber);
			}
		});
		if (logger.isDebugEnabled()) logger.debug("bulk was drained in " + (System.currentTimeMillis()-t) + " ms");
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
        if (logger.isDebugEnabled()){
            logger.debug("OutboundBuffer is in recovery, clearing buffer.");
        }
        buffer.clear();
        currentWeight = 0 ;
    }

    private class CompoundMessage implements Message{
        private Message[] messages;

        public CompoundMessage(Message[] messages){
            this.messages = messages.clone();
        }

        @Override
        public int getWeight() {
            int weight = 0;
            for (Message message : messages) weight += message.getWeight();
            return weight;
        }

        public List<Message> asList() {
            return Arrays.asList(messages);
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
        if(maxMemory  < 0.5*GB) return 10000;
        if(maxMemory  < 1*GB) return 15000;
        if(maxMemory  < 2*GB) return 30000;
        return 60000;
    }
}
