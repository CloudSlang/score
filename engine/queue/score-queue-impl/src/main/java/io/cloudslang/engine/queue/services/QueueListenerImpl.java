/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package io.cloudslang.engine.queue.services;

import io.cloudslang.engine.queue.entities.ExecutionMessage;
import io.cloudslang.engine.queue.entities.ExecutionMessageConverter;
import io.cloudslang.orchestrator.entities.Message;
import io.cloudslang.orchestrator.entities.SplitMessage;
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

/**
 * User:
 * Date: 19/09/12
 * Time: 15:14
 */
public class QueueListenerImpl implements QueueListener {

	private static Logger logger = Logger.getLogger(QueueListenerImpl.class);

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
		ScoreEvent[] scoreEvents = handleTerminatedMessages(messages);
		if (scoreEvents.length > 0) {
            try {
                eventBus.dispatch(scoreEvents);
            } catch (InterruptedException e) {
                logger.error("Thread is interrupted. Ignoring... ", e);
            }
        }
    }

    @Override
    public void onCorrupted(Message message) {
        ScoreEvent[] scoreEvents = handleCorruptedMessage(message);
        if (scoreEvents.length > 0) {
            try {
                eventBus.dispatch(scoreEvents);
            } catch (InterruptedException e) {
                logger.error("Thread is interrupted. Ignoring... ", e);
            }
        }
    }

    private ScoreEvent[] handleCorruptedMessage(Message message) {

        Execution execution = null;

        if(message instanceof ExecutionMessage){
            execution = extractExecution((ExecutionMessage) message);
        }
        else if(message instanceof SplitMessage){
            execution = ((SplitMessage) message).getParent();
        }

        return handleCorruptedMessage(execution, message.getExceptionMessage());

    }

    private ScoreEvent[] handleCorruptedMessage(Execution execution, String exceptionMessage) {
        List<ScoreEvent> scoreEvents = new ArrayList<>();

        String branchId = execution.getSystemContext().getBranchId();
        Long executionId = execution.getExecutionId();

        Boolean isBranch = isBranch(execution);

        scoreEvents.add(scoreEventFactory.createCorruptedMessageEvent(execution, exceptionMessage));

        if (!isBranch) {
            executionStateService.deleteExecutionState(executionId, ExecutionSummary.EMPTY_BRANCH);
        }
        else {
            executionStateService.deleteExecutionState(executionId, branchId);
        }

        //for corrupted execution - we need to delete its parents executions (all levels!!!) and finished branches
        splitJoinService.deleteSuspendedExecutionsWithBranches(executionId);

        return scoreEvents.toArray(new ScoreEvent[scoreEvents.size()]);
    }


    private ScoreEvent[] handleTerminatedMessages(List<ExecutionMessage> messages) {
        List<ScoreEvent> scoreEvents = new ArrayList<>(messages.size());
        List<Execution> branches = new ArrayList<>();

        for (ExecutionMessage executionMessage : messages) {
            Execution execution = extractExecution(executionMessage);
            Boolean isBranch = isBranch(execution);
            if(!isBranch){
                scoreEvents.add(scoreEventFactory.createFinishedEvent(execution));
                executionStateService.deleteExecutionState(Long.valueOf(executionMessage.getMsgId()), ExecutionSummary.EMPTY_BRANCH);
            }
            else{
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

	private Long pauseExecution(Execution execution) {
		String branchId = execution.getSystemContext().getBranchId();

		ExecutionSummary pe = pauseResumeService.readPausedExecution(execution.getExecutionId(), branchId);

		//Check if this execution is not paused already (by user)
		Long pauseId;
		if (pe == null) {
			pauseId = pauseResumeService.pauseExecution(execution.getExecutionId(), branchId, PauseReason.NO_WORKERS_IN_GROUP);
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
				Long pauseID = pauseExecution(execution);
				events.add(scoreEventFactory.createNoWorkerEvent(execution, pauseID));
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
			if (!failedBecauseNoWorker(extractExecution(executionMessage))) {
				executionStateService.deleteExecutionState(Long.valueOf(executionMessage.getMsgId()), ExecutionSummary.EMPTY_BRANCH);
			}
		}
	}

	private boolean failedBecauseNoWorker(Execution execution) {
		return execution != null && !StringUtils.isEmpty(execution.getSystemContext().getNoWorkerInGroupName());
	}

}
