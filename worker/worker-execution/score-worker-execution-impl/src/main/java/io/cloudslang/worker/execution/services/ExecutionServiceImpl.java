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

package io.cloudslang.worker.execution.services;

import io.cloudslang.orchestrator.services.PauseResumeService;
import io.cloudslang.score.api.ControlActionMetadata;
import io.cloudslang.score.api.ExecutionPlan;
import io.cloudslang.score.api.ExecutionStep;
import io.cloudslang.score.api.StartBranchDataContainer;
import io.cloudslang.score.api.execution.ExecutionMetadataConsts;
import io.cloudslang.score.api.execution.ExecutionParametersConsts;
import io.cloudslang.score.events.EventBus;
import io.cloudslang.score.events.EventConstants;
import io.cloudslang.score.events.ScoreEvent;
import io.cloudslang.score.facade.TempConstants;
import io.cloudslang.score.facade.entities.Execution;
import io.cloudslang.score.facade.entities.RunningExecutionPlan;
import io.cloudslang.score.facade.execution.ExecutionStatus;
import io.cloudslang.score.facade.execution.ExecutionSummary;
import io.cloudslang.score.facade.execution.PauseReason;
import io.cloudslang.score.lang.SystemContext;
import io.cloudslang.worker.execution.model.SandboxExecutionRunnable;
import io.cloudslang.worker.execution.reflection.ReflectionAdapter;
import io.cloudslang.worker.management.WorkerConfigurationService;
import io.cloudslang.worker.management.services.dbsupport.WorkerDbSupportService;
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
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;

