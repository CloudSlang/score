/*
 * Licensed to Hewlett-Packard Development Company, L.P. under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
*/
package org.eclipse.score.engine.queue.services.recovery;

import org.eclipse.score.engine.queue.entities.ExecStatus;
import org.eclipse.score.engine.queue.entities.ExecutionMessage;
import org.eclipse.score.engine.queue.services.ExecutionQueueService;
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
final public class MessageRecoveryServiceImpl implements MessageRecoveryService{

    private final Logger logger = Logger.getLogger(getClass());

    @Autowired
    private ExecutionQueueService executionQueueService;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean recoverMessagesBulk(String workerName, int defaultPoolSize) {

        List<ExecutionMessage> messages = executionQueueService.poll(workerName, defaultPoolSize,
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
