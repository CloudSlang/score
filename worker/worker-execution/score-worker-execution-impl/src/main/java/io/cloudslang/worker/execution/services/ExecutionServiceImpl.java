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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.cloudslang.orchestrator.services.AplsLicensingService;
import io.cloudslang.orchestrator.services.PauseResumeService;
import io.cloudslang.score.api.ControlActionMetadata;
import io.cloudslang.score.api.ExecutionPlan;
import io.cloudslang.score.api.ExecutionStep;
import io.cloudslang.score.api.StartBranchDataContainer;
import io.cloudslang.score.api.execution.ExecutionMetadataConsts;
import io.cloudslang.score.api.execution.ExecutionParametersConsts;
import io.cloudslang.score.events.EventBus;
import io.cloudslang.score.events.EventConstants;
import io.cloudslang.score.events.FastEventBus;
import io.cloudslang.score.events.ScoreEvent;
import io.cloudslang.score.facade.TempConstants;
import io.cloudslang.score.facade.entities.Execution;
import io.cloudslang.score.facade.entities.RunningExecutionPlan;
import io.cloudslang.score.facade.execution.ExecutionStatus;
import io.cloudslang.score.facade.execution.ExecutionSummary;
import io.cloudslang.score.facade.execution.PauseReason;
import io.cloudslang.score.lang.SystemContext;
import io.cloudslang.worker.execution.model.SandboxExecutionRunnable;
import io.cloudslang.worker.execution.model.StepActionDataHolder;
import io.cloudslang.worker.execution.model.StepActionDataHolder.ReadonlyStepActionDataAccessor;
import io.cloudslang.worker.execution.reflection.ReflectionAdapter;
import io.cloudslang.worker.management.WorkerConfigurationService;
import io.cloudslang.worker.management.services.dbsupport.WorkerDbSupportService;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeoutException;

import static io.cloudslang.orchestrator.services.AplsLicensingService.BRANCH_ID_TO_CHECK_IN_LICENSE;
import static io.cloudslang.orchestrator.services.AplsLicensingService.BRANCH_ID_TO_CHECK_OUT_LICENSE;
import static io.cloudslang.score.api.execution.ExecutionParametersConsts.ACTION_TYPE;
import static io.cloudslang.score.api.execution.ExecutionParametersConsts.SEQUENTIAL;
import static io.cloudslang.score.events.EventConstants.BRANCH_ID;
import static io.cloudslang.score.events.EventConstants.EXECUTION_ID;
import static io.cloudslang.score.events.EventConstants.SCORE_STEP_SPLIT_ERROR;
import static io.cloudslang.score.events.EventConstants.SPLIT_ID;
import static io.cloudslang.score.events.EventConstants.STEP_PATH;
import static io.cloudslang.score.facade.TempConstants.EXECUTE_CONTENT_ACTION;
import static io.cloudslang.score.facade.TempConstants.EXECUTE_CONTENT_ACTION_CLASSNAME;
import static io.cloudslang.score.facade.TempConstants.MI_REMAINING_BRANCHES_CONTEXT_KEY;
import static io.cloudslang.score.facade.TempConstants.SC_TIMEOUT_MINS;
import static io.cloudslang.score.facade.TempConstants.SC_TIMEOUT_START_TIME;
import static io.cloudslang.score.facade.execution.PauseReason.NO_ROBOTS_IN_GROUP;
import static io.cloudslang.score.facade.execution.PauseReason.PENDING_ROBOT;
import static io.cloudslang.score.lang.ExecutionRuntimeServices.LICENSE_TYPE;
import static java.lang.Boolean.getBoolean;
import static java.lang.Integer.getInteger;
import static java.lang.Long.MAX_VALUE;
import static java.lang.Long.getLong;
import static java.lang.String.valueOf;
import static java.lang.Thread.currentThread;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.commons.lang.StringUtils.endsWith;


public final class ExecutionServiceImpl implements ExecutionService {

    private static final Logger logger = LogManager.getLogger(ExecutionServiceImpl.class);

    @Autowired(required = false)
    @Qualifier("pauseResumeService")
    private PauseResumeService pauseService;