import static io.cloudslang.score.api.execution.ExecutionParametersConsts.ACTION_TYPE;
import static io.cloudslang.score.api.execution.ExecutionParametersConsts.SEQUENTIAL;
import static io.cloudslang.score.facade.TempConstants.EXECUTE_CONTENT_ACTION;
import static io.cloudslang.score.facade.TempConstants.EXECUTE_CONTENT_ACTION_CLASSNAME;
import static io.cloudslang.score.facade.TempConstants.SC_TIMEOUT_MINS;
import static io.cloudslang.score.facade.TempConstants.SC_TIMEOUT_START_TIME;
import static java.lang.Boolean.getBoolean;
import static java.lang.Integer.getInteger;
import static java.lang.Long.getLong;
import static java.lang.String.valueOf;


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

    @Autowired
    private RobotConnectionState robotConnectionState;

    private static final int DEFAULT_PLATFORM_LEVEL_OPERATION_TIMEOUT_IN_SECONDS = 24 * 60 * 60; // seconds in a day
    private static final int DEFAULT_PLATFORM_LEVEL_WAIT_PERIOD_FOR_TIMEOUT_IN_SECONDS = 5 * 60; // 5 minutes
    private static final long DEFAULT_PLATFORM_LEVEL_WAIT_PAUSE_FOR_TIMEOUT_IN_MILLIS = 200; // 200 milliseconds

    private final long operationTimeoutMillis;
    private final long waitPauseForTimeoutMillis;
    private final long waitPeriodForTimeoutMillis;
    private final boolean interruptOperationExecution;

    public ExecutionServiceImpl() {
        this.operationTimeoutMillis = getSafeIntProperty("execution.operationTimeoutInSeconds",
                DEFAULT_PLATFORM_LEVEL_OPERATION_TIMEOUT_IN_SECONDS) * 1000L;
        this.waitPeriodForTimeoutMillis = getSafeIntProperty("execution.waitPeriodForTimeoutInSeconds",
                DEFAULT_PLATFORM_LEVEL_WAIT_PERIOD_FOR_TIMEOUT_IN_SECONDS) * 1000L;
        this.waitPauseForTimeoutMillis = getSafeLongProperty("execution.waitPauseForTimeoutInMillis",
                DEFAULT_PLATFORM_LEVEL_WAIT_PAUSE_FOR_TIMEOUT_IN_MILLIS);
        this.interruptOperationExecution = getBoolean("execution.interruptOperation");
    }

    private int getSafeIntProperty(String property, int defaultValue) {
        int intVal = getInteger(property, defaultValue);
        return (intVal > 0) ? intVal : defaultValue;
    }

    private long getSafeLongProperty(String property, long defaultValue) {
        long longVal = getLong(property, defaultValue);
        return (longVal > 0) ? longVal : defaultValue;
    }

    @Override
    public Execution execute(Execution execution) throws InterruptedException {
        try {
            // handle flow cancellation
            if (handleCancelledFlow(execution)) {
                return execution;
            }
            ExecutionStep currStep = loadExecutionStep(execution);
            // Check if this execution was paused
            if (!isDebuggerMode(execution.getSystemContext()) && handlePausedFlow(execution)) {
                return null;
            }
            // dum bus event
            dumpBusEvents(execution);
            // Run the execution step
            String timeoutMessage = executeStep(execution, currStep);
            if (timeoutMessage != null) { // Timeout of run
                try {
                    return doWaitForCancel(execution);
                } catch (TimeoutException timeout) {
                    logger.error("Timed out waiting for cancel for execution id " + execution.getExecutionId());
                    execution.getSystemContext().setStepErrorKey(timeoutMessage);
                }
            }
            if ((!execution.getSystemContext().hasStepErrorKey()) && currStep.getActionData().get(ACTION_TYPE) != null &&
                    currStep.getActionData().get(ACTION_TYPE).toString().equalsIgnoreCase(SEQUENTIAL)) {
                if (!robotConnectionState.hasRunningRobot("Default")) {
                    pauseFlow(PauseReason.ROBOT_NOT_AVAILABLE, execution);
                } else {
                    pauseFlow(PauseReason.SEQUENTIAL_EXECUTION, execution);
                }
                return null;
            }
            // Run the navigation
            navigate(execution, currStep);
            // currently handles groups and jms optimizations
            postExecutionSettings(execution);
            // If execution was paused in language - to avoid delay of configuration
            if (execution.getSystemContext().isPaused()) {
                if (handlePausedFlowAfterStep(execution)) {
                    return null;
                }
            }
            // dum bus event
            dumpBusEvents(execution);
            if (logger.isDebugEnabled()) {
                logger.debug("End of step: " + execution.getPosition() + " in execution id: " + execution.getExecutionId());
            }
            return execution;
        } catch (InterruptedException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Error during execution: ", ex);
            execution.getSystemContext().setStepErrorKey(ex.getMessage()); // this is done only fo reporting
            execution.getSystemContext().setFlowTerminationType(ExecutionStatus.SYSTEM_FAILURE);
            execution.setPosition(null); // this ends the flow!!!
            return execution;
        }
    }

    private Execution doWaitForCancel(Execution execution) throws InterruptedException, TimeoutException {
        int iterations = ((int) (waitPeriodForTimeoutMillis / waitPauseForTimeoutMillis)) + 1; // at least one iteration
        for (int i = 0; i < iterations; i++) {
            if (handleCancelledFlow(execution)) {
                return execution;
            }
            Thread.sleep(waitPauseForTimeoutMillis);
        }
        throw new TimeoutException();
    }

    @Override
    // returns null in case the split was not done - flow is paused or cancelled
    public List<Execution> executeSplit(Execution execution) throws InterruptedException {
        try {
            ExecutionStep currStep = loadExecutionStep(execution);
            // Check if this execution was paused
            if (!isDebuggerMode(execution.getSystemContext()) && handlePausedFlow(execution)) {
                return null;
            }
            // dum bus event
            dumpBusEvents(execution);
            executeSplitStep(execution, currStep);
            failFlowIfSplitStepFailed(execution);

            dumpBusEvents(execution);

            // Run the split step
            List<StartBranchDataContainer> newBranches = execution.getSystemContext().removeBranchesData();
            List<Execution> newExecutions = createChildExecutions(execution.getExecutionId(), newBranches);
            // Run the navigation
            navigate(execution, currStep);

            dumpBusEvents(execution);

            if (logger.isDebugEnabled()) {
                logger.debug(
                        "End of step: " + execution.getPosition() + " in execution id: " + execution.getExecutionId());
            }
            return newExecutions;
        } catch (Exception ex) {
            logger.error("Exception during the split step!", ex);
            throw ex;
        }
    }

    private void failFlowIfSplitStepFailed(Execution execution) throws InterruptedException {
        if (execution.getSystemContext().hasStepErrorKey()) {
            String exception = execution.getSystemContext().getStepErrorKey();
            execution.getSystemContext().setFlowTerminationType(ExecutionStatus.SYSTEM_FAILURE);
            execution.setPosition(null); // this ends the flow!!!
            try {
                createErrorEvent(exception, "Error occurred during split step ", EventConstants.SCORE_STEP_SPLIT_ERROR,
                        execution.getSystemContext());
            } catch (RuntimeException eventEx) {
                logger.error("Failed to create event: ", eventEx);
            }
            throw new RuntimeException(exception);
        }
    }

    private static List<Execution> createChildExecutions(Long executionId, List<StartBranchDataContainer> newBranches) {
        List<Execution> newExecutions = new ArrayList<>();
        String splitId = UUID.randomUUID().toString();
        for (int i = 0; i < newBranches.size(); i++) {
            StartBranchDataContainer from = newBranches.get(i);
            Execution to = new Execution(executionId, from.getExecutionPlanId(), from.getStartPosition(),
                    from.getContexts(), from.getSystemContext());

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
        boolean executionIsCancelled = workerConfigurationService.isExecutionCancelled(execution
                .getExecutionId()); // in this case - just check if need to cancel. It will set as cancelled later on QueueEventListener
        // Another scenario of getting canceled - it was cancelled from the SplitJoinService (the configuration can still be not updated). Defect #:22060
        if (ExecutionStatus.CANCELED.equals(execution.getSystemContext().getFlowTerminationType())) {
            executionIsCancelled = true;
        }
        if (executionIsCancelled) {
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
        if (reason != null) { // need to pause the execution
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
        if (execSummary != null && execSummary.getStatus().equals(ExecutionStatus.PENDING_PAUSE)) {
            reason = execSummary.getPauseReason();
        }
        if (reason != null) { // need to pause the execution
            pauseFlow(reason, execution);
            return true;
        }
        return false;
    }

    private void pauseIfSequentialExecutions (Long executionId, String branchId,PauseReason reason) {
        if (reason.equals(PauseReason.SEQUENTIAL_EXECUTION) || reason.equals(PauseReason.ROBOT_NOT_AVAILABLE)) {
            pauseService.pauseExecution(executionId, branchId, reason);
        }
    }

    public void pauseFlow(PauseReason reason, Execution execution) throws InterruptedException {
        SystemContext systemContext = execution.getSystemContext();
        Long executionId = execution.getExecutionId();
        String branchId = systemContext.getBranchId();
        // If USER_PAUSED send such event
        if (!isDebuggerMode(execution.getSystemContext()) && reason.equals(PauseReason.USER_PAUSED)) {
            if (branchId != null) {
                // we pause the branch because the Parent was user-paused (see findPauseReason)
                pauseService.pauseExecution(executionId, branchId, reason); // this creates a DB record for this branch, as Pending-paused
            }
        } else {
            pauseIfSequentialExecutions(executionId, branchId, reason);
        }
        addPauseEvent(systemContext);
        // dump bus events here because out side is too late
        dumpBusEvents(execution);
        // Write execution to the db! Pay attention - do not do anything to the execution or its context after this line!!!
        pauseService.writeExecutionObject(executionId, branchId, execution);
        if (logger.isDebugEnabled()) {
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
        if (workerConfigurationService.isExecutionPaused(executionId, branchId)) {
            ExecutionSummary execSummary = pauseService.readPausedExecution(executionId, branchId);
            if (execSummary != null && execSummary.getStatus().equals(ExecutionStatus.PENDING_PAUSE)) {
                return execSummary.getPauseReason();
            }
            // 2. Check the parent if we're in branch (subflow or MI\Parallel lane).
            // If the user pressed Pause on the Parent then we need to pause the branch (the parent is in the Suspended table).
        } else if (branchId != null && workerConfigurationService.isExecutionPaused(executionId, null)) {
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

    private static boolean isDebuggerMode(Map<String, Serializable> systemContext) {
        Boolean isDebuggerMode = (Boolean) systemContext.get(TempConstants.DEBUGGER_MODE);
        if (isDebuggerMode == null) {
            return false;
        }
        return isDebuggerMode;
    }

    public void dumpBusEvents(Execution execution) throws InterruptedException {
        ArrayDeque<ScoreEvent> eventsQueue = execution.getSystemContext().getEvents();
        if (eventsQueue == null) {
            return;
        }
        for (ScoreEvent eventWrapper : eventsQueue) {
            eventBus.dispatch(eventWrapper);
        }
        eventsQueue.clear();
    }

    public ExecutionStep loadExecutionStep(Execution execution) {
        RunningExecutionPlan runningExecutionPlan;
        if (execution != null) {
            // Optimization for external workers - run the content only without loading the execution plan
            if (execution.getSystemContext().get(TempConstants.CONTENT_EXECUTION_STEP) != null) {
                return (ExecutionStep) execution.getSystemContext().get(TempConstants.CONTENT_EXECUTION_STEP);
            }
            Long position = execution.getPosition();
            if (position != null) {
                runningExecutionPlan = workerDbSupportService
                        .readExecutionPlanById(execution.getRunningExecutionPlanId());
                if (runningExecutionPlan != null) {
                    updateMetadata(execution, runningExecutionPlan);
                    ExecutionStep currStep = runningExecutionPlan.getExecutionPlan().getStep(position);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Begin step: " + position + " in flow " + runningExecutionPlan.getExecutionPlan()
                                .getFlowUuid() + " [" + execution.getExecutionId() + "]");
                    }
                    if (currStep != null) {
                        return currStep;
                    }
                }
            }
        }
        // If we got here - one of the objects was null
        throw new RuntimeException("Failed to load ExecutionStep!");
    }

    private void updateMetadata(Execution execution, RunningExecutionPlan runningExecutionPlan) {
        Map<String, Serializable> executionMetadata = (Map<String, Serializable>) execution.getSystemContext()
                .getMetaData();
        ExecutionPlan executionPlan = runningExecutionPlan.getExecutionPlan();
        executionMetadata.put(ExecutionMetadataConsts.EXECUTION_PLAN_ID, executionPlan.getFlowUuid());
        executionMetadata.put(ExecutionMetadataConsts.EXECUTION_PLAN_NAME, executionPlan.getName());
    }

    protected String executeStep(Execution execution, ExecutionStep currStep) throws InterruptedException {
        try {
            final Map<String, Object> stepData = prepareStepData(execution, currStep);
            final ControlActionMetadata action = currStep.getAction();

            if (isContentOperationStep(action)) {
                Long startTime = (Long) execution.getSystemContext().get(SC_TIMEOUT_START_TIME);
                Integer timeoutMins = (Integer) execution.getSystemContext().get(SC_TIMEOUT_MINS);

                if ((startTime != null) && (timeoutMins != null)) { // Timeout information is available, we use it
                    long now = System.currentTimeMillis();
                    Callable<Object> operationCallable = () -> reflectionAdapter.executeControlAction(action, stepData);
                    Thread operationExecutionThread = new Thread(
                            new SandboxExecutionRunnable<>(Thread.currentThread().getContextClassLoader(),
                                    operationCallable));

                    long dynamicTimeout = getDynamicTimeout(startTime, timeoutMins, now);
                    if (dynamicTimeout == -1L) { // execution time exceeded for this execution
                        String timeoutErrorMessageBeforeStep = String
                                .format("Timeout (%d minutes) exceeded for execution id %s having start time %s (current time %s) before executing step %s",
                                        timeoutMins, valueOf(execution.getExecutionId()), valueOf(startTime),
                                        valueOf(now), valueOf(currStep.getExecStepId()));
                        logger.error(timeoutErrorMessageBeforeStep);
                        return timeoutErrorMessageBeforeStep;
                    }

                    operationExecutionThread.setName("operationExecutionThread-" + execution.getExecutionId() + "-" + currStep.getExecStepId());
                    operationExecutionThread.start();
                    operationExecutionThread.join(dynamicTimeout);

                    if (operationExecutionThread.isAlive()) {
                        String timeoutErrorMessageDuringStep = String
                                .format("Timeout (%d minutes) exceeded for execution id %s having start time %s (current time %s) when running step %s",
                                        timeoutMins, valueOf(execution.getExecutionId()), valueOf(startTime),
                                        valueOf(System.currentTimeMillis()), valueOf(currStep.getExecStepId()));
                        logger.error(timeoutErrorMessageDuringStep);

                        if (interruptOperationExecution) {
                            operationExecutionThread
                                    .interrupt(); // interrupt the execution of the content operation
                        }
                        return timeoutErrorMessageDuringStep;
                    }
                } else { // Execute on regular executor as usual if no timeout is present
                    reflectionAdapter.executeControlAction(action, stepData);
                }
            } else { // Execute on regular executor as this is not an operation
                reflectionAdapter.executeControlAction(action, stepData);
            }
        } catch (RuntimeException ex) {
            handleStepExecutionException(execution, ex);
        }

        return null;
    }

    private boolean isContentOperationStep(ControlActionMetadata action) {
        return StringUtils.equals(action.getMethodName(), EXECUTE_CONTENT_ACTION) && StringUtils.endsWith(action.getClassName(), EXECUTE_CONTENT_ACTION_CLASSNAME);
    }

    private long getDynamicTimeout(long startTime, int timeoutMins, long now) {
        if (timeoutMins > 0) { // we have a defined timeout
            if (now > startTime) {
                long elapsedTime = now - startTime;
                long minsInMillis = timeoutMins * 60 * 1000L;
                long diffInMillis = minsInMillis - elapsedTime;
                return (diffInMillis > 0) ? diffInMillis : -1L;
            }
        }

        // Default operation timeout is provided in all other cases
        return operationTimeoutMillis;
    }

    protected void executeSplitStep(Execution execution, ExecutionStep currStep) {
        try {
            Map<String, Object> stepData = prepareStepData(execution, currStep);
            reflectionAdapter.executeControlAction(currStep.getAction(), stepData);
        } catch (RuntimeException ex) {
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
        if (actionData != null) {
            stepData.putAll(actionData);
        }
        // We add all the contexts to the step data - so inside of each control action we will have access to all contexts
        addContextData(stepData, execution);
        return stepData;
    }

    private void createErrorEvent(String ex, String logMessage, String errorType, SystemContext systemContext)
            throws InterruptedException {
        HashMap<String, Serializable> eventData = new HashMap<>();
        eventData.put(ExecutionParametersConsts.SYSTEM_CONTEXT, new HashMap<>(systemContext));
        eventData.put(EventConstants.SCORE_ERROR_MSG, ex);
        eventData.put(EventConstants.SCORE_ERROR_LOG_MSG, logMessage);
        eventData.put(EventConstants.SCORE_ERROR_TYPE, errorType);
        ScoreEvent eventWrapper = new ScoreEvent(EventConstants.SCORE_ERROR_EVENT, eventData);
        eventBus.dispatch(eventWrapper);
    }

    public void navigate(Execution execution, ExecutionStep currStep) throws InterruptedException {
        Long position;
        try {
            if (currStep.getNavigation() != null) {
                Map<String, Object> navigationData = new HashMap<>(currStep.getNavigationData());
                // We add all the contexts to the step data - so inside of each control action we will have access to all contexts
                addContextData(navigationData, execution);
                position = (Long) reflectionAdapter.executeControlAction(currStep.getNavigation(), navigationData);
                execution.setPosition(position);
            } else {
                execution.setPosition(null); // terminate the flow - we got to the last step!
            }
        } catch (RuntimeException navEx) {
            // If Exception occurs in navigation (almost impossible since now we always have Flow Exception Step) we can not continue since we don't know which step is the next step...
            // terminating...
            logger.error("Error occurred during navigation execution. Execution id: " + execution.getExecutionId(),
                    navEx);
            execution.getSystemContext().setStepErrorKey(navEx.getMessage()); // this is done only for reporting
            execution.getSystemContext().setFlowTerminationType(ExecutionStatus.SYSTEM_FAILURE);
            execution.setPosition(null); // this ends the flow!!!
            try {
                createErrorEvent(navEx.getMessage(), "Error occurred during navigation execution ",
                        EventConstants.SCORE_STEP_NAV_ERROR, execution.getSystemContext());
            } catch (RuntimeException eventEx) {
                logger.error("Failed to create event: ", eventEx);
            }
        }
    }

    private static boolean useDefaultGroup(Execution execution) {
        Boolean useDefaultGroup = (Boolean) execution.getSystemContext().get(TempConstants.USE_DEFAULT_GROUP);
        if (useDefaultGroup == null) {
            return false;
        }
        return useDefaultGroup;
    }

    public void postExecutionSettings(Execution execution) {
        setWorkerGroup(execution);

        //if there is a request to change the running execution plan id, we update the execution to the new execution plan ID
        Long requestForChangingExecutionPlan = execution.getSystemContext().pullRequestForChangingExecutionPlan();
        if (requestForChangingExecutionPlan != null) {
            execution.setRunningExecutionPlanId(requestForChangingExecutionPlan);
        }
    }

    private void setWorkerGroup(Execution execution) {
        //get group from system context
        String group = (String) execution.getSystemContext().get(TempConstants.ACTUALLY_OPERATION_GROUP);

        //if not overridden get the group from the step
        if (group == null) {
            ExecutionStep nextStep = getNextStep(execution);
            if (nextStep != null && nextStep.getActionData().get("workerGroup") != null) {
                group = nextStep.getActionData().get("workerGroup").toString();
                execution.getSystemContext().put(TempConstants.SHOULD_CHECK_GROUP, true);
            }
        }
        execution.setGroupName(group);

        if (isDebuggerMode(execution.getSystemContext())) {
            if (!StringUtils.isEmpty(group) && useDefaultGroup(execution)) {
                execution.setGroupName(null);
            }
        }
    }

    private ExecutionStep getNextStep(Execution execution) {
        ExecutionStep nextStep = null;
        Long position = execution.getPosition();
        if (position != null) {
            RunningExecutionPlan runningExecutionPlan = workerDbSupportService
                    .readExecutionPlanById(execution.getRunningExecutionPlanId());
            if (runningExecutionPlan != null) {
                nextStep = runningExecutionPlan.getExecutionPlan().getStep(position);
            }
        }
        return nextStep;
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
