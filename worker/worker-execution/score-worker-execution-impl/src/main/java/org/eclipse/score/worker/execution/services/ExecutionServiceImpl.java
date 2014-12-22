/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package org.eclipse.score.worker.execution.services;

import org.eclipse.score.api.ExecutionStep;
import org.eclipse.score.api.StartBranchDataContainer;
import org.eclipse.score.api.execution.ExecutionParametersConsts;
import org.eclipse.score.events.EventBus;
import org.eclipse.score.events.EventConstants;
import org.eclipse.score.events.ScoreEvent;
import org.openscore.facade.TempConstants;
import org.openscore.facade.entities.Execution;
import org.openscore.facade.entities.RunningExecutionPlan;
import org.eclipse.score.facade.execution.ExecutionStatus;
import org.openscore.facade.execution.ExecutionSummary;
import org.openscore.facade.execution.PauseReason;
import org.eclipse.score.lang.SystemContext;
import org.openscore.orchestrator.services.PauseResumeService;
import org.eclipse.score.worker.execution.reflection.ReflectionAdapter;
import org.eclipse.score.worker.management.WorkerConfigurationService;
import org.openscore.worker.management.services.dbsupport.WorkerDbSupportService;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author
 * @author Avi Moradi
 * @since 08/01/2011
 * @version $Id$
 */
public final class ExecutionServiceImpl implements ExecutionService {

	private static final Logger logger = Logger.getLogger(ExecutionServiceImpl.class);

	@Autowired
	private PauseResumeService pauseService;
	@Autowired
	private ReflectionAdapter reflectionAdapter;
	@Autowired
	private WorkerDbSupportService workerDbSupportService;
	@Autowired
	private WorkerConfigurationService workerConfigurationService;
	@Autowired
	private EventBus eventBus;

	@Override
	public Execution execute(Execution execution) throws InterruptedException {
		try {
			// handle flow cancellation
			if(handleCancelledFlow(execution)) {
				return execution;
			}
			ExecutionStep currStep = loadExecutionStep(execution);
			// Check if this execution was paused
			if(!isDebuggerMode(execution.getSystemContext()) && handlePausedFlow(execution)) {
				return null;
			}
			// dum bus event
			dumpBusEvents(execution);
			// Run the execution step
			executeStep(execution, currStep);
			// Run the navigation
			navigate(execution, currStep);
			// currently handles groups and jms optimizations
			postExecutionSettings(execution);
			// If execution was paused in language - to avoid delay of configuration
			if(execution.getSystemContext().isPaused()) {
				if(handlePausedFlowAfterStep(execution)) {
					return null;
				}
			}
			// dum bus event
			dumpBusEvents(execution);
			if(logger.isDebugEnabled()) {
				logger.debug("End of step: " + execution.getPosition() + " in execution id: " + execution.getExecutionId());
			}
			return execution;
		}
        catch(InterruptedException ex) {
            throw ex;
        }
        catch(Exception ex) {
            logger.error("Error during execution: ", ex);
            execution.getSystemContext().setStepErrorKey(ex.getMessage()); // this is done only fo reporting
            execution.getSystemContext().setFlowTerminationType(ExecutionStatus.SYSTEM_FAILURE);
            execution.setPosition(null); // this ends the flow!!!
            return execution;
        }
    }

	@Override
	// returns null in case the split was not done - flow is paused or cancelled
	public List<Execution> executeSplit(Execution execution) throws InterruptedException {
		try {
			ExecutionStep currStep = loadExecutionStep(execution);
			// Check if this execution was paused
			if(!isDebuggerMode(execution.getSystemContext()) && handlePausedFlow(execution)) {
				return null;
			}
			// dum bus event
			dumpBusEvents(execution);
			executeStep(execution, currStep);
			failFlowIfSplitStepFailed(execution);

            dumpBusEvents(execution);

            // Run the split step
			List<StartBranchDataContainer> newBranches = execution.getSystemContext().removeBranchesData();
			List<Execution> newExecutions = createChildExecutions(execution.getExecutionId(), newBranches);
			// Run the navigation
			navigate(execution, currStep);

            dumpBusEvents(execution);

            if(logger.isDebugEnabled()) {
				logger.debug("End of step: " + execution.getPosition() + " in execution id: " + execution.getExecutionId());
			}
			return newExecutions;
		} catch(Exception ex) {
			logger.error("Exception during the split step!", ex);
			throw ex;
		}
	}

