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

package io.cloudslang.engine.queue.services;

import io.cloudslang.engine.queue.entities.ExecutionMessage;
import io.cloudslang.engine.queue.entities.ExecutionMessageConverter;
import io.cloudslang.orchestrator.services.ExecutionStateService;
import io.cloudslang.orchestrator.services.PauseResumeService;
import io.cloudslang.orchestrator.services.SplitJoinService;
import io.cloudslang.score.events.EventBus;
import io.cloudslang.score.events.ScoreEvent;
import io.cloudslang.score.facade.entities.Execution;
import io.cloudslang.score.facade.execution.ExecutionSummary;
import io.cloudslang.score.facade.execution.PauseReason;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.cloudslang.score.facade.execution.ExecutionStatus.CANCELED;
import static io.cloudslang.score.facade.execution.ExecutionSummary.EMPTY_BRANCH;
import static java.lang.String.valueOf;

/**
 * User:
 * Date: 19/09/12
 * Time: 15:14
 */
public class QueueListenerImpl implements QueueListener {

    private static Logger logger = Logger.getLogger(QueueListenerImpl.class);
    private static final String FLOW_TERMINATION_TYPE = "FLOW_TERMINATION_TYPE";
    private static final String STEP_TYPE = "STEP_TYPE";
    private static final String MULTI_INSTANCE = "MULTI_INSTANCE";

    @Autowired
    private ExecutionStateService executionStateService;

    @Autowired
    private ExecutionMessageConverter executionMessageConverter;

    @Autowired
    private EventBus eventBus;

    @Autowired
    private SplitJoinService splitJoinService;

    @Autowired
    private ScoreEventFactory scoreEventFactory;

    @Autowired
    private PauseResumeService pauseResumeService;

    @Override
    public void prePersist(List<ExecutionMessage> messages) {
    }

    @Override
    public void onEnqueue(List<ExecutionMessage> messages, int queueSize) {
        if (logger.isDebugEnabled()) {
            logger.debug("Enqueue " + messages.size() + " messages:");
            logger.debug("queue size: " + queueSize);
            if (logger.isTraceEnabled()) {
                for (ExecutionMessage msg : messages) {
                    String message = String.format(
                            "Enqueue msgId= %s:%s,workerId=%s,status=%s",
                            msg.getMsgUniqueId(), valueOf(msg.getMsgSeqId()), msg.getWorkerId(), msg.getStatus().toString()
                    );
                    logger.trace(message);
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
                    String message = String.format(
                            "Poll msgId= %s:%s,workerId=%s,status=%s",
                            msg.getMsgUniqueId(), valueOf(msg.getMsgSeqId()), msg.getWorkerId(), msg.getStatus().toString()
                    );
                    logger.trace(message);
                }
            }
        }
    }

    @Override
    public void onTerminated(List<ExecutionMessage> messages) {
        ScoreEvent[] scoreEvents = handleTerminatedMessages(messages);
        if (scoreEvents.length > 0) {
            try {
                eventBus.dispatch(scoreEvents);
            } catch (InterruptedException e) {
                logger.error("Thread is interrupted. Ignoring... ", e);
            }
        }
    }

    private ScoreEvent[] handleTerminatedMessages(List<ExecutionMessage> messages) {
        List<ScoreEvent> scoreEvents = new ArrayList<>(messages.size());
        List<Execution> branches = new ArrayList<>();

        for (ExecutionMessage executionMessage : messages) {
            Execution execution = extractExecution(executionMessage);
            Boolean isBranch = isBranch(execution);
            if (!isBranch) {
                scoreEvents.add(scoreEventFactory.createFinishedEvent(execution));
            } else {
                branches.add(execution);
                scoreEvents.add(scoreEventFactory.createFinishedBranchEvent(execution));
            }
        }

        if (CollectionUtils.isNotEmpty(branches)) {
            splitJoinService.endBranch(branches);
        }

        return scoreEvents.toArray(new ScoreEvent[scoreEvents.size()]);
    }

    /**
     * Returns true when the execution is a branch with the new branch mechanism
     * It will return true for executions of parallel, multi-instance, sub-flows and non blocking
     */
    private boolean isBranch(Execution execution) {
        return execution != null && !StringUtils.isEmpty(execution.getSystemContext().getBranchId());
    }

    private Execution extractExecution(ExecutionMessage executionMessage) {
        return executionMessageConverter.extractExecution(executionMessage.getPayload());
    }

    @Override
    public void onFailed(List<ExecutionMessage> messages) {
        deleteExecutionStateObjects(messages);
        ScoreEvent[] events = createFailureEvents(messages);
        if (events.length > 0) {
            try {
                eventBus.dispatch(events);
            } catch (InterruptedException e) {
                logger.error("Thread is interrupted. Ignoring... ", e);
            }
        }
    }

    @Override
    public void onPersistMessage(List<ExecutionMessage> messages) {
        //do nothing
    }

    private Long pauseExecution(Execution execution, PauseReason pauseReason) {
        String branchId = execution.getSystemContext().getBranchId();

        ExecutionSummary pe = pauseResumeService.readPausedExecution(execution.getExecutionId(), branchId);

        //Check if this execution is not paused already (by user)
        Long pauseId;
        if (pe == null) {
            // When cancel execution and no worker in group it should return to be paused without any termination type
            execution.getSystemContext().setFlowTerminationType(null);
            pauseId = pauseResumeService.pauseExecution(execution.getExecutionId(), branchId, pauseReason);
            pauseResumeService.writeExecutionObject(execution.getExecutionId(), branchId, execution);
        } else {
            pauseId = null;
            //If yes - just write the object
            pauseResumeService.writeExecutionObject(execution.getExecutionId(), branchId, execution);
        }
        return pauseId;
    }

    private ScoreEvent[] createFailureEvents(List<ExecutionMessage> messages) {
        Execution execution;
        List<ScoreEvent> events = new ArrayList<>(messages.size());
        for (ExecutionMessage executionMessage : messages) {
            execution = extractExecution(executionMessage);
            if (failedBecauseNoWorker(execution)) {
                Long pauseID = pauseExecution(execution, PauseReason.NO_WORKERS_IN_GROUP);
                events.add(scoreEventFactory.createNoWorkerEvent(execution, pauseID));
            } else if (failedBecausePreconditionNotFulfilled(execution)) {
                pauseExecution(execution, PauseReason.PRECONDITION_NOT_FULFILLED);
            } else if (isBranch(execution)) {
                splitJoinService.endBranch(Arrays.asList(execution));
                events.add(scoreEventFactory.createFailedBranchEvent(execution));
            } else {
                events.add(scoreEventFactory.createFailureEvent(execution));
            }
        }
        return events.toArray(new ScoreEvent[events.size()]);
    }

    private void deleteExecutionStateObjects(List<ExecutionMessage> messages) {
        for (ExecutionMessage executionMessage : messages) {
            if (!failedBecauseNoWorker(extractExecution(executionMessage)) && !failedBecausePreconditionNotFulfilled(extractExecution(executionMessage))) {
                executionStateService.deleteExecutionState(Long.valueOf(executionMessage.getMsgId()), EMPTY_BRANCH);
            }
        }
    }

    private boolean failedBecauseNoWorker(Execution execution) {
        return execution != null && !StringUtils.isEmpty(execution.getSystemContext().getNoWorkerInGroupName());
    }

    private boolean failedBecausePreconditionNotFulfilled(Execution execution) {
        return execution != null && execution.getSystemContext().getPreconditionNotFulfilled();
    }
}
