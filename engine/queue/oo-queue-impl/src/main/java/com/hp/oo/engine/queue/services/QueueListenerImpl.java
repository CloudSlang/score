package com.hp.oo.engine.queue.services;

import com.hp.oo.engine.queue.entities.ExecutionMessage;
import com.hp.oo.engine.queue.entities.ExecutionMessageConverter;
import com.hp.oo.enginefacade.execution.ExecutionSummary;
import com.hp.oo.internal.sdk.execution.Execution;
import com.hp.oo.internal.sdk.execution.ExecutionConstants;
import com.hp.oo.orchestrator.services.SplitJoinService;
import com.hp.score.api.ScoreEvent;
import com.hp.score.events.EventBus;
import com.hp.score.events.EventConstants;
import com.hp.score.services.ExecutionStateService;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: Amit Levin
 * Date: 19/09/12
 * Time: 15:14
 */
@SuppressWarnings("unused")
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
		List<ScoreEvent> scoreEvents = handleTerminatedMessages(messages);
		if (scoreEvents.size() > 0) {
			eventBus.dispatch(scoreEvents.toArray(new ScoreEvent[scoreEvents.size()]));
		}
	}

	private List<ScoreEvent> handleTerminatedMessages(List<ExecutionMessage> messages) {
		List<ScoreEvent> scoreEvents = new ArrayList<>(messages.size());

		for (ExecutionMessage executionMessage : messages) {
			handleTerminatedMessage(executionMessage);
			scoreEvents.add(createScoreEvent(executionMessage));
		}
		return scoreEvents;
	}

	private void handleTerminatedMessage(ExecutionMessage executionMessage) {
		//Only delete parent runs and not branches because the Terminated event of branches should not cause the
		//deletion of the entire run
		if (!isBranchExecution(executionMessage)) {
			executionStateService.deleteExecutionState(Long.valueOf(executionMessage.getMsgId()), ExecutionSummary.EMPTY_BRANCH);
		} else {
			Execution execution = extractExecution(executionMessage);
			finishBranchExecution(execution);
		}
	}

	//The logic for this method was copied from oo's FinishedFlowEventsListener
	private void finishBranchExecution(Execution execution) {
		if (execution.isNewBranchMechanism()) {
			splitJoinService.endBranch(Arrays.asList(execution));
		}
	}

	private ScoreEvent createScoreEvent(ExecutionMessage executionMessage) {
		String eventType = EventConstants.SCORE_FINISHED_EVENT;
		Serializable eventData = createEventData(executionMessage);
		return new ScoreEvent(eventType, eventData);
	}

	private Serializable createEventData(ExecutionMessage executionMessage) {
		Execution execution = extractExecution(executionMessage);

		Map<String, Serializable> eventData = new HashMap<>();
		eventData.put(ExecutionConstants.SYSTEM_CONTEXT, execution.getSystemContext());
		eventData.put(ExecutionConstants.EXECUTION_ID_CONTEXT, execution.getExecutionId());
		eventData.put(ExecutionConstants.EXECUTION_CONTEXT, (Serializable) execution.getContexts());
		eventData.put(ExecutionConstants.IS_BRANCH, execution.isBranch() && execution.isNewBranchMechanism());
		return (Serializable) eventData;
	}

	/*
	Parses the payload of the execution message and returns true if the execution is marked as a branch
	 */
	private boolean isBranchExecution(ExecutionMessage executionMessage) {
		Execution execution = extractExecution(executionMessage);
		return execution != null && execution.isBranch();
	}

	private Execution extractExecution(ExecutionMessage executionMessage) {
		try {
			return executionMessageConverter.extractExecution(executionMessage.getPayload());
		} catch (IOException e) {
			logger.error("Unable to parse payload from execution message");
			return null;
		}
	}

	@Override
	public void onFailed(List<ExecutionMessage> messages) {
		for (ExecutionMessage executionMessage : messages) {
			if (!failedBecauseNoWorker(executionMessage)) {
				executionStateService.deleteExecutionState(Long.valueOf(executionMessage.getMsgId()), ExecutionSummary.EMPTY_BRANCH);
			}
		}
	}

	private boolean failedBecauseNoWorker(ExecutionMessage executionMessage) {
		Execution execution = extractExecution(executionMessage);
		return execution != null && !StringUtils.isEmpty(execution.getSystemContext().getNoWorkerInGroupName());
	}

}
