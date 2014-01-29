package com.hp.oo.orchestrator.services;

import com.hp.oo.broker.entities.RunningExecutionPlan;
import com.hp.oo.broker.services.RunningExecutionPlanService;
import com.hp.oo.engine.execution.events.services.ExecutionEventService;
import com.hp.oo.engine.node.entities.WorkerNode;
import com.hp.oo.engine.queue.entities.ExecStatus;
import com.hp.oo.engine.queue.entities.ExecutionMessage;
import com.hp.oo.engine.queue.entities.ExecutionMessageConverter;
import com.hp.oo.engine.queue.entities.Payload;
import com.hp.oo.engine.queue.services.QueueDispatcherService;
import com.hp.oo.enginefacade.execution.ExecutionEnums;
import com.hp.oo.internal.sdk.execution.Execution;
import com.hp.oo.internal.sdk.execution.ExecutionConstants;
import com.hp.oo.internal.sdk.execution.ExecutionPlan;
import com.hp.oo.internal.sdk.execution.OOContext;
import com.hp.oo.internal.sdk.execution.events.ExecutionEvent;
import com.hp.oo.internal.sdk.execution.events.ExecutionEventFactory;
import com.hp.oo.internal.sdk.execution.events.ExecutionEventUtils;
import com.hp.score.engine.data.IdentityGenerator;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Date: 10/17/12
 *
 * @author Dima Rassin
 */
@SuppressWarnings("UnusedDeclaration")
@Service("orchestratorService")
public final class OrchestratorServiceImpl implements OrchestratorService {
    private final Logger logger = Logger.getLogger(getClass());
    private final ExecutionEnums.LogLevel DEFAULT_LOG_LEVEL = ExecutionEnums.LogLevel.INFO;

    @Autowired
    private RunningExecutionPlanService runningExecutionPlanService;

    @Autowired
    private ExecutionEventService executionEventService;

    @Autowired
    private ExecutionSummaryService executionSummaryService;

    @Autowired
    private QueueDispatcherService queueDispatcher;

    @Autowired
    private ExecutionMessageConverter executionMessageConverter;

    @Autowired
    private RunningExecutionConfigurationService executionConfigurationService;

    @Autowired
    private IdentityGenerator idGenerator;

    @Override
    @Transactional
    public void triggerFlow(String flowUuid, String triggerType, String executionName, String flowPath, String flowInputsContextName, String triggeredBy, String triggeringSource, Execution execution, Map<String, String> executionConfiguration) {
        ExecutionEventUtils.startFlow(execution.getSystemContext());
        OOContext flowInputsContext = (OOContext)execution.getContexts().get(flowInputsContextName); //get the flow context in generic way

        //TODO configure running execution configuration
        long versionNumber = executionConfigurationService.createRunningExecutionConfiguration(executionConfiguration);
        execution.getSystemContext().put(ExecutionConstants.EXECUTION_CONFIGURATION_VERSION, versionNumber);
        //TODO after moving all execution events creating and dispatching into score this should also moved into score
        execution.getSystemContext().put(ExecutionConstants.EXECUTION_EVENTS_STEP_MAPPED, new HashMap<String,List>());

        // send execution event and add the execution to the queue
        ExecutionEvent startEvent = sendExecutionEvent(flowUuid, triggerType, executionName, execution.getExecutionId(), flowInputsContext, execution.getSystemContext());

        // create execution record in ExecutionSummary table
        executionSummaryService.createExecution(execution.getExecutionId(), null, startEvent.getPublishTime(), ExecutionEnums.ExecutionStatus.RUNNING, executionName, flowUuid, flowPath, triggeredBy, triggeringSource);

        // create execution message
        ExecutionMessage message = createExecutionMessage(execution);

        // send it to queue
        if (logger.isDebugEnabled()) {
            logger.debug("Orchestrator is triggering flow: " + flowUuid + ". ExecutionId: " + execution.getExecutionId());
        }
        enqueue(message);
    }

    // returns the created START event
    private ExecutionEvent sendExecutionEvent(String uuid, String triggerType, String executionName, String executionId, OOContext context, Map<String, Serializable> systemContext) {
        logger.debug("Create start execution event for " + uuid);

        String logLevelStr = (String) systemContext.get(ExecutionConstants.EXECUTION_EVENTS_LOG_LEVEL);

        // send "Start execution" events
        List<ExecutionEvent> events = new ArrayList<>();
        ExecutionEvent startEvent = ExecutionEventFactory.createStartEvent(executionId, uuid, triggerType, executionName, logLevelStr, ExecutionEventUtils.increaseEvent(systemContext), systemContext);
        events.add(startEvent);
        for (Map.Entry<String, String> contextEntry : context.entrySet()) {
            if (context.getEncryptedSet().contains(contextEntry.getKey())) {
                // add this input as encrypted
                events.add(ExecutionEventFactory.createFlowInputEvent(executionId, contextEntry.getKey(), OOContext.ENCRYPTED_VALUE, ExecutionEventUtils.increaseEvent(systemContext),systemContext));
            } else {
                events.add(ExecutionEventFactory.createFlowInputEvent(executionId, contextEntry.getKey(), contextEntry.getValue(), ExecutionEventUtils.increaseEvent(systemContext),systemContext));
            }
        }
        if (logger.isDebugEnabled()) logger.debug("send start execution event for " + uuid);
        executionEventService.createEvents(events);

        return startEvent;
    }

    private ExecutionMessage createExecutionMessage(Execution execution) {
        Payload payload = executionMessageConverter.createPayload(execution);

        return new ExecutionMessage(ExecutionMessage.EMPTY_EXEC_STATE_ID,
                ExecutionMessage.EMPTY_WORKER,
                WorkerNode.DEFAULT_WORKER_GROUPS[0],
                String.valueOf(execution.getExecutionId()),
                ExecStatus.PENDING, //start new flow also in PENDING
                payload,
                0);
    }

    @Override
    @Transactional(readOnly = true)
    public String getFlowUuidByRunningExecutionPlanId(Long runningExecutionPlanId) {
        RunningExecutionPlan runningExecutionPlan = runningExecutionPlanService.readExecutionPlanById(runningExecutionPlanId);
        if (runningExecutionPlan == null) throw new RuntimeException("runningExecutionPlan is null");

        return runningExecutionPlan.getFlowUUID();
    }

    @Override
    @Transactional
    public Long getOrCreateRunningExecutionPlan(ExecutionPlan executionPlan) {
        return runningExecutionPlanService.getOrCreateRunningExecutionPlan(executionPlan);
    }

    @Override
    @Transactional
    public Execution createExecution(Long runningExecutionPlanId, Long position, List<String> contextsNames, ExecutionEnums.LogLevel logLevel) {
        Execution execution = new Execution(runningExecutionPlanId, position, contextsNames);
        execution.setExecutionId(String.valueOf(idGenerator.next()));

        // calculation of the execution log level, this is NOT a log4j log level,
        // but for "Log Events" that will be created during the execution itself
        ExecutionEnums.LogLevel computedLogLevel = (logLevel == null) ? DEFAULT_LOG_LEVEL : logLevel;
        execution.getSystemContext().put(ExecutionConstants.EXECUTION_EVENTS_LOG_LEVEL, computedLogLevel.name());

        return execution;
    }

    private void enqueue(ExecutionMessage... messages) {
        queueDispatcher.dispatch(Arrays.asList(messages));
    }
}