    @Autowired
    private ReflectionAdapter reflectionAdapter;

    @Autowired(required = false)
    @Qualifier("workerDbSupportService")
    private WorkerDbSupportService workerDbSupportService;

    @Autowired
    private WorkerConfigurationService workerConfigurationService;

    @Autowired(required = false)
    @Qualifier("aplsLicensingService")
    private AplsLicensingService aplsLicensingService;

    @Autowired
    private EventBus eventBus;

    @Autowired
    @Qualifier("consumptionFastEventBus")
    private FastEventBus fastEventBus;

    @Autowired(required = false)
    @Qualifier("robotAvailabilityService")
    private RobotAvailabilityService robotAvailabilityService;

    private static final int DEFAULT_PLATFORM_LEVEL_OPERATION_TIMEOUT_IN_SECONDS = 24 * 60 * 60; // seconds in a day
    private static final int DEFAULT_PLATFORM_LEVEL_WAIT_PERIOD_FOR_TIMEOUT_IN_SECONDS = 5 * 60; // 5 minutes
    private static final long DEFAULT_PLATFORM_LEVEL_WAIT_PAUSE_FOR_TIMEOUT_IN_MILLIS = 200; // 200 milliseconds

    private final long operationTimeoutMillis;
    private final long waitPauseForTimeoutMillis;
    private final long waitPeriodForTimeoutMillis;
    private final boolean interruptOperationExecution;
    private final boolean enableNewTimeoutMechanism;
    private ExecutorService executorService;

    @PostConstruct
    public void init() {
        ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("miAsync - %d").build();
        executorService = new ThreadPoolExecutor(5, 5, MAX_VALUE, MILLISECONDS, new LinkedBlockingDeque<>(20), threadFactory, new ThreadPoolExecutor.CallerRunsPolicy());
    }

    @PreDestroy
    public void destroy() {
        executorService.shutdown();
        try {
            executorService.awaitTermination(30, SECONDS);
        } catch (InterruptedException ignored) {
        } finally {
            executorService.shutdownNow();
        }
    }

