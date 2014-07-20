package com.hp.oo.execution.services;

import com.hp.oo.broker.entities.BranchContextHolder;
import com.hp.oo.broker.entities.RunningExecutionPlan;
import com.hp.oo.broker.services.RuntimeValueService;
import com.hp.oo.enginefacade.execution.ExecutionEnums.ExecutionStatus;
import com.hp.oo.enginefacade.execution.ExecutionEnums.LogLevelCategory;
import com.hp.oo.enginefacade.execution.ExecutionSummary;
import com.hp.oo.enginefacade.execution.PauseReason;
import com.hp.oo.execution.reflection.ReflectionAdapter;
import com.hp.oo.execution.services.dbsupport.WorkerDbSupportService;
import com.hp.oo.internal.sdk.execution.Execution;
import com.hp.oo.internal.sdk.execution.ExecutionConstants;
import com.hp.oo.internal.sdk.execution.OOContext;
import com.hp.oo.orchestrator.services.PauseResumeService;
import com.hp.oo.orchestrator.services.configuration.WorkerConfigurationService;
import com.hp.score.api.ExecutionStep;
import com.hp.score.api.ScoreEvent;
import com.hp.score.api.StartBranchDataContainer;
import com.hp.score.events.EventBus;
import com.hp.score.events.EventConstants;
import com.hp.score.lang.SystemContext;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Date: 8/1/11
 *
 * @author Dima Rassin
 */
public final class ExecutionServiceImpl implements ExecutionService {
	private final Logger logger = Logger.getLogger(getClass());
	private static final String SYSTEM_CONTEXT = "systemContext";
	private static final String EXECUTION = "execution";

	@Autowired
	private PauseResumeService pauseService;

	@Autowired
	private ReflectionAdapter reflectionAdapter;

	@Autowired
	private WorkerDbSupportService workerDbSupportService;

	@Autowired
	private WorkerConfigurationService configurationService;

	@Autowired
	private RuntimeValueService runtimeValueService;

	@Autowired
	private WorkerRecoveryManager recoveryManager;

	@Autowired
	private EventBus eventBus;

	@Override
	public Execution execute(Execution execution) {
		try {

			// handle flow cancellation
			if (handleCancelledFlow(execution)) {
				return execution;
			}

			ExecutionStep currStep = loadExecutionStep(execution);

			//Check if this execution was paused
			if (!isDebuggerMode(execution.getSystemContext()) && handlePausedFlow(execution)) {
				return null;
			}

			//dum bus event
			dumpBusEvents(execution);

			//Run the execution step
			executeStep(execution, currStep);

			//Run the navigation
			navigate(execution, currStep);

			// currently handles groups and jms optimizations
			postExecutionSettings(execution);

			//If execution was  paused in language - to avoid delay of configuration
			if (execution.getSystemContext().isPaused()) {
				if (handlePausedFlowAfterStep(execution)) {
					return null;
				}
			}
			//dum bus event
			dumpBusEvents(execution);

			if (logger.isDebugEnabled()) {
				logger.debug("End of step: " + execution.getPosition() + " in execution id: " + execution.getExecutionId());
			}

			return execution;

		} catch (Exception ex) {
			//In case this is execution of branch that failed - need special treatment
			if (execution.getSystemContext().containsKey(ExecutionConstants.SPLIT_ID)) {
				handleBranchFailure(execution, ex);
				execution.setPosition(-1L); //finishing this branch but not finishing the entire flow
				return execution;
			} else {
				logger.error("Error during execution: ", ex);
				execution.getSystemContext().put(ExecutionConstants.EXECUTION_STEP_ERROR_KEY, ex.getMessage()); //this is done only fo reporting
				execution.getSystemContext().put(ExecutionConstants.FLOW_TERMINATION_TYPE, ExecutionStatus.SYSTEM_FAILURE);
				execution.setPosition(null); //this ends the flow!!!
				return execution;
			}
		}
	}

