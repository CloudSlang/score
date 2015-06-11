/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/

package io.cloudslang.orchestrator.services;

import io.cloudslang.engine.node.services.WorkerNodeService;
import io.cloudslang.engine.queue.entities.ExecutionMessage;
import io.cloudslang.engine.queue.services.QueueDispatcherService;
import io.cloudslang.orchestrator.entities.Message;
import io.cloudslang.orchestrator.entities.SplitMessage;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.hamcrest.Matchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static ch.lambdaj.Lambda.filter;

/**
 * Date: 12/1/13
 *
 * @author
 */
public final class QueueDispatcherHelperServiceImpl implements QueueDispatcherHelperService {
    private final Logger logger = Logger.getLogger(getClass());

    @Autowired
    private QueueDispatcherService queueDispatcher;

    @Autowired
    private SplitJoinService splitJoinService;

    @Autowired
    private WorkerNodeService workerNodeService;


    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void dispatch(Serializable message) {
        Validate.notNull(message, "Messages is null");

        if(message instanceof ExecutionMessage){
            queueDispatcher.dispatch(Arrays.asList((ExecutionMessage)message));
        }
        else if(message instanceof SplitMessage){
            splitJoinService.split(Arrays.asList((SplitMessage)message));
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void dispatchBulk(List<? extends Serializable> messages, String bulkNumber, String workerUuid) {
        Validate.notNull(messages, "Messages list is null");

        if (logger.isDebugEnabled()){
            logger.debug("Dispatching bulk of " + messages.size() + " messages");
        }
        long t = System.currentTimeMillis();
        final AtomicInteger messagesCounter = new AtomicInteger(0);

        dispatch(messages, ExecutionMessage.class, new Handler<ExecutionMessage>() {
            @Override
            public void handle(List<ExecutionMessage> messages) {
                messagesCounter.addAndGet(messages.size());
                queueDispatcher.dispatch(messages);
            }
        });

        dispatch(messages, SplitMessage.class, new Handler<SplitMessage>() {
            @Override
            public void handle(List<SplitMessage> messages) {
                messagesCounter.addAndGet(messages.size());
                splitJoinService.split(messages);
            }
        });

        t = System.currentTimeMillis()-t;
        if (logger.isDebugEnabled()){
            logger.debug("Dispatching bulk of " + messagesCounter.get() + " messages was done in " + t + " ms");
        }
        if (messages.size() > messagesCounter.get()){
            logger.warn((messages.size() - messagesCounter.get()) + " messages were not being dispatched, since unknown type");
        }

        //We must update the bulk here in the same transaction as dispatch to make sure both are in or both are roll-backed!!!
        workerNodeService.updateBulkNumber(workerUuid, bulkNumber);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean isDbConnectionOk() {
        //just try select to check connection to DB
        try{
            workerNodeService.readAllWorkers();
            return true;
        }
        catch (Exception ex){
            return false;
        }
    }

    private <T extends Serializable> void dispatch(List<? extends Serializable> messages, Class<T> messageClass, Handler<T> handler) {
        @SuppressWarnings("unchecked")
        List<T> filteredMessages = (List<T>) filter(Matchers.instanceOf(messageClass), messages);
        if (!messages.isEmpty()) {
            handler.handle(filteredMessages);
        }
    }

    private interface Handler<T> {
        public void handle(List<T> messages);
    }
}