    public ExecutionServiceImpl() {
        this.operationTimeoutMillis = getSafeIntProperty("execution.operationTimeoutInSeconds",
                DEFAULT_PLATFORM_LEVEL_OPERATION_TIMEOUT_IN_SECONDS) * 1000L;
        this.waitPeriodForTimeoutMillis = getSafeIntProperty("execution.waitPeriodForTimeoutInSeconds",
                DEFAULT_PLATFORM_LEVEL_WAIT_PERIOD_FOR_TIMEOUT_IN_SECONDS) * 1000L;
        this.waitPauseForTimeoutMillis = getSafeLongProperty("execution.waitPauseForTimeoutInMillis",
                DEFAULT_PLATFORM_LEVEL_WAIT_PAUSE_FOR_TIMEOUT_IN_MILLIS);
        this.interruptOperationExecution = getBoolean("execution.interruptOperation");
        this.enableNewTimeoutMechanism = getBoolean("enable.new.timeout");
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
            // Handle cancel or pause of execution
            if (handleCancelledFlow(execution)
                    || (!isDebuggerMode(execution.getSystemContext()) && handlePausedFlow(execution))) {
                return handleCancelledFlow(execution) ? execution : null;
            }

            checkoutLicenseForLaneIfRequired(execution);

            // Dump the bus events before execution of steps
            dumpBusEvents(execution);
            // Load the execution step
            ExecutionStep currStep = loadExecutionStep(execution);
            // Run the execution step
            String timeoutMessage = executeStep(execution, currStep);
            // Handle timeout for content operation execution
            if (timeoutMessage != null) {
                try {
                    return doWaitForCancel(execution);
                } catch (TimeoutException timeout) {
                    logger.error("Timed out waiting for cancel for execution id " + execution.getExecutionId());
                    execution.getSystemContext().setStepErrorKey(timeoutMessage);
                }
            }
            // Pause execution in case of reaching a sequential step
            if ((!execution.getSystemContext().hasStepErrorKey()) && (currStep.getActionData().get(ACTION_TYPE) != null)
                    && currStep.getActionData().get(ACTION_TYPE).toString().equalsIgnoreCase(SEQUENTIAL)) {
                // Pause the execution here, the rest of the steps are done by the Sequential Message Handler
                return null;
            }
            // Execute the step navigation
            navigate(execution, currStep);
            // Handle Worker group and change of running execution plan
            postExecutionSettings(execution);
            // If execution was paused in language - to prevent spin in case of engine internal pause
            if (execution.getSystemContext().isPaused()) {
                if (handlePausedFlowAfterStep(execution)) {
                    return null;
                }
            }
            // Dump the bus events
            dumpBusEvents(execution);
            // Update MI suspended execution
            updateMiIfRequired(execution);
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

    private void checkoutLicenseForLaneIfRequired(Execution execution) {
        try {
            String licenseType = (String) execution.getSystemContext().get(LICENSE_TYPE);
            if (StringUtils.equalsIgnoreCase(licenseType, "SUITE_LICENSE")) {
                return;
            }
            String branchIdToCheckoutLicense = (String) execution.getSystemContext().get(BRANCH_ID_TO_CHECK_OUT_LICENSE);
            if (StringUtils.isNotEmpty(branchIdToCheckoutLicense) && StringUtils.equals(branchIdToCheckoutLicense, execution.getSystemContext().getBranchId())) {
                String executionId = execution.getExecutionId().toString();
                Long executionStartTimeMillis = Optional.ofNullable((Long) execution.getSystemContext().get(SC_TIMEOUT_START_TIME)).orElse(0L);
                Integer executionTimeoutMinutes = Optional.ofNullable((Integer) execution.getSystemContext().get(SC_TIMEOUT_MINS)).orElse(0);
                aplsLicensingService.checkoutBeginLane(executionId, branchIdToCheckoutLicense, executionStartTimeMillis, executionTimeoutMinutes);
                execution.getSystemContext().put(BRANCH_ID_TO_CHECK_IN_LICENSE, execution.getSystemContext().getBranchId());
            }
        } finally {
            execution.getSystemContext().remove(BRANCH_ID_TO_CHECK_OUT_LICENSE);
        }
    }

    private void updateMiIfRequired(Execution execution) {
        if (execution.getSystemContext().containsKey(MI_REMAINING_BRANCHES_CONTEXT_KEY)) {
            executorService.execute(() -> workerDbSupportService.updateSuspendedExecutionMiThrottlingContext(execution));
        }
    }

    @Override
    public void pauseSequentialExecution(Execution execution) throws InterruptedException {
        final PauseReason pauseReason =
                robotAvailabilityService.isRobotAvailable(execution.getRobotGroupName()) ? PENDING_ROBOT : NO_ROBOTS_IN_GROUP;
        pauseFlow(execution, pauseReason);
    }

    @Override
    public void postExecutionWork(Execution execution) throws InterruptedException {
        navigate(execution, loadExecutionStep(execution));
        postExecutionSettings(execution);
        dumpBusEvents(execution);
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
    public List<Execution> executeSplitForNonBlockAndParallel(Execution execution) throws InterruptedException {
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
            List<Execution> newExecutions = createChildExecutionsForNonBlockingAndParallel(execution.getExecutionId(),
                    newBranches, currStep);
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

    @Override
    public List<Execution> executeSplitForMiAndParallelLoop(Execution execution,
                                                            String splitUuid,
                                                            int nrOfAlreadyCreatedBranches,
                                                            String splitDataKey) throws InterruptedException {
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
            List<Execution> newExecutions = createChildExecutionsForMi(execution.getExecutionId(), newBranches,
                    splitUuid, nrOfAlreadyCreatedBranches, currStep);

            Serializable splitDataValue = execution.getSystemContext().get(splitDataKey);
            if (splitDataValue == null) {
                // Run the navigation since we don't have any inputs left to process
                navigate(execution, currStep);
            }

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
                createErrorEvent(exception, "Error occurred during split step ", SCORE_STEP_SPLIT_ERROR,
                        execution.getSystemContext());
            } catch (RuntimeException eventEx) {
                logger.error("Failed to create event: ", eventEx);
            }
            throw new RuntimeException(exception);
        }
    }

    private List<Execution> createChildExecutionsForNonBlockingAndParallel(Long executionId,
                                                                           List<StartBranchDataContainer> newBranches,
                                                                           ExecutionStep currStep) {
        List<Execution> newExecutions = new ArrayList<>();
        String splitId = UUID.randomUUID().toString();
        ListIterator<StartBranchDataContainer> listIterator = newBranches.listIterator();
        int count = 0;
        while (listIterator.hasNext()) {
            StartBranchDataContainer from = listIterator.next();
            Execution to = new Execution(executionId, from.getExecutionPlanId(), from.getStartPosition(),
                    from.getContexts(), from.getSystemContext());

            to.getSystemContext().setSplitId(splitId);
            String branchId = splitId + ":" + (count++ + 1);
            to.getSystemContext().setBranchId(branchId);
            newExecutions.add(to);
            dispatchBranchStartEvent(executionId, splitId, branchId, currStep);
            addCheckoutLaneFlagToContext(branchId, to.getSystemContext(), count);
        }
        return newExecutions;
    }

    private List<Execution> createChildExecutionsForMi(Long executionId,
                                                       List<StartBranchDataContainer> newBranches,
                                                       String splitUuid,
                                                       int nrOfAlreadyCreatedBranches,
                                                       ExecutionStep currStep) {
        List<Execution> newExecutions = new ArrayList<>();
        ListIterator<StartBranchDataContainer> listIterator = newBranches.listIterator();
        int count = 0;
        while (listIterator.hasNext()) {
            StartBranchDataContainer from = listIterator.next();
            Execution to = new Execution(executionId, from.getExecutionPlanId(), from.getStartPosition(),
                    from.getContexts(), from.getSystemContext());

            to.getSystemContext().setSplitId(splitUuid);
            int branchIndexInSplitStep = nrOfAlreadyCreatedBranches + count++ + 1;
            String branchId = splitUuid + ":" + branchIndexInSplitStep;
            to.getSystemContext().setBranchId(branchId);
            dispatchBranchStartEvent(executionId, splitUuid, branchId, currStep);
            addCheckoutLaneFlagToContext(branchId, to.getSystemContext(), count);
            newExecutions.add(to);
        }
        return newExecutions;
    }

    private void addCheckoutLaneFlagToContext(String branchId, SystemContext systemContext, int branchNumber) {
        Integer parallelismLevel = (Integer) systemContext.getLevelParallelism();
        boolean nonBlocking = "NON_BLOCKING".equals(systemContext.get("STEP_TYPE"));
        if (!nonBlocking && parallelismLevelToCheckout(parallelismLevel, branchNumber)) {
            systemContext.put(BRANCH_ID_TO_CHECK_OUT_LICENSE, branchId);
        }
    }

    private boolean parallelismLevelToCheckout(Integer parallelismLevel, int branchNumber) {
        return parallelismLevel != null && (parallelismLevel == 1 || (parallelismLevel > 1 && branchNumber > 1));
    }

    @Override
    public boolean isSplitStep(Execution execution) {
        ExecutionStep currStep = loadExecutionStep(execution);
        return currStep.isSplitStep();
    }

    protected boolean handleCancelledFlow(Execution execution) {
        // In this case - just check if need to cancel. It will set as cancelled later on QueueEventListener
        // Another scenario of getting canceled - it was cancelled from the SplitJoinService
        // The configuration can still be not updated
        if (workerConfigurationService.isExecutionCancelled(execution.getExecutionId())
                || (execution.getSystemContext().getFlowTerminationType() == ExecutionStatus.CANCELED)) {
            // NOTE: an execution can be cancelled directly from CancelExecutionService, if it's currently paused.
            // Thus, if you change the code here, please check CancelExecutionService as well.
            execution.getSystemContext().setFlowTerminationType(ExecutionStatus.CANCELED);
            execution.setPosition(null);
            return true;
        } else {
            return false;
        }
    }

    // check if the execution should be Paused, and pause it if needed
    protected boolean handlePausedFlow(Execution execution) throws InterruptedException {
        String branchId = execution.getSystemContext().getBranchId();
        PauseReason reason = findPauseReason(execution.getExecutionId(), branchId);
        if (reason != null) { // need to pause the execution
            pauseFlow(execution, reason);
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
        if (reason != null) {
            // need to pause the execution
            pauseFlow(execution, reason);
            return true;
        }
        return false;
    }

    private void pauseFlow(Execution execution, PauseReason reason) throws InterruptedException {
        SystemContext systemContext = execution.getSystemContext();
        Long executionId = execution.getExecutionId();
        String branchId = systemContext.getBranchId();
        // If USER_PAUSED send such event
        if (!isDebuggerMode(execution.getSystemContext()) && reason.equals(PauseReason.USER_PAUSED)) {
            if (branchId != null) {
                // we pause the branch because the Parent was user-paused (see findPauseReason)
                pauseService.pauseExecution(executionId, branchId, reason); // this creates a DB record for this branch, as Pending-paused
            }
        } else if (reason == NO_ROBOTS_IN_GROUP || reason == PENDING_ROBOT) {
            Long pauseId = pauseService.pauseExecution(executionId, branchId, reason);
            if (pauseId != null && reason == NO_ROBOTS_IN_GROUP) {
                pauseService.createNoRobotGroup(execution, pauseId, branchId);
                logger.warn("Can't assign robot for group name: " + systemContext.getRobotGroupName() + "; because there are no available robots for that group.");
            }
        }
        addPauseEvent(systemContext);
        // dump bus events here because out side is too late
        dumpBusEvents(execution);
        // Write execution to the db! Pay attention - do not do anything to the execution or its context after this line!!!
        pauseService.writeExecutionObject(executionId, branchId, execution, false);
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
            if (execSummary != null && EnumSet.of(ExecutionStatus.PENDING_PAUSE, ExecutionStatus.PAUSED).contains(execSummary.getStatus())) {
                PauseReason reason = execSummary.getPauseReason();
                // we only care about User-Paused here!
                // we don't want to Pause if the parent is paused due to branch_paused! (other branch is paused for some reason
                // (e.g. required_input), so the parent is paused as well).
                if (PauseReason.USER_PAUSED.equals(reason)) {
                    return reason;
                }
            }
        }
        return null; // not paused
    }

    private static boolean isDebuggerMode(Map<String, Serializable> systemContext) {
        final Boolean isDebuggerMode = (Boolean) systemContext.get(TempConstants.DEBUGGER_MODE);
        return (isDebuggerMode != null) && isDebuggerMode;
    }

    private void dumpBusEvents(Execution execution) throws InterruptedException {
        final ArrayDeque<ScoreEvent> eventsQueue = execution.getSystemContext().getEvents();
        if ((eventsQueue != null) && !eventsQueue.isEmpty()) {
            for (ScoreEvent eventWrapper : eventsQueue) {
                eventBus.dispatch(eventWrapper);
            }
            eventsQueue.clear();
        }
    }

    protected ExecutionStep loadExecutionStep(Execution execution) {
        RunningExecutionPlan runningExecutionPlan;
        // Optimization for external workers - run the content only without loading the execution plan
        if (execution.getSystemContext().get(TempConstants.CONTENT_EXECUTION_STEP) != null) {
            return (ExecutionStep) execution.getSystemContext().get(TempConstants.CONTENT_EXECUTION_STEP);
        } else {
            Long position = execution.getPosition();
            if (position != null) {
                runningExecutionPlan = workerDbSupportService.readExecutionPlanById(execution.getRunningExecutionPlanId());
                if (runningExecutionPlan != null) {
                    updateMetadata(execution, runningExecutionPlan);
                    ExecutionStep currStep = runningExecutionPlan.getExecutionPlan().getStep(position);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Begin step: " + position
                                + " in flow " + runningExecutionPlan.getExecutionPlan().getFlowUuid()
                                + " [" + execution.getExecutionId() + "]");
                    }
                    if (currStep != null) {
                        return currStep;
                    }
                }
            }
            // If we got here - one of the objects was null
            throw new RuntimeException("Failed to load ExecutionStep!");
        }
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
            final ReadonlyStepActionDataAccessor actionDataAccessor = doPrepareStepData(execution, currStep);
            final ControlActionMetadata action = currStep.getAction();