	@Override
	//returns null in case the split was not done - flow is paused or cancelled
	public List<Execution> executeSplit(Execution execution) {
		try {
			List<Execution> newExecutions = new ArrayList<>();

			// handle flow cancellation
			if (handleCancelledFlow(execution)) {
				newExecutions.add(execution);
				return newExecutions;
			}

			ExecutionStep currStep = loadExecutionStep(execution);

			//Check if this execution was paused
			if (!isDebuggerMode(execution.getSystemContext()) && handlePausedFlow(execution)) {
				return null;
			}

			//dum bus event
			dumpBusEvents(execution);

			executeStep(execution, currStep);

			failFlowIfSplitStepFailed(execution);

			//Run the split step
			List<StartBranchDataContainer> newBranches = execution.getSystemContext().removeBranchesData();

			newExecutions = createChildExecutions(execution.getExecutionId(), newBranches);

			//Run the navigation
			navigate(execution, currStep);

			if (logger.isDebugEnabled()) {
				logger.debug("End of step: " + execution.getPosition() + " in execution id: " + execution.getExecutionId());
			}

			return newExecutions;
		} catch (Exception ex) {
			logger.error("Exception during the split step!", ex);
			throw ex;
		}
	}

	private void failFlowIfSplitStepFailed(Execution execution) {
		if (execution.getSystemContext().containsKey(ExecutionConstants.EXECUTION_STEP_ERROR_KEY)) {
			String exception = (String) execution.getSystemContext().get(ExecutionConstants.EXECUTION_STEP_ERROR_KEY);
			execution.getSystemContext().put(ExecutionConstants.FLOW_TERMINATION_TYPE, ExecutionStatus.SYSTEM_FAILURE);
			execution.setPosition(null); //this ends the flow!!!
			try {
				createErrorEvent(exception, "Error occurred during split step ", LogLevelCategory.STEP_SPLIT_ERROR, execution.getSystemContext());
			} catch (RuntimeException eventEx) {
				logger.error("Failed to create event: ", eventEx);
			}
			throw new RuntimeException(exception);

		}
	}

	private List<Execution> createChildExecutions(Long executionId, List<StartBranchDataContainer> newBranches) {
		List<Execution> newExecutions = new ArrayList<>();
		String splitId = UUID.randomUUID().toString();

		for (int i = 0; i < newBranches.size(); i++) {
			StartBranchDataContainer from = newBranches.get(i);
			Execution to = new Execution(executionId,
					from.getExecutionPlanId(),
					from.getStartPosition(),
					from.getContexts(),
					from.getSystemContext());

			to.putSplitId(splitId);
			to.putBranchId(splitId + ":" + (i + 1));
			newExecutions.add(to);
		}
		return newExecutions;
	}

	@Override
	public boolean isSplitStep(Execution execution) {
		ExecutionStep currStep = loadExecutionStep(execution);
		return currStep.isSplitStep();
	}

	protected boolean isExecutionTerminating(Execution execution) {
		return (execution.getPosition() == null || execution.getPosition() == -1L || execution.getPosition() == -2L);
	}

	//This method deals with the situation when a branch execution was terminated because of a system failure - not execution exception
	protected void handleBranchFailure(Execution execution, Exception exception) {

		String splitId = (String) execution.getSystemContext().get(ExecutionConstants.SPLIT_ID);
		String branchId = (String) execution.getSystemContext().get(ExecutionConstants.BRANCH_ID);

		logger.error("Branch failed due to SYSTEM FAILURE! Execution id: " + execution.getExecutionId() + " Branch id: " + branchId, exception);

		BranchContextHolder branchContextHolder = new BranchContextHolder();
		branchContextHolder.setSplitId(splitId);
		branchContextHolder.setBranchId(branchId);
		branchContextHolder.setExecutionId((String) execution.getSystemContext().get(ExecutionConstants.EXECUTION_ID_CONTEXT));

		Map<String, OOContext> context = new HashMap<>();
		branchContextHolder.setContext(context);
		branchContextHolder.setBranchException(exception.getMessage());

		while (!recoveryManager.isInRecovery()) {
			try {
				workerDbSupportService.createBranchContext(branchContextHolder);
				//todo - maybe add events like in endBranch action
				try {
					clearBranchLocks(execution);
				} catch (Exception ex) {
					logger.error("Failed to clear locks on execution " + execution.getExecutionId(), ex);
				}
				return;
			} catch (Exception ex) {
				logger.error("Failed to save branch failure. Retrying...", ex);
				try {
					Thread.sleep(5000L);
				} catch (InterruptedException iex) {/*do nothing*/}
			}
		}
	}

