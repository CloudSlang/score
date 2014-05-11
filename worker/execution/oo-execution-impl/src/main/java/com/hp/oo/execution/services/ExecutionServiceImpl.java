package com.hp.oo.execution.services;

import com.hp.oo.broker.entities.BranchContextHolder;
import com.hp.oo.broker.entities.RunningExecutionPlan;
import com.hp.oo.broker.services.RuntimeValueService;
import com.hp.oo.engine.execution.events.services.ExecutionEventService;
import com.hp.oo.enginefacade.execution.ExecutionEnums;
import com.hp.oo.enginefacade.execution.ExecutionEnums.ExecutionStatus;
import com.hp.oo.enginefacade.execution.ExecutionEnums.LogLevel;
import com.hp.oo.enginefacade.execution.ExecutionEnums.LogLevelCategory;
import com.hp.oo.enginefacade.execution.ExecutionSummary;
import com.hp.oo.enginefacade.execution.PauseReason;
import com.hp.oo.enginefacade.execution.StartBranchDataContainer;
import com.hp.oo.execution.ExecutionEventAggregatorHolder;
import com.hp.oo.execution.ExecutionLogLevelHolder;
import com.hp.oo.execution.gateways.EventGateway;
import com.hp.oo.execution.reflection.ReflectionAdapter;
import com.hp.oo.execution.services.dbsupport.WorkerDbSupportService;
import com.hp.oo.internal.sdk.execution.Execution;
import com.hp.oo.internal.sdk.execution.ExecutionConstants;
import com.hp.oo.internal.sdk.execution.ExecutionStep;
import com.hp.oo.internal.sdk.execution.OOContext;
import com.hp.oo.internal.sdk.execution.events.EventBus;
import com.hp.oo.internal.sdk.execution.events.EventWrapper;
import com.hp.oo.internal.sdk.execution.events.ExecutionEvent;
import com.hp.oo.internal.sdk.execution.events.ExecutionEventFactory;
import com.hp.oo.internal.sdk.execution.events.ExecutionEventUtils;
import com.hp.oo.orchestrator.services.CancelExecutionService;
import com.hp.oo.orchestrator.services.PauseResumeService;
import com.hp.oo.orchestrator.services.configuration.WorkerConfigurationService;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
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

    @Autowired
    private PauseResumeService pauseService;

    @Autowired
    private ReflectionAdapter reflectionAdapter;

    @Autowired
    private WorkerDbSupportService workerDbSupportService;

    @Autowired
    private EventGateway eventGateway;

    @Autowired
    private WorkerConfigurationService configurationService;

    @Autowired
    private ExecutionEventService executionEventService;

    @Autowired
    private CancelExecutionService cancelExecutionService;

    @Autowired
    private RuntimeValueService runtimeValueService;

    @Autowired
    private WorkerRecoveryManager recoveryManager;

    @Autowired
    private EventBus eventBus;

    private boolean eventsPersistencyOn = Boolean.getBoolean("events.persistency");

    @Override
    public Execution execute(Execution execution) {
        try {
            // sets thread local context with log level
            storeCurrentLogLevel(execution);

            // sets thread local context with a reference to the execution objects aggregated events list
            storeAggregatedEvents(execution);

            // handle flow cancellation
            if (handleCancelledFlow(execution, isDebuggerMode(execution.getSystemContext()))) {
                dumpExecutionEvents(execution, true);
                return execution;
            }

            ExecutionStep currStep = loadExecutionStep(execution);

            //Check if this execution was paused
            if (!isDebuggerMode(execution.getSystemContext()) && handlePausedFlow(execution)) {
                return null;
            }

            if(execution.getSystemContext().get(ExecutionConstants.EXECUTION_EVENTS_QUEUE) != null){
                //TODO temproray solution until Elia finish moving PauseResumeServiceImpl  to score
                @SuppressWarnings("unchecked") ArrayDeque<ExecutionEvent> eventsQueue = (ArrayDeque) execution.getSystemContext().get(ExecutionConstants.EXECUTION_EVENTS_QUEUE);
                Iterator<ExecutionEvent> executionEvents  = eventsQueue.iterator();
                while(executionEvents.hasNext()){
                    ExecutionEvent executionEvent  =  executionEvents.next();
                    if(executionEvent.getType().equals(ExecutionEnums.Event.STEP_LOG)
                            && executionEvent.getStepLogCategory().equals(ExecutionEnums.StepLogCategory.STEP_RESUMED)){
                        eventBus.dispatch(new EventWrapper(executionEvent.getType().name(), executionEvent));
                    }
                }
            }


            //Run the execution step
            executeStep(execution, currStep);

            //Run the navigation
            navigate(execution, currStep);

            // currently handles groups and jms optimizations
            postExecutionSettings(execution);

            //If execution was  paused in afl - to avoid delay of configuration
            if (execution.getSystemContext().containsKey(ExecutionConstants.EXECUTION_PAUSED)) {
                if (!isDebuggerMode(execution.getSystemContext()) && handlePausedFlowAfterStep(execution)) {
                    return null;
                }
                if (isDebuggerMode(execution.getSystemContext()) && handlePausedFlowForDebuggerMode(execution)) {
                    return null;
                }
            }
            //dum bus event
            dumpBusEvents(execution);

            // add execution events
            addExecutionEvent(execution);

            // dump execution events
            dumpExecutionEvents(execution, false);

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
            // sets thread local context with log level
            storeCurrentLogLevel(execution);

            // sets thread local context with a reference to the execution objects aggregated events list
            storeAggregatedEvents(execution);

            ExecutionStep currStep = loadExecutionStep(execution);

            //Run the split step
            List<StartBranchDataContainer> newBranches = executeSplitStep(execution, currStep);

            List<Execution> newExecutions = createChildExecutions(execution.getExecutionId(), newBranches);

            //Run the navigation
            navigate(execution, currStep);

            // add execution events
            addExecutionEvent(execution);

            // dump execution events
            dumpExecutionEvents(execution, true);

            if (logger.isDebugEnabled()) {
                logger.debug("End of step: " + execution.getPosition() + " in execution id: " + execution.getExecutionId());
            }

            return newExecutions;
        } catch (Exception ex) {
            logger.error("Exception during the split step!", ex);
            throw ex;
        }
    }

    private List<Execution> createChildExecutions(String executionId, List<StartBranchDataContainer> newBranches) {
        List<Execution> newExecutions = new ArrayList<>();
        String splitId = UUID.randomUUID().toString();

        for (int i = 0; i < newBranches.size(); i++) {
            StartBranchDataContainer from = newBranches.get(i);
            Execution to = new Execution(Long.parseLong(executionId),
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

    protected boolean handleCancelledFlow(Execution execution, boolean isDebugMode) {

        String executionId = execution.getExecutionId();
        boolean executionIsCancelled;

        // Different methods to see if the run is cancelled, and cancel it:
        // in debug-mode we go to the DB (thru the service).
        // in regular execution we don't want to ask the DB every step (expensive..), so we use the WorkerConfigurationService which goes to the DB every x seconds.
        if (isDebugMode) {
            executionIsCancelled = cancelExecutionService.isCanceledExecution(executionId); // in this case we already cancel the execution if needed
        } else {
            List<String> cancelledExecutions = configurationService.getCancelledExecutions(); // in this case - just check if need to cancel. It will set as cancelled later on QueueEventListener
            executionIsCancelled = cancelledExecutions.contains(executionId);
        }

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

        Map<String, Serializable> systemContext = execution.getSystemContext();
        String executionId = execution.getExecutionId();
        String branchId = (String) systemContext.get(ExecutionConstants.BRANCH_ID);
        String flowUuid = (String) systemContext.get(ExecutionConstants.FLOW_UUID);

        @SuppressWarnings({"unchecked"})
        ArrayDeque<ExecutionEvent> eventsQueue = (ArrayDeque<ExecutionEvent>) systemContext.get(ExecutionConstants.EXECUTION_EVENTS_QUEUE);
        if (eventsQueue == null) {
            eventsQueue = new ArrayDeque<>();
            execution.getSystemContext().put(ExecutionConstants.EXECUTION_EVENTS_QUEUE, eventsQueue);
        }

        //If USER_PAUSED send such event
        if (reason.equals(PauseReason.USER_PAUSED)) {
            ExecutionEvent pauseEvent = ExecutionEventFactory.createPausedEvent(executionId, flowUuid, ExecutionEventUtils.increaseEvent(systemContext), systemContext);
            eventsQueue.add(pauseEvent);

            if (branchId != null) {
                // we pause the branch because the Parent was user-paused (see findPauseReason)
                pauseService.pauseExecution(executionId, branchId, reason); // this creates a DB record for this branch, as Pending-paused
            }
        }

        ExecutionEvent stepLogEvent = ExecutionEventFactory.createStepLogEvent(executionId,ExecutionEventUtils.increaseEvent(systemContext), ExecutionEnums.StepLogCategory.STEP_PAUSED, systemContext);
        eventsQueue.add(stepLogEvent);

        //dump bus events here because out side is too late
        dumpBusEvents(execution);

        //just dump events that were written in afl or just now
        addExecutionEvent(execution);
        dumpEvents(execution);

        //Write execution to the db! Pay attention - do not do anything to the execution or its context after this line!!!
        pauseService.writeExecutionObject(executionId, branchId, execution);

        if (logger.isDebugEnabled()) {
            logger.debug("Execution with execution_id: " + execution.getExecutionId() + " is paused!");
        }
    }

    private boolean handlePausedFlowForDebuggerMode(Execution execution) {

        Map<String, Serializable> systemContext = execution.getSystemContext();
        String executionId = execution.getExecutionId();
        String branchId = (String) systemContext.get(ExecutionConstants.BRANCH_ID);

        //get the type of paused flow

        PauseReason reason = null;
        ExecutionSummary execSummary = pauseService.readPausedExecution(execution.getExecutionId(), branchId);
        if (execSummary != null && execSummary.getStatus().equals(ExecutionStatus.PENDING_PAUSE)) {
            reason = execSummary.getPauseReason();
        }

        if (reason == null) {
            return false; //indicate that the flow was not paused
        }

        @SuppressWarnings("unchecked") Deque<ExecutionEvent> eventsQueue = (Deque) systemContext.get(ExecutionConstants.EXECUTION_EVENTS_QUEUE);
        ExecutionEvent pausedStepEvent = ExecutionEventFactory.createStepLogEvent(executionId, ExecutionEventUtils.increaseEvent(systemContext),ExecutionEnums.StepLogCategory.STEP_PAUSED,systemContext);
        eventsQueue.add(pausedStepEvent);

        dumpBusEvents(execution);

        //just dump events that were written in afl
        addExecutionEvent(execution);
        dumpEvents(execution);

        //Write execution to the db! Pay attention - do not do anything to the execution or its context after this line!!!
        pauseService.writeExecutionObject(executionId, branchId, execution);

        if (logger.isDebugEnabled()) {
            logger.debug("Execution with execution_id: " + execution.getExecutionId() + " is paused!");
        }

        return true;
    }

    private PauseReason findPauseReason(String executionId, String branchId) {

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


    private void storeAggregatedEvents(Execution execution) {
        if (execution.getAggregatedEvents() == null) {
            execution.setAggregatedEvents(new ArrayList<ExecutionEvent>());
        }

        ExecutionEventAggregatorHolder.setAggregatedExecutionEvents(execution.getAggregatedEvents());
    }

    private void addExecutionEvent(Execution execution) {
        // move all the events form the SystemContext into the event channel
        @SuppressWarnings("unchecked") ArrayDeque<ExecutionEvent> eventsQueue = (ArrayDeque) execution.getSystemContext().get(ExecutionConstants.EXECUTION_EVENTS_QUEUE);
        for (ExecutionEvent executionEvent : eventsQueue) {
            eventGateway.addEvent(executionEvent);
        }

        eventsQueue.clear();
        execution.getSystemContext().remove(ExecutionConstants.EXECUTION_EVENTS_QUEUE);
        // clean up thread local context to avoid memory leaks
        ExecutionLogLevelHolder.removeExecutionLogLevel();
    }

    private void dumpExecutionEvents(Execution execution, boolean forceDump) {
        boolean shouldDump = false;
        long currTime = System.currentTimeMillis();

        //Init it according system context (done in order to support parallel immediate events release of parent lane)
        if (execution.getSystemContext().get(ExecutionConstants.MUST_RELEASE_EVENTS) != null) {
            shouldDump = (boolean) execution.getSystemContext().get(ExecutionConstants.MUST_RELEASE_EVENTS);
            execution.getSystemContext().remove(ExecutionConstants.MUST_RELEASE_EVENTS);
        }

        // timeout trigger
        //noinspection ConstantConditions
        shouldDump |= (execution.getLastEventDumpTime() != 0) &&
                (currTime - execution.getLastEventDumpTime() >= ExecutionConstants.EVENT_AGGREGATION_TIME_THRESHOLD);

        // amount trigger
        shouldDump |= execution.getAggregatedEvents().size() >= ExecutionConstants.EVENT_AGGREGATION_AMOUNT_THRESHOLD;

        // ending execution trigger (null for flow ending, -1L for mi/parralel, -2L for subflow)
        shouldDump |= isExecutionTerminating(execution);

        // force it ?
        shouldDump |= forceDump;

        // make sure we are actually sending something
        shouldDump &= execution.getAggregatedEvents().size() > 0;

        if (shouldDump) {
            dumpEvents(execution);
        }

        // clean up thread local context to avoid memory leaks
        ExecutionEventAggregatorHolder.removeAggregatedExecutionEvents();
    }

    private boolean isDebuggerMode(Map<String, Serializable> systemContext) {

        Boolean isDebuggerMode = (Boolean) systemContext.get(ExecutionConstants.DEBUGGER_MODE);
        if (isDebuggerMode == null) {
            return false;
        }

        return isDebuggerMode;
    }

    private void dumpEvents(Execution execution) {
        List<ExecutionEvent> executionEvents = execution.getAggregatedEvents();
        for (ExecutionEvent executionEvent:executionEvents){
            eventBus.dispatch(new EventWrapper(executionEvent.getType().name(), executionEvent));
        }

        List<ExecutionEvent> filteredExecutionEvents = new ArrayList<>();
        for (ExecutionEvent executionEvent : executionEvents){
            if(executionEvent.getType().equals(ExecutionEnums.Event.STEP_LOG)){
                continue;
            }
            filteredExecutionEvents.add(executionEvent);
        }
        if(eventsPersistencyOn || isDebuggerMode(execution.getSystemContext())){ //consider flag events and debugger before sending events
            executionEventService.createEvents(filteredExecutionEvents);
        }
        execution.getAggregatedEvents().clear(); //must clean so we wont send it twice - once from here and once from the QueueListener onTerminated()
        execution.setLastEventDumpTime(System.currentTimeMillis());
    }

//    private void dumpEvents(Execution execution) {
//        List<ExecutionEvent> executionEvents = execution.getAggregatedEvents();
//        executionEventService.createEvents(executionEvents);
//        execution.getAggregatedEvents().clear(); //must clean so we wont send it twice - once from here and once from the QueueListener onTerminated()
//        execution.setLastEventDumpTime(System.currentTimeMillis());
//    }

    private void dumpBusEvents(Execution execution) {
        @SuppressWarnings("unchecked") ArrayDeque<ExecutionEvent> eventsQueue = (ArrayDeque) execution.getSystemContext().get(ExecutionConstants.EXECUTION_EVENTS_QUEUE);
        Iterator<ExecutionEvent> executionEvents  = eventsQueue.iterator();

        while(executionEvents.hasNext()){
            ExecutionEvent executionEvent  =  executionEvents.next();
            if(executionEvent.getType().equals(ExecutionEnums.Event.STEP_LOG)){
//                System.out.println(executionEvent.getExecutionId() + ", " + executionEvent.getPath() +"," + executionEvent.getType() +":" + executionEvent.getStepLogCategory() + "-- " + executionEvent);
                eventBus.dispatch(new EventWrapper(executionEvent.getType().name(), executionEvent));
                executionEvents.remove();
            }
        }
    }

    private void storeCurrentLogLevel(Execution execution) {
        String logLevelStr = (String) execution.getSystemContext().get(ExecutionConstants.EXECUTION_EVENTS_LOG_LEVEL);
        if (StringUtils.isNotEmpty(logLevelStr)) {
            LogLevel logLevel;
            try {
                logLevel = LogLevel.valueOf(logLevelStr);
            } catch (NullPointerException ex) {
                logLevel = LogLevel.INFO;
            }
            ExecutionLogLevelHolder.setExecutionLogLevel(logLevel);
        }
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
            Map<String, Object> stepData = new HashMap<>(currStep.getActionData());
            //We add all the contexts to the step data - so inside of each control action we will have access to all contexts
            addContextData(stepData, execution);

            // put in Queue the ExecutionEvent
            ArrayDeque<ExecutionEvent> eventsQueue = new ArrayDeque<>();
            execution.getSystemContext().put(ExecutionConstants.EXECUTION_EVENTS_QUEUE, eventsQueue);

            //now we run the exe step
            reflectionAdapter.executeControlAction(currStep.getAction(), stepData);
        } catch (RuntimeException ex) {
            logger.error("Error occurred during operation execution.  Execution id: " + execution.getExecutionId(), ex);
            execution.getSystemContext().put(ExecutionConstants.EXECUTION_STEP_ERROR_KEY, ex.getMessage());

            try {
                ExecutionEvent executionEvent = createErrorEvent(execution, currStep, ex, "Error occurred during operation execution", LogLevelCategory.STEP_OPER_ERROR, execution.getSystemContext());
                @SuppressWarnings("unchecked") Deque<ExecutionEvent> eventsQueue = (Deque<ExecutionEvent>) execution.getSystemContext().get(ExecutionConstants.EXECUTION_EVENTS_QUEUE);
                eventsQueue.add(executionEvent);

            } catch (RuntimeException eventEx) {
                logger.error("Failed to create event: ", eventEx);
            }
        }
    }

    protected List<StartBranchDataContainer> executeSplitStep(Execution execution, ExecutionStep currStep) {
        try {
            Map<String, Object> stepData = new HashMap<>(currStep.getActionData());
            //We add all the contexts to the step data - so inside of each control action we will have access to all contexts
            addContextData(stepData, execution);

            // put in Queue the ExecutionEvent
            ArrayDeque<ExecutionEvent> eventsQueue = new ArrayDeque<>();
            execution.getSystemContext().put(ExecutionConstants.EXECUTION_EVENTS_QUEUE, eventsQueue);

            //now we run the exe step
	        //noinspection unchecked
	        return (List<StartBranchDataContainer>) reflectionAdapter.executeControlAction(currStep.getAction(), stepData);
        } catch (RuntimeException ex) {
            logger.error("Error occurred during operation execution.  Execution id: " + execution.getExecutionId(), ex);
            execution.getSystemContext().put(ExecutionConstants.EXECUTION_STEP_ERROR_KEY, ex.getMessage());

            try {
                ExecutionEvent executionEvent = createErrorEvent(execution, currStep, ex, "Error occurred during operation execution", LogLevelCategory.STEP_OPER_ERROR, execution.getSystemContext());
                @SuppressWarnings("unchecked") Deque<ExecutionEvent> eventsQueue = (Deque<ExecutionEvent>) execution.getSystemContext().get(ExecutionConstants.EXECUTION_EVENTS_QUEUE);
                eventsQueue.add(executionEvent);

            } catch (RuntimeException eventEx) {
                logger.error("Failed to create event: ", eventEx);
            }

            throw ex;
        }
    }

    private ExecutionEvent createErrorEvent(Execution execution, ExecutionStep currStep, RuntimeException ex, String logMessage, LogLevelCategory logLevelCategory, Map<String, Serializable> systemContext) {
        Map<String, String> map = new HashMap<>();
        map.put("error_message", ex.getMessage());
        OOContext stepInputForEvent = new OOContext();
        stepInputForEvent.put("error_message", map.get("error_message"), false);

        String stepId = currStep.getExecStepId().toString();
        if (isDebuggerMode(execution.getSystemContext())) {
            return ExecutionEventFactory.createDebuggerErrorEvent(execution.getExecutionId(), stepId, logMessage, LogLevel.ERROR,
                    logLevelCategory, stepInputForEvent, ExecutionEventUtils.increaseEvent(systemContext), systemContext);
        } else {
            return ExecutionEventFactory.createLogEvent(execution.getExecutionId(), stepId, logMessage, LogLevel.ERROR,
                logLevelCategory, stepInputForEvent, ExecutionEventUtils.increaseEvent(systemContext), systemContext);
        }
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
                ExecutionEvent executionEvent = createErrorEvent(execution, currStep, navEx, "Error occurred during navigation execution ", LogLevelCategory.STEP_NAV_ERROR, execution.getSystemContext());
                @SuppressWarnings("unchecked") Deque<ExecutionEvent> eventsQueue = (Deque) execution.getSystemContext().get(ExecutionConstants.EXECUTION_EVENTS_QUEUE);
                eventsQueue.add(executionEvent);

                ExecutionEvent stepLogEvent = ExecutionEventFactory.createStepLogEvent(execution.getExecutionId(),ExecutionEventUtils.increaseEvent(execution.getSystemContext()),
                        ExecutionEnums.StepLogCategory.STEP_ERROR, execution.getSystemContext());
                eventsQueue.add(stepLogEvent);

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
        data.put(ExecutionConstants.SYSTEM_CONTEXT, execution.getSystemContext());
        data.put(ExecutionConstants.SERIALIZABLE_SESSION_CONTEXT, execution.getSerializableSessionContext());
        data.put(ExecutionConstants.EXECUTION, execution);
        data.put(ExecutionConstants.RUNNING_EXECUTION_PLAN_ID, execution.getRunningExecutionPlanId());
    }
}
