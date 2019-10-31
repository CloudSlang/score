/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/

package io.cloudslang.worker.management.services;

import io.cloudslang.engine.queue.entities.ExecStatus;
import io.cloudslang.engine.queue.entities.ExecutionMessage;
import io.cloudslang.engine.queue.services.QueueDispatcherService;
import io.cloudslang.worker.management.ExecutionsActivityListener;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;

import static ch.lambdaj.Lambda.extract;
import static ch.lambdaj.Lambda.on;

/**
 * Created with IntelliJ IDEA.
 * User: kravtsov
 * Date: 20/11/12
 * Time: 08:46
 */
public class InBuffer implements WorkerRecoveryListener, ApplicationListener, Runnable{
    private static final Logger logger = Logger.getLogger(InBuffer.class);

    private final static long THRESHOLD = Runtime.getRuntime().maxMemory() *12 / 100;
    private final static long ONE_GB = 1_000_000_000;
    private final static long MEMORY_THRESHOLD = Math.min(THRESHOLD, ONE_GB);
    private final static int MINIMUM_GC_DELTA = 10000; // minimum delta between garbage collections in milliseconds

    @Autowired
    private QueueDispatcherService queueDispatcher;

    @Resource
    private String workerUuid;

    @Autowired
    @Qualifier("inBufferCapacity")
    private Integer capacity;

    @Autowired(required = false)
    @Qualifier("coolDownPollingMillis")
    private Integer coolDownPollingMillis = 200;

    private Thread fillBufferThread = new Thread(this);

    private boolean inShutdown;

    private boolean endOfInit = false;

    private long gcTimer = System.currentTimeMillis();

    @Autowired
    private WorkerManager workerManager;

    @Autowired
    private SimpleExecutionRunnableFactory simpleExecutionRunnableFactory;

    @Autowired
    private OutboundBuffer outBuffer;

    @Autowired
    private SynchronizationManager syncManager;

    @Autowired(required = false)
    private ExecutionsActivityListener executionsActivityListener;

    @PostConstruct
    private void init(){
        capacity = Integer.getInteger("worker.inbuffer.capacity",capacity);
        coolDownPollingMillis = Integer.getInteger("worker.inbuffer.coolDownPollingMillis",coolDownPollingMillis);
        logger.info("InBuffer capacity is set to :" + capacity + ", coolDownPollingMillis is set to :"+ coolDownPollingMillis);
    }



    private void fillBufferPeriodically() {

        while (!inShutdown) {
            try {
                boolean workerUp = workerManager.isUp();
                if(!workerUp) {
                    Thread.sleep(3000); //sleep if worker is not fully started yet
                }
                else {
                    syncManager.startGetMessages(); //we must lock recovery lock before poll - otherwise we will get duplications

                    //We need to check if the current thread was interrupted while waiting for the lock (InBufferThread) and RESET its interrupted flag!
                    if(Thread.interrupted()){
                        logger.info("Thread was interrupted while waiting on the lock in fillBufferPeriodically()!");
                        continue;
                    }

                    if (needToPoll()) {
                        int messagesToGet = capacity - workerManager.getInBufferSize();

                        if (logger.isDebugEnabled()) logger.debug("Polling messages from queue (max " + messagesToGet + ")");
                        List<ExecutionMessage> newMessages = queueDispatcher.poll(workerUuid, messagesToGet);
                        if (executionsActivityListener != null) {
                            executionsActivityListener.onActivate(extract(newMessages, on(ExecutionMessage.class).getExecStateId()));
                        }
                        if (logger.isDebugEnabled()) logger.debug("Received " + newMessages.size() + " messages from queue");

                        if (!newMessages.isEmpty()) {
                            //we must acknowledge the messages that we took from the queue
                            ackMessages(newMessages);
                            for(ExecutionMessage msg :newMessages){
                                addExecutionMessageInner(msg);
                            }

                            syncManager.finishGetMessages(); //release all locks before going to sleep!!!

                            Thread.sleep(coolDownPollingMillis/8); //cool down - sleep a while
                        }
                        else {
                            syncManager.finishGetMessages(); //release all locks before going to sleep!!!

                            Thread.sleep(coolDownPollingMillis); //if there are no messages - sleep a while
                        }
                    }
                    else {
                        syncManager.finishGetMessages(); //release all locks before going to sleep!!!

                        Thread.sleep(coolDownPollingMillis); //if the buffer is not empty enough yet or in recovery - sleep a while
                    }
                }
            } catch (InterruptedException ex) {
                logger.error("Fill InBuffer thread was interrupted... ", ex);
                syncManager.finishGetMessages(); //release all locks before going to sleep!!!
                try {Thread.sleep(1000);} catch (InterruptedException e) {/*ignore*/}
            } catch (Exception ex) {
                logger.error("Failed to load new ExecutionMessages to the buffer!", ex);
                syncManager.finishGetMessages(); //release all locks before going to sleep!!!
                try {Thread.sleep(1000);} catch (InterruptedException e) {/*ignore*/}
            }
            finally {
                syncManager.finishGetMessages();
            }
        }
    }