	protected void clearBranchLocks(Execution execution) {
		@SuppressWarnings("unchecked")
		Set<String> acquiredLockIds = (Set<String>) execution.getSystemContext().get(ExecutionConstants.ACQUIRED_LOCKS);
		if (acquiredLockIds != null) {
			for (String lockId : acquiredLockIds) {
				runtimeValueService.remove(ExecutionConstants.LOCK_PREFIX_IN_DB + lockId);
			}
			execution.getSystemContext().remove(ExecutionConstants.ACQUIRED_LOCKS);
		}
	}

	protected boolean handleCancelledFlow(Execution execution) {

		Long executionId = execution.getExecutionId();

		List<Long> cancelledExecutions = configurationService.getCancelledExecutions(); // in this case - just check if need to cancel. It will set as cancelled later on QueueEventListener
		boolean executionIsCancelled = cancelledExecutions.contains(executionId);

		//Another scenario of getting canceled - it was cancelled from the SplitJoinService (the configuration can still be not updated). Defect #:22060
		if (ExecutionStatus.CANCELED.equals(execution.getSystemContext().get(ExecutionConstants.FLOW_TERMINATION_TYPE))) {
			executionIsCancelled = true;
		}

		if (executionIsCancelled) {
			// NOTE: an execution can be cancelled directly from CancelExecutionService, if it's currently paused.
			// Thus, if you change the code here, please check CancelExecutionService as well.
			execution.getSystemContext().put(ExecutionConstants.FLOW_TERMINATION_TYPE, ExecutionStatus.CANCELED);
			execution.setPosition(null);
			return true;
		}
		return false;
	}

	// check if the execution should be Paused, and pause it if needed
	protected boolean handlePausedFlow(Execution execution) {
		String branchId = (String) execution.getSystemContext().get(ExecutionConstants.BRANCH_ID);

		PauseReason reason = findPauseReason(execution.getExecutionId(), branchId);

		if (reason != null) { // need to pause the execution
			pauseFlow(reason, execution);
			return true;
		} else {
			return false;
		}
	}

	// no need to check if paused - because this is called after the step, when the Pause flag exists in the context
	private boolean handlePausedFlowAfterStep(Execution execution) {
		String branchId = (String) execution.getSystemContext().get(ExecutionConstants.BRANCH_ID);

		PauseReason reason = null;
		ExecutionSummary execSummary = pauseService.readPausedExecution(execution.getExecutionId(), branchId);
		if (execSummary != null && execSummary.getStatus().equals(ExecutionStatus.PENDING_PAUSE)) {
			reason = execSummary.getPauseReason();
		}

		if (reason != null) { // need to pause the execution
			pauseFlow(reason, execution);
			return true;
		} else {
			return false;
		}
	}

	private void pauseFlow(PauseReason reason, Execution execution) {

		SystemContext systemContext = execution.getSystemContext();
		Long executionId = execution.getExecutionId();
		String branchId = (String) systemContext.get(ExecutionConstants.BRANCH_ID);

		//If USER_PAUSED send such event
		if (!isDebuggerMode(execution.getSystemContext()) && reason.equals(PauseReason.USER_PAUSED)) {
			if (branchId != null) {
				// we pause the branch because the Parent was user-paused (see findPauseReason)
				pauseService.pauseExecution(executionId, branchId, reason); // this creates a DB record for this branch, as Pending-paused
			}
		}
		addPauseEvent(systemContext);

		//dump bus events here because out side is too late
		dumpBusEvents(execution);

		//Write execution to the db! Pay attention - do not do anything to the execution or its context after this line!!!
		pauseService.writeExecutionObject(executionId, branchId, execution);

		if (logger.isDebugEnabled()) {
			logger.debug("Execution with execution_id: " + execution.getExecutionId() + " is paused!");
		}
	}

	private void addPauseEvent(SystemContext systemContext) {
		//TODO : add pause reason??
		HashMap<String, Serializable> eventData = new HashMap<>();
		eventData.put(ExecutionConstants.SYSTEM_CONTEXT, new HashMap<>(systemContext));
		ScoreEvent eventWrapper = new ScoreEvent(EventConstants.SCORE_PAUSED_EVENT, eventData);
		eventBus.dispatch(eventWrapper);
	}