	private void failFlowIfSplitStepFailed(Execution execution) throws InterruptedException {
		if(execution.getSystemContext().hasStepErrorKey()) {
			String exception = execution.getSystemContext().getStepErrorKey();
			execution.getSystemContext().setFlowTerminationType(ExecutionStatus.SYSTEM_FAILURE);
			execution.setPosition(null); // this ends the flow!!!
			try {
				createErrorEvent(exception, "Error occurred during split step ", EventConstants.SCORE_STEP_SPLIT_ERROR, execution.getSystemContext());
			} catch(RuntimeException eventEx) {
				logger.error("Failed to create event: ", eventEx);
			}
			throw new RuntimeException(exception);
		}
	}

	private static List<Execution> createChildExecutions(Long executionId, List<StartBranchDataContainer> newBranches) {
		List<Execution> newExecutions = new ArrayList<>();
		String splitId = UUID.randomUUID().toString();
		for(int i = 0; i < newBranches.size(); i++) {
			StartBranchDataContainer from = newBranches.get(i);
			Execution to = new Execution(executionId, from.getExecutionPlanId(), from.getStartPosition(), from.getContexts(), from.getSystemContext());

			to.getSystemContext().setSplitId(splitId);
			to.getSystemContext().setBranchId(splitId + ":" + (i + 1));
			newExecutions.add(to);
		}
		return newExecutions;
	}

	@Override
	public boolean isSplitStep(Execution execution) {
		ExecutionStep currStep = loadExecutionStep(execution);
		return currStep.isSplitStep();
	}

	protected boolean handleCancelledFlow(Execution execution) {
		boolean executionIsCancelled = workerConfigurationService.isExecutionCancelled(execution.getExecutionId()); // in this case - just check if need to cancel. It will set as cancelled later on QueueEventListener
		// Another scenario of getting canceled - it was cancelled from the SplitJoinService (the configuration can still be not updated). Defect #:22060
		if(ExecutionStatus.CANCELED.equals(execution.getSystemContext().getFlowTerminationType())) {
			executionIsCancelled = true;
		}
		if(executionIsCancelled) {
			// NOTE: an execution can be cancelled directly from CancelExecutionService, if it's currently paused.
			// Thus, if you change the code here, please check CancelExecutionService as well.
			execution.getSystemContext().setFlowTerminationType(ExecutionStatus.CANCELED);
			execution.setPosition(null);
			return true;
		}
		return false;
	}

	// check if the execution should be Paused, and pause it if needed
	protected boolean handlePausedFlow(Execution execution) throws InterruptedException {
		String branchId = execution.getSystemContext().getBranchId();
		PauseReason reason = findPauseReason(execution.getExecutionId(), branchId);
		if(reason != null) { // need to pause the execution
			pauseFlow(reason, execution);
			return true;
		}
		return false;
	}

	// no need to check if paused - because this is called after the step, when the Pause flag exists in the context
	private boolean handlePausedFlowAfterStep(Execution execution) throws InterruptedException {
		String branchId = execution.getSystemContext().getBranchId();
		PauseReason reason = null;
		ExecutionSummary execSummary = pauseService.readPausedExecution(execution.getExecutionId(), branchId);
		if(execSummary != null && execSummary.getStatus().equals(ExecutionStatus.PENDING_PAUSE)) {
			reason = execSummary.getPauseReason();
		}
		if(reason != null) { // need to pause the execution
			pauseFlow(reason, execution);
			return true;
		}
		return false;
	}

	private void pauseFlow(PauseReason reason, Execution execution) throws InterruptedException {
		SystemContext systemContext = execution.getSystemContext();
		Long executionId = execution.getExecutionId();
		String branchId = systemContext.getBranchId();
		// If USER_PAUSED send such event
		if(!isDebuggerMode(execution.getSystemContext()) && reason.equals(PauseReason.USER_PAUSED)) {
			if(branchId != null) {
				// we pause the branch because the Parent was user-paused (see findPauseReason)
				pauseService.pauseExecution(executionId, branchId, reason); // this creates a DB record for this branch, as Pending-paused
			}
		}
		addPauseEvent(systemContext);
		// dump bus events here because out side is too late
		dumpBusEvents(execution);
		// Write execution to the db! Pay attention - do not do anything to the execution or its context after this line!!!
		pauseService.writeExecutionObject(executionId, branchId, execution);
		if(logger.isDebugEnabled()) {
			logger.debug("Execution with execution_id: " + execution.getExecutionId() + " is paused!");
		}
	}

