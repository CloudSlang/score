package com.hp.oo.orchestrator.services;

import com.hp.oo.broker.entities.RunningExecutionPlan;
import com.hp.oo.broker.services.RunningExecutionPlanService;
import com.hp.oo.engine.node.entities.WorkerNode;
import com.hp.oo.engine.queue.entities.ExecStatus;
import com.hp.oo.engine.queue.entities.ExecutionMessage;
import com.hp.oo.engine.queue.entities.ExecutionMessageConverter;
import com.hp.oo.engine.queue.entities.Payload;
import com.hp.oo.engine.queue.services.QueueDispatcherService;
import com.hp.oo.enginefacade.execution.ExecutionEnums;
import com.hp.oo.internal.sdk.execution.Execution;
import com.hp.oo.internal.sdk.execution.ExecutionConstants;
import com.hp.score.api.ExecutionPlan;
import com.hp.score.engine.data.IdentityGenerator;
import com.hp.score.services.ExecutionStateService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Date: 10/17/12
 *
 * @author Dima Rassin
 */
public final class OrchestratorServiceImpl implements OrchestratorService {
    private final Logger logger = Logger.getLogger(getClass());
    private final ExecutionEnums.LogLevel DEFAULT_LOG_LEVEL = ExecutionEnums.LogLevel.INFO;

    @Autowired
    private RunningExecutionPlanService runningExecutionPlanService;

    @Autowired
    private QueueDispatcherService queueDispatcher;

    @Autowired
    private ExecutionMessageConverter executionMessageConverter;

    @Autowired
    private IdentityGenerator idGenerator;

    @Autowired
    private ExecutionStateService executionStateService;

    @Override
    @Transactional
    public void triggerFlow(String flowUuid, String triggerType, String executionName, String flowPath, String flowInputsContextName, String triggeredBy, String triggeringSource, Execution execution, Map<String, String> executionConfiguration) {
        // create execution record in ExecutionSummary table
        executionStateService.createParentExecution(execution.getExecutionId());

        // create execution message
        ExecutionMessage message = createExecutionMessage(execution);

        // send it to queue
        if (logger.isDebugEnabled()) {
            logger.debug("Orchestrator is triggering flow: " + flowUuid + ". ExecutionId: " + execution.getExecutionId());
        }
        enqueue(message);
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
        if (runningExecutionPlan == null)
            throw new RuntimeException("runningExecutionPlan is null");

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
        execution.setExecutionId(idGenerator.next());

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
