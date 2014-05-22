package com.hp.oo.engine.queue.services;

import com.hp.oo.engine.queue.entities.ExecutionMessage;
import com.hp.oo.engine.queue.entities.ExecutionMessageConverter;
import com.hp.oo.enginefacade.execution.ExecutionSummary;
import com.hp.oo.internal.sdk.execution.Execution;
import com.hp.score.services.RunStateService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;

/**
 * User: Amit Levin
 * Date: 19/09/12
 * Time: 15:14
 */
@SuppressWarnings("unused")
public class QueueListenerImpl implements QueueListener {

    private static Logger logger = Logger.getLogger(QueueListenerImpl.class);

    @Autowired
    private RunStateService runStateService;

    @Autowired
    private ExecutionMessageConverter executionMessageConverter;

    @Override
    public void onEnqueue(List<ExecutionMessage> messages, int queueSize) {
        if (logger.isDebugEnabled()) {
            logger.debug("Enqueue " + messages.size() + " messages:");
            logger.debug("queue size: " + queueSize);
            if (logger.isTraceEnabled()) {
                for (ExecutionMessage msg : messages) {
                    logger.trace("Enqueue msgId= " + msg.getMsgUniqueId() + ":" + msg.getMsgSeqId() + ",workerId=" + msg.getWorkerId() + ",status=" + msg.getStatus());
                }
            }
        }
    }

    @Override
    public void onPoll(List<ExecutionMessage> messages, int queueSize) {
        if (logger.isDebugEnabled()) {
            logger.debug("poll " + messages.size() + " messages:");
            logger.debug("queue size: " + queueSize);
            if (logger.isTraceEnabled()) {
                for (ExecutionMessage msg : messages) {
                    logger.trace("Poll msgId= " + msg.getMsgUniqueId() + ":" + msg.getMsgSeqId() + ",workerId=" + msg.getWorkerId() + ",status=" + msg.getStatus());
                }
            }
        }
    }

    @Override
    public void onTerminated(List<ExecutionMessage> messages) {
        for (ExecutionMessage executionMessage : messages) {
            //Only delete parent runs and not branches because the Terminated event of branches should not cause the
            //deletion of the entire run
            if (!isBranchExecution(executionMessage)) {
                runStateService.deleteRunState(executionMessage.getMsgId(), ExecutionSummary.EMPTY_BRANCH);
            }
        }
    }

    /*
    Parses the payload of the execution message and returns true if the execution is marked as a branch
     */
    private boolean isBranchExecution(ExecutionMessage executionMessage) {
        try {
            Execution execution = executionMessageConverter.extractExecution(executionMessage.getPayload());
            return execution.isBranch();
        } catch (IOException e) {
            logger.error("Unable to parse payload from execution message");
            return false;
        }
    }

    @Override
    public void onFailed(List<ExecutionMessage> messages) {
        for (ExecutionMessage executionMessage : messages) {
            runStateService.deleteRunState(executionMessage.getMsgId(), ExecutionSummary.EMPTY_BRANCH);
        }
    }

}