	private void addPauseEvent(SystemContext systemContext) throws InterruptedException {
		HashMap<String, Serializable> eventData = new HashMap<>();
		eventData.put(ExecutionParametersConsts.SYSTEM_CONTEXT, new HashMap<>(systemContext));
		ScoreEvent eventWrapper = new ScoreEvent(EventConstants.SCORE_PAUSED_EVENT, eventData);
		eventBus.dispatch(eventWrapper);
	}

	private PauseReason findPauseReason(Long executionId, String branchId) {
		// 1. Check the configuration according to branch (can be null or not null...)
		if(workerConfigurationService.isExecutionPaused(executionId, branchId)) {
			ExecutionSummary execSummary = pauseService.readPausedExecution(executionId, branchId);
			if(execSummary != null && execSummary.getStatus().equals(ExecutionStatus.PENDING_PAUSE)) {
				return execSummary.getPauseReason();
			}
			// 2. Check the parent if we're in branch (subflow or MI\Parallel lane).
			// If the user pressed Pause on the Parent then we need to pause the branch (the parent is in the Suspended table).
		} else if(branchId != null && workerConfigurationService.isExecutionPaused(executionId, null)) {
			ExecutionSummary execSummary = pauseService.readPausedExecution(executionId, null);
			if(execSummary != null && execSummary.getStatus().equals(ExecutionStatus.PENDING_PAUSE)) {
				PauseReason reason = execSummary.getPauseReason();
				// we only care about User-Paused here!
				// we don't want to Pause if the parent is paused due to branch_paused! (other branch is paused for some reason (e.g. required_input), so the parent is paused as well).
				if(PauseReason.USER_PAUSED.equals(reason)) {
					return reason;
				}
			}
		}
		return null; // not paused
	}

	private static boolean isDebuggerMode(Map<String, Serializable> systemContext) {
		Boolean isDebuggerMode = (Boolean)systemContext.get(TempConstants.DEBUGGER_MODE);
		if(isDebuggerMode == null) {
			return false;
		}
		return isDebuggerMode;
	}

	private void dumpBusEvents(Execution execution) throws InterruptedException {
		ArrayDeque<ScoreEvent> eventsQueue = execution.getSystemContext().getEvents();
		if(eventsQueue == null) {
			return;
		}
		for(ScoreEvent eventWrapper : eventsQueue) {
			eventBus.dispatch(eventWrapper);
		}
		eventsQueue.clear();
	}

	protected ExecutionStep loadExecutionStep(Execution execution) {
		RunningExecutionPlan runningExecutionPlan;
		if(execution != null) {
			// Optimization for external workers - run the content only without loading the execution plan
			if(execution.getSystemContext().get(TempConstants.CONTENT_EXECUTION_STEP) != null) {
				return (ExecutionStep)execution.getSystemContext().get(TempConstants.CONTENT_EXECUTION_STEP);
			}
			Long position = execution.getPosition();
			if(position != null) {
				runningExecutionPlan = workerDbSupportService.readExecutionPlanById(execution.getRunningExecutionPlanId());
				if(runningExecutionPlan != null) {
					ExecutionStep currStep = runningExecutionPlan.getExecutionPlan().getStep(position);
					if(logger.isDebugEnabled()) {
						logger.debug("Begin step: " + position + " in flow " + runningExecutionPlan.getExecutionPlan().getFlowUuid() + " [" + execution.getExecutionId() + "]");
					}
					if(currStep != null) {
						return currStep;
					}
				}
			}
		}
		// If we got here - one of the objects was null
		throw new RuntimeException("Failed to load ExecutionStep!");
	}

	protected void executeStep(Execution execution, ExecutionStep currStep) {
		try {
			Map<String, Object> stepData = prepareStepData(execution, currStep);
			reflectionAdapter.executeControlAction(currStep.getAction(), stepData);
		} catch(RuntimeException ex) {
			handleStepExecutionException(execution, ex);
		}
	}

	private static void handleStepExecutionException(Execution execution, RuntimeException ex) {
		logger.error("Error occurred during operation execution.  Execution id: " + execution.getExecutionId(), ex);
		execution.getSystemContext().setStepErrorKey(ex.getMessage());
	}