	private PauseReason findPauseReason(Long executionId, String branchId) {

		// 1. Check the configuration according to branch (can be null or not null...)
		if (configurationService.isExecutionPaused(executionId, branchId)) {
			ExecutionSummary execSummary = pauseService.readPausedExecution(executionId, branchId);
			if (execSummary != null && execSummary.getStatus().equals(ExecutionStatus.PENDING_PAUSE)) {
				return execSummary.getPauseReason();
			}
			// 2. Check the parent if we're in branch (subflow or MI\Parallel lane).
			// If the user pressed Pause on the Parent then we need to pause the branch (the parent is in the Suspended table).
		} else if (branchId != null &&
				configurationService.isExecutionPaused(executionId, null)) {
			ExecutionSummary execSummary = pauseService.readPausedExecution(executionId, null);
			if (execSummary != null && execSummary.getStatus().equals(ExecutionStatus.PENDING_PAUSE)) {
				PauseReason reason = execSummary.getPauseReason();
				// we only care about User-Paused here!
				// we don't want to Pause if the parent is paused due to branch_paused! (other branch is paused for some reason (e.g. required_input), so the parent is paused as well).
				if (PauseReason.USER_PAUSED.equals(reason)) {
					return reason;
				}
			}
		}
		return null; // not paused
	}

	private boolean isDebuggerMode(Map<String, Serializable> systemContext) {

		Boolean isDebuggerMode = (Boolean) systemContext.get(ExecutionConstants.DEBUGGER_MODE);
		if (isDebuggerMode == null) {
			return false;
		}

		return isDebuggerMode;
	}

	private void dumpBusEvents(Execution execution) {
		@SuppressWarnings("unchecked") ArrayDeque<ScoreEvent> eventsQueue = execution.getSystemContext().getEvents();
		if (eventsQueue == null) {
			return;
		}
		for (ScoreEvent eventWrapper : eventsQueue) {
			eventBus.dispatch(eventWrapper);
		}
		eventsQueue.clear();
	}

	protected ExecutionStep loadExecutionStep(Execution execution) {
		RunningExecutionPlan runningExecutionPlan;
		if (execution != null) {
			//Optimization for external workers - run the content only without loading the execution plan
			if (execution.getSystemContext().get(ExecutionConstants.CONTENT_EXECUTION_STEP) != null) {
				return (ExecutionStep) execution.getSystemContext().get(ExecutionConstants.CONTENT_EXECUTION_STEP);
			} else {
				Long position = execution.getPosition();
				if (position != null) {
					runningExecutionPlan = workerDbSupportService.readExecutionPlanById(execution.getRunningExecutionPlanId());
					if (runningExecutionPlan != null) {
						ExecutionStep currStep = runningExecutionPlan.getExecutionPlan().getStep(position);

						if (logger.isDebugEnabled()) {
							logger.debug("Begin step: " + position + " in flow " + runningExecutionPlan.getExecutionPlan().getFlowUuid() + " [" + execution.getExecutionId() + "]");
						}

						if (currStep != null) {
							return currStep;
						}
					}
				}
			}
		}
		//If we got here - one of the objects was null
		throw new RuntimeException("Failed to load ExecutionStep!");
	}

	protected void executeStep(Execution execution, ExecutionStep currStep) {
		try {
			Map<String, Object> stepData = prepareStepData(execution, currStep);
			reflectionAdapter.executeControlAction(currStep.getAction(), stepData);
		} catch (RuntimeException ex) {
			handleStepExecutionException(execution, ex);
		}
	}

	private void handleStepExecutionException(Execution execution, RuntimeException ex) {
		logger.error("Error occurred during operation execution.  Execution id: " + execution.getExecutionId(), ex);
		execution.getSystemContext().put(ExecutionConstants.EXECUTION_STEP_ERROR_KEY, ex.getMessage());
	}

	private Map<String, Object> prepareStepData(Execution execution, ExecutionStep currStep) {
		Map<String, Object> stepData = new HashMap<>(currStep.getActionData());
		//We add all the contexts to the step data - so inside of each control action we will have access to all contexts
		addContextData(stepData, execution);

		return stepData;
	}