            if (enableNewTimeoutMechanism && isContentOperationStep(action)) {
                Long startTime = (Long) execution.getSystemContext().get(SC_TIMEOUT_START_TIME);
                Integer timeoutMins = (Integer) execution.getSystemContext().get(SC_TIMEOUT_MINS);

                // New Timeout is enabled and timeout information is available for this run
                if ((startTime != null) && (timeoutMins != null)) {
                    long now = System.currentTimeMillis();
                    Callable<Object> operationCallable = () -> reflectionAdapter.executeControlAction(action, actionDataAccessor);
                    SandboxExecutionRunnable<Object> sandboxExecutionRunnable =
                            new SandboxExecutionRunnable<>(currentThread().getContextClassLoader(), operationCallable);
                    Thread operationExecutionThread = new Thread(sandboxExecutionRunnable);

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

                    if (operationExecutionThread.isAlive()) { // Timeout exceeded
                        String timeoutErrorMessageDuringStep = String
                                .format("Timeout (%d minutes) exceeded for execution id %s having start time %s (current time %s) when running step %s",
                                        timeoutMins, valueOf(execution.getExecutionId()), valueOf(startTime),
                                        valueOf(System.currentTimeMillis()), valueOf(currStep.getExecStepId()));
                        logger.error(timeoutErrorMessageDuringStep);

                        if (interruptOperationExecution) {
                            operationExecutionThread.interrupt(); // interrupt the execution of the content operation
                        }
                        return timeoutErrorMessageDuringStep;
                    } else { // Thread was finished
                        sandboxExecutionRunnable.afterExecute();
                    }
                } else { // Execute on regular executor as usual if no timeout information is present
                    reflectionAdapter.executeControlAction(action, actionDataAccessor);
                }
            } else { // Execute on regular executor as this is not an operation or new mechanism is not enabled
                reflectionAdapter.executeControlAction(action, actionDataAccessor);
            }
        } catch (RuntimeException ex) {
            handleStepExecutionException(execution, ex);
        }

        return null;
    }

    private boolean isContentOperationStep(ControlActionMetadata action) {
        return (action != null) && StringUtils.equals(action.getMethodName(), EXECUTE_CONTENT_ACTION) &&
                endsWith(action.getClassName(), EXECUTE_CONTENT_ACTION_CLASSNAME);
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
            ReadonlyStepActionDataAccessor readonlyStepActionDataAccessor = doPrepareStepData(execution, currStep);
            reflectionAdapter.executeControlAction(currStep.getAction(), readonlyStepActionDataAccessor);
        } catch (RuntimeException ex) {
            handleStepExecutionException(execution, ex);
        }
    }

    private static void handleStepExecutionException(Execution execution, RuntimeException ex) {
        logger.error("Error occurred during operation execution.  Execution id: " + execution.getExecutionId(), ex);
        execution.getSystemContext().setStepErrorKey(ex.getMessage());
    }

    private ReadonlyStepActionDataAccessor doPrepareStepData(Execution execution, ExecutionStep currStep) {
        StepActionDataHolder stepActionDataHolder = new StepActionDataHolder();
        stepActionDataHolder.addNullablePartToHolder(currStep.getActionData());
        doAddNavigationRelatedParts(execution, currStep, stepActionDataHolder);
        return new ReadonlyStepActionDataAccessor(stepActionDataHolder);
    }

    private ReadonlyStepActionDataAccessor doPrepareNavigationStepData(Execution execution, ExecutionStep currStep) {
        StepActionDataHolder actionDataHolder = new StepActionDataHolder();
        doAddNavigationRelatedParts(execution, currStep, actionDataHolder);
        return new ReadonlyStepActionDataAccessor(actionDataHolder);
    }

    private void doAddNavigationRelatedParts(Execution execution, ExecutionStep currStep, StepActionDataHolder actionDataHolder) {
        actionDataHolder.addNotNullPartToHolder(currStep.getNavigationData());
        actionDataHolder.addNotNullPartToHolder(execution.getContexts());
        actionDataHolder.addNotNullPartToHolder(getStepMetadataMap(execution));
    }

    private Map<String, Object> getStepMetadataMap(Execution execution) {
        // Maps.newHashMapWithExpectedSize gives 5 + 5 / 3 = 6
        Map<String, Object> data = new HashMap<>(6);
        data.put(ExecutionParametersConsts.SYSTEM_CONTEXT, execution.getSystemContext());
        data.put(ExecutionParametersConsts.EXECUTION_RUNTIME_SERVICES, execution.getSystemContext());
        data.put(ExecutionParametersConsts.EXECUTION, execution);
        data.put(ExecutionParametersConsts.EXECUTION_CONTEXT, execution.getContexts());
        data.put(ExecutionParametersConsts.RUNNING_EXECUTION_PLAN_ID, execution.getRunningExecutionPlanId());
        return data;
    }

    protected void navigate(Execution execution, ExecutionStep currStep) throws InterruptedException {
        Long position;
        try {
            if (currStep.getNavigation() != null) {
                // We add all the contexts to the step data - so inside of each control action we will have access to all contexts
                ReadonlyStepActionDataAccessor readonlyActionDataAccessor = doPrepareNavigationStepData(execution, currStep);
                position = (Long) reflectionAdapter.executeControlAction(currStep.getNavigation(), readonlyActionDataAccessor);
                execution.setPosition(position);
            } else {
                execution.setPosition(null); // terminate the flow - we got to the last step!
            }
        } catch (RuntimeException navEx) {
            // If Exception occurs in navigation (almost impossible since now we always have Flow Exception Step)
            // we can not continue since we don't know which step is the next step...
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

    protected void postExecutionSettings(Execution execution) {
        setWorkerGroup(execution);

        //if there is a request to change the running execution plan id, we update the execution to the new execution plan ID
        Long requestForChangingExecutionPlan = execution.getSystemContext().pullRequestForChangingExecutionPlan();
        if (requestForChangingExecutionPlan != null) {
            execution.setRunningExecutionPlanId(requestForChangingExecutionPlan);
        }
    }

    private void setWorkerGroup(Execution execution) {
        String group = (String) execution.getSystemContext().get(TempConstants.ACTUALLY_OPERATION_GROUP);

        if (group != null) {
            execution.setGroupName(group);
        }
        // Removed debugger mode check. This will allow user to remotely debug flows from studio/designer on specific worker group. Defect OCTCR19F1753456.

    }

    private void createErrorEvent(String ex, String logMessage, String errorType, SystemContext systemContext)
            throws InterruptedException {
        HashMap<String, Serializable> eventData = new HashMap<>();
        eventData.put(ExecutionParametersConsts.SYSTEM_CONTEXT, new HashMap<>(systemContext));
        eventData.put(EventConstants.SCORE_ERROR_MSG, ex);
        eventData.put(EventConstants.EXECUTION_ID_CONTEXT, systemContext.getExecutionId());
        eventData.put(EventConstants.SCORE_ERROR_LOG_MSG, logMessage);
        eventData.put(EventConstants.SCORE_ERROR_TYPE, errorType);
        ScoreEvent eventWrapper = new ScoreEvent(EventConstants.SCORE_ERROR_EVENT, eventData);
        eventBus.dispatch(eventWrapper);
    }

    private void dispatchBranchStartEvent(Long executionId, String splitId, String branchId, ExecutionStep currStep) {
        HashMap<String, Serializable> eventData = new HashMap<>();
        eventData.put(EXECUTION_ID, executionId);
        eventData.put(SPLIT_ID, splitId);
        eventData.put(BRANCH_ID, branchId);
        String stepPath = currStep.getActionData().get("refId") + "/" + currStep.getActionData().get("nodeName");
        eventData.put(STEP_PATH, stepPath);
        ScoreEvent eventWrapper = new ScoreEvent(EventConstants.SCORE_STARTED_BRANCH_EVENT, eventData);
        fastEventBus.dispatch(eventWrapper);
    }
}