	private Map<String, Object> prepareStepData(Execution execution, ExecutionStep currStep) {
        Map<String, ?> actionData = currStep.getActionData();
        Map<String, Object> stepData = new HashMap<>();
        if (actionData != null){
            stepData.putAll(actionData);
        }
		// We add all the contexts to the step data - so inside of each control action we will have access to all contexts
		addContextData(stepData, execution);
		return stepData;
	}

	private void createErrorEvent(String ex, String logMessage, String errorType, SystemContext systemContext) throws InterruptedException {
		HashMap<String, Serializable> eventData = new HashMap<>();
		eventData.put(ExecutionParametersConsts.SYSTEM_CONTEXT, new HashMap<>(systemContext));
		eventData.put(EventConstants.SCORE_ERROR_MSG, ex);
		eventData.put(EventConstants.SCORE_ERROR_LOG_MSG, logMessage);
		eventData.put(EventConstants.SCORE_ERROR_TYPE, errorType);
		ScoreEvent eventWrapper = new ScoreEvent(EventConstants.SCORE_ERROR_EVENT, eventData);
		eventBus.dispatch(eventWrapper);
	}

	protected void navigate(Execution execution, ExecutionStep currStep) throws InterruptedException {
		Long position;
		try {
			if(currStep.getNavigation() != null) {
				Map<String, Object> navigationData = new HashMap<>(currStep.getNavigationData());
				// We add all the contexts to the step data - so inside of each control action we will have access to all contexts
				addContextData(navigationData, execution);
				position = (Long)reflectionAdapter.executeControlAction(currStep.getNavigation(), navigationData);
				execution.setPosition(position);
			} else {
				execution.setPosition(null); // terminate the flow - we got to the last step!
			}
		} catch(RuntimeException navEx) {
			// If Exception occurs in navigation (almost impossible since now we always have Flow Exception Step) we can not continue since we don't know which step is the next step...
			// terminating...
			logger.error("Error occurred during navigation execution. Execution id: " + execution.getExecutionId(), navEx);
			execution.getSystemContext().setStepErrorKey(navEx.getMessage()); // this is done only fo reporting
			execution.getSystemContext().setFlowTerminationType(ExecutionStatus.SYSTEM_FAILURE);
			execution.setPosition(null); // this ends the flow!!!
			try {
				createErrorEvent(navEx.getMessage(), "Error occurred during navigation execution ", EventConstants.SCORE_STEP_NAV_ERROR, execution.getSystemContext());
			} catch(RuntimeException eventEx) {
				logger.error("Failed to create event: ", eventEx);
			}
		}
	}

	private static boolean useDefaultGroup(Execution execution) {
		Boolean useDefaultGroup = (Boolean)execution.getSystemContext().get(TempConstants.USE_DEFAULT_GROUP);
		if(useDefaultGroup == null) {
			return false;
		}
		return useDefaultGroup;
	}

	protected static void postExecutionSettings(Execution execution) {
		// Decide on Group
		String group = (String)execution.getSystemContext().get(TempConstants.ACTUALLY_OPERATION_GROUP);

		execution.setGroupName(group);

		if(isDebuggerMode(execution.getSystemContext())) {
			if(!StringUtils.isEmpty(group) && useDefaultGroup(execution)) {
				execution.setGroupName(null);
			}
		}
        //if there is a request to change the running execution plan id, we update the execution to the new execution plan ID
        Long requestForChangingExecutionPlan = execution.getSystemContext().pullRequestForChangingExecutionPlan();
        if (requestForChangingExecutionPlan != null) {
            execution.setRunningExecutionPlanId(requestForChangingExecutionPlan);
        }
	}

	private static void addContextData(Map<String, Object> data, Execution execution) {
		data.putAll(execution.getContexts());
		data.put(ExecutionParametersConsts.SYSTEM_CONTEXT, execution.getSystemContext());
		data.put(ExecutionParametersConsts.EXECUTION_RUNTIME_SERVICES, execution.getSystemContext());
		data.put(ExecutionParametersConsts.EXECUTION, execution);
		data.put(ExecutionParametersConsts.EXECUTION_CONTEXT, execution.getContexts());
		data.put(ExecutionParametersConsts.RUNNING_EXECUTION_PLAN_ID, execution.getRunningExecutionPlanId());
	}

}
