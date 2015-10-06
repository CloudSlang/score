/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package io.cloudslang.engine.queue.services.recovery;

import io.cloudslang.engine.queue.entities.ExecStatus;
import io.cloudslang.engine.queue.entities.ExecutionMessage;
import io.cloudslang.engine.queue.services.ExecutionQueueService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * User: varelasa
 * Date: 22/07/14
 * Time: 13:24
 */
final public class MessageRecoveryServiceImpl implements MessageRecoveryService {

    private final Logger logger = Logger.getLogger(getClass());

    @Autowired
    private ExecutionQueueService executionQueueService;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean recoverMessagesBulk(String workerName, int defaultPoolSize) {

        List<ExecutionMessage> messages = executionQueueService.pollRecovery(workerName, defaultPoolSize,
                ExecStatus.ASSIGNED,
                ExecStatus.SENT,
                ExecStatus.IN_PROGRESS);

        logMessageRecovery(messages);
        enqueueMessages(messages,ExecStatus.RECOVERED);
        //noinspection RedundantIfStatement
        if (messages == null || messages.size() < defaultPoolSize){
           return false;
        }
        return true;
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public void logMessageRecovery(List<ExecutionMessage> messages) {
        if(!CollectionUtils.isEmpty(messages)){
            logger.warn("Will do recovery for " + messages.size() + " messages. ");
            if(logger.isDebugEnabled()){
                for(ExecutionMessage msg:messages){
                    logger.debug("Will do recovery for messages with ExecStateId = " + msg.getExecStateId());
                }
            }
        }
    }

    @Override
    @Transactional
    public void enqueueMessages(List<ExecutionMessage> messages, ExecStatus messageStatus) {
        if(!CollectionUtils.isEmpty(messages)){
            for(ExecutionMessage msg:messages){
                msg.setStatus(messageStatus);
                msg.setWorkerId(ExecutionMessage.EMPTY_WORKER);
                msg.incMsgSeqId();
            }
            executionQueueService.enqueue(messages);
        }
    }
}