	private void createErrorEvent(String ex, String logMessage,
								  LogLevelCategory logLevelCategory, SystemContext systemContext
	) {
		HashMap<String, Serializable> eventData = new HashMap<>();
		eventData.put(ExecutionConstants.SYSTEM_CONTEXT, new HashMap<>(systemContext));
		eventData.put(ExecutionConstants.SCORE_ERROR_MSG, ex);
		eventData.put(ExecutionConstants.SCORE_ERROR_LOG_MSG, logMessage);
		eventData.put(ExecutionConstants.SCORE_ERROR_TYPE, logLevelCategory.getCategoryName());
		ScoreEvent eventWrapper = new ScoreEvent(EventConstants.SCORE_ERROR_EVENT, eventData);
		eventBus.dispatch(eventWrapper);

	}

	protected void navigate(Execution execution, ExecutionStep currStep) {
		Long position;
		try {
			if (currStep.getNavigation() != null) {
				Map<String, Object> navigationData = new HashMap<>(currStep.getNavigationData());
				//We add all the contexts to the step data - so inside of each control action we will have access to all contexts
				addContextData(navigationData, execution);
				position = (Long) reflectionAdapter.executeControlAction(currStep.getNavigation(), navigationData);
				execution.setPosition(position);
			} else {
				execution.setPosition(null);  //terminate the flow - we got to the last step!
			}
		} catch (RuntimeException navEx) {
			//If Exception occurs in navigation (almost impossible since now we always have Flow Exception Step) we can not continue since we don't know which step is the next step...
			// terminating...
			logger.error("Error occurred during navigation execution. Execution id: " + execution.getExecutionId(), navEx);
			execution.getSystemContext().put(ExecutionConstants.EXECUTION_STEP_ERROR_KEY, navEx.getMessage()); //this is done only fo reporting
			execution.getSystemContext().put(ExecutionConstants.FLOW_TERMINATION_TYPE, ExecutionStatus.SYSTEM_FAILURE);
			execution.setPosition(null); //this ends the flow!!!

			try {
				createErrorEvent(navEx.getMessage(), "Error occurred during navigation execution ", LogLevelCategory.STEP_NAV_ERROR, execution.getSystemContext());
			} catch (RuntimeException eventEx) {
				logger.error("Failed to create event: ", eventEx);
			}
		}
	}

	private boolean useDefaultGroup(Execution execution) {
		Boolean useDefaultGroup = (Boolean) execution.getSystemContext().get(ExecutionConstants.USE_DEFAULT_GROUP);
		if (useDefaultGroup == null) {
			return false;
		}

		return useDefaultGroup;
	}

	protected void postExecutionSettings(Execution execution) {
		// Decide on Group
		String group = (String) execution.getSystemContext().get(ExecutionConstants.ACTUALLY_OPERATION_GROUP);
		if (StringUtils.isEmpty(group) || ExecutionConstants.DEFAULT_GROUP.equals(group)) {
			execution.setGroupName(null);
		} else {
			execution.setGroupName(group);
		}

		if (isDebuggerMode(execution.getSystemContext())) {
			if (!StringUtils.isEmpty(group) && useDefaultGroup(execution)) {
				execution.setGroupName(null);
			}
		}

		// Decide Whether should go to jms or perform an internal agent recursion
		Boolean mustGoToQueue = (Boolean) execution.getSystemContext().get(ExecutionConstants.MUST_GO_TO_QUEUE);
		mustGoToQueue = (mustGoToQueue == null) ? Boolean.FALSE : mustGoToQueue;

		// execution.mustGoToQueue is the value checked upon return
		execution.setMustGoToQueue(mustGoToQueue);

		// reset the flag in the context
		execution.getSystemContext().put(ExecutionConstants.MUST_GO_TO_QUEUE, Boolean.FALSE);
	}

	private void addContextData(Map<String, Object> data, Execution execution) {
		data.putAll(execution.getContexts());
		data.put(SYSTEM_CONTEXT, execution.getSystemContext());
		data.put(ExecutionConstants.SERIALIZABLE_SESSION_CONTEXT, execution.getSerializableSessionContext());
		data.put(EXECUTION, execution);
		data.put(ExecutionConstants.RUNNING_EXECUTION_PLAN_ID, execution.getRunningExecutionPlanId());
	}
}