    private boolean needToPoll(){
        int bufferSize = workerManager.getInBufferSize();

        if (logger.isDebugEnabled()) logger.debug("InBuffer size: " + bufferSize);

        return bufferSize < (capacity * 0.2) && checkFreeMemorySpace(MEMORY_THRESHOLD);
    }

    private void ackMessages(List<ExecutionMessage> newMessages) throws InterruptedException {
        ExecutionMessage cloned;
        for (ExecutionMessage message : newMessages) {
            // create a unique id for this lane in this specific worker to be used in out buffer optimization
            //logger.error("ACK FOR MESSAGE: " + message.getMsgId() + " : " + message.getExecStateId());
            message.setWorkerKey(message.getMsgId() + " : " + message.getExecStateId());
            cloned = (ExecutionMessage) message.clone();
            cloned.setStatus(ExecStatus.IN_PROGRESS);
            cloned.incMsgSeqId();
            message.incMsgSeqId(); // increment the original message seq too in order to preserve the order of all messages of entire step
            cloned.setPayload(null); //payload is not needed in ack - make it null in order to minimize the data that is being sent
            outBuffer.put(cloned);
        }
    }


    public void addExecutionMessage(ExecutionMessage msg) throws InterruptedException {
        try{
            syncManager.startGetMessages(); //this is a public method that can push new executions from outside - from execution threads
            //We need to check if the current execution thread was interrupted while waiting for the lock
            if(Thread.currentThread().isInterrupted()){
                throw new InterruptedException("Thread was interrupted while waiting on the lock in fillBufferPeriodically()!");
            }
            addExecutionMessageInner(msg);
        }
        finally {
            syncManager.finishGetMessages();
        }
    }

    private void addExecutionMessageInner(ExecutionMessage msg) {
        SimpleExecutionRunnable simpleExecutionRunnable = simpleExecutionRunnableFactory.getObject();
        simpleExecutionRunnable.setExecutionMessage(msg);
        Long executionId = null;
        if (!StringUtils.isEmpty(msg.getMsgId())) {
            executionId = Long.valueOf(msg.getMsgId());
        }
        workerManager.addExecution(executionId, simpleExecutionRunnable);
    }

    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        if (applicationEvent instanceof ContextRefreshedEvent && ! endOfInit) {
            endOfInit = true;
            inShutdown = false;
            fillBufferThread.setName("WorkerFillBufferThread");
            fillBufferThread.start();
        } else if (applicationEvent instanceof ContextClosedEvent) {
            inShutdown = true;
        }
    }

    @Override
    public void run() {
        fillBufferPeriodically();
    }

    public boolean checkFreeMemorySpace(long threshold){
        double allocatedMemory      = Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
        double presumableFreeMemory = Runtime.getRuntime().maxMemory() - allocatedMemory;
        boolean result = presumableFreeMemory > threshold;
        if (! result) {
            logger.warn("InBuffer would not poll messages, because there is not enough free memory.");
            if (System.currentTimeMillis() > (gcTimer + MINIMUM_GC_DELTA)){
                logger.warn("Trying to initiate garbage collection");
                System.gc();
                gcTimer = System.currentTimeMillis();
            }
        }
        return result;
    }

    @Override
    public void doRecovery() {
        //We must interrupt the inBuffer thread in case it is stuck in await() because the outBuffer is full
        fillBufferThread.interrupt();
    }

    public int getCapacity() {
        return capacity;
    }
}
