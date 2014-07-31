package com.hp.oo.engine.queue.services;

import com.hp.oo.internal.sdk.execution.Execution;
import com.hp.oo.internal.sdk.execution.ExecutionConstants;
import com.hp.score.events.EventConstants;
import com.hp.score.events.ScoreEvent;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * User: maromg
 * Date: 30/07/2014
 */
@Component
public class ScoreEventFactory {

//	@Autowired
//	private PauseResumeService pauseResumeService;

//	@Autowired
//	private OrchestratorService orchestratorService;

	ScoreEvent createFinishedEvent(Execution execution) {
		String eventType = EventConstants.SCORE_FINISHED_EVENT;
		Serializable eventData = createFinishedEventData(execution);
		return new ScoreEvent(eventType, eventData);
	}

	private Serializable createFinishedEventData(Execution execution) {
		Map<String, Serializable> eventData = new HashMap<>();
		eventData.put(ExecutionConstants.SYSTEM_CONTEXT, execution.getSystemContext());
		eventData.put(ExecutionConstants.EXECUTION_ID_CONTEXT, execution.getExecutionId());
		eventData.put(EventConstants.EXECUTION_CONTEXT, (Serializable) execution.getContexts());
		eventData.put(EventConstants.IS_BRANCH, isBranchNewMechanism(execution));
		return (Serializable) eventData;
	}

	public ScoreEvent createFailedBranchEvent(Execution execution) {
		String eventType = EventConstants.SCORE_BRANCH_FAILURE_EVENT;
		Serializable eventData = createBranchFailureEventData(execution);
		return new ScoreEvent(eventType, eventData);
	}

	private Serializable createBranchFailureEventData(Execution execution) {
		Map<String, Serializable> eventData = new HashMap<>();
		eventData.put(ExecutionConstants.SYSTEM_CONTEXT, execution.getSystemContext());
		eventData.put(ExecutionConstants.EXECUTION_ID_CONTEXT, execution.getExecutionId());
		eventData.put(ExecutionConstants.BRANCH_ID, execution.getBranchId());
		return (Serializable) eventData;
	}

	public ScoreEvent createFailureEvent(Execution execution) {
		String eventType = EventConstants.SCORE_FAILURE_EVENT;
		Serializable eventData = createFailureEventData(execution);
		return new ScoreEvent(eventType, eventData);
	}

	private Serializable createFailureEventData(Execution execution) {
		Map<String, Serializable> eventData = new HashMap<>();
		eventData.put(ExecutionConstants.SYSTEM_CONTEXT, execution.getSystemContext());
		eventData.put(ExecutionConstants.EXECUTION_ID_CONTEXT, execution.getExecutionId());
		eventData.put(ExecutionConstants.BRANCH_ID, execution.getBranchId());
		eventData.put(ExecutionConstants.RUNNING_EXECUTION_PLAN_ID, execution.getRunningExecutionPlanId());
		return (Serializable) eventData;
	}

	/**
	 * Returns true when the execution is a branch with the new branch mechanism
	 * It will return true for executions of parallel, multi-instance and sub-flows but not for non-blocking
	 * (which is the old mechanism)
	 */
	private boolean isBranchNewMechanism(Execution execution) {
		return execution.isBranch() && execution.isNewBranchMechanism();
	}

	public ScoreEvent createNoWorkerEvent(Execution execution) {
		String eventType = EventConstants.SCORE_NO_WORKER_FAILURE_EVENT;
		Serializable eventData = createNoWorkerFailureEventData(execution);
		return new ScoreEvent(eventType, eventData);
	}

	private Serializable createNoWorkerFailureEventData(Execution execution) {
//		String flowUuid = extractFlowUuid(execution.getRunningExecutionPlanId());
//		Long pauseId = extractPauseId(execution.getExecutionId(), execution.getBranchId());

		Map<String, Serializable> eventData = new HashMap<>();
		eventData.put(ExecutionConstants.SYSTEM_CONTEXT, execution.getSystemContext());
		eventData.put(ExecutionConstants.EXECUTION_ID_CONTEXT, execution.getExecutionId());
		eventData.put(ExecutionConstants.BRANCH_ID, execution.getBranchId());
		eventData.put(ExecutionConstants.RUNNING_EXECUTION_PLAN_ID, execution.getRunningExecutionPlanId());
//		eventData.put(ExecutionConstants.FLOW_UUID, flowUuid);
//		eventData.put(EventConstants.PAUSE_ID, pauseId);
		return (Serializable) eventData;
	}

//	private String extractFlowUuid(Long runningExecutionPlanId) {
//		return orchestratorService.getFlowUuidByRunningExecutionPlanId(runningExecutionPlanId);
//	}

//	private Long extractPauseId(Long executionId, String branchId) {
//		ExecutionSummary executionSummary = pauseResumeService.readPausedExecution(executionId, branchId);
//		return executionSummary.getExecutionId()
//	}

}
