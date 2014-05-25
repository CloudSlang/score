package com.hp.score;

import com.hp.oo.broker.services.RunningExecutionPlanService;
import com.hp.oo.engine.node.entities.WorkerNode;
import com.hp.oo.engine.queue.entities.ExecStatus;
import com.hp.oo.engine.queue.entities.ExecutionMessage;
import com.hp.oo.engine.queue.entities.ExecutionMessageConverter;
import com.hp.oo.engine.queue.entities.Payload;
import com.hp.oo.engine.queue.services.QueueDispatcherService;
import com.hp.oo.internal.sdk.execution.Execution;
import com.hp.oo.internal.sdk.execution.ExecutionConstants;
import com.hp.oo.internal.sdk.execution.ExecutionPlan;
import com.hp.score.engine.data.IdentityGenerator;
import com.hp.score.services.RunStateService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * User: wahnonm
 * Date: 30/01/14
 * Time: 14:19
 */
public class ScoreTriggeringImpl implements ScoreTriggering {

    @Autowired
    private RunningExecutionPlanService runningExecutionPlanService;

    @Autowired
    private IdentityGenerator idGenerator;

    @Autowired
    private QueueDispatcherService queueDispatcher;

    @Autowired
    private ExecutionMessageConverter executionMessageConverter;

    @Autowired
    private RunStateService runStateService;

    @Override
    public Long trigger(ExecutionPlan executionPlan, Map<String, Serializable> context, Map<String, Serializable> systemContext, Long startStep) {
        Long runningExecutionPlanId = saveRunningExecutionPlan(executionPlan, systemContext);
        Long executionId = (Long) idGenerator.next();
        Execution execution = new Execution(executionId, runningExecutionPlanId, startStep, context, systemContext);

        // create execution record in ExecutionSummary table
        runStateService.createParentRun(execution.getExecutionId());

        // create execution message
        ExecutionMessage message = createExecutionMessage(execution);
        enqueue(message);
        return executionId;
    }

    private Long saveRunningExecutionPlan(ExecutionPlan executionPlan, Map<String, Serializable> systemContext) {
        Map<String, Long> runningPlansIds = new HashMap<>();
        Map<String, Long> beginStepsIds = new HashMap<>();

        for (ExecutionPlan dependencyExecutionPlan : executionPlan.getDependencies().values()) {
            String subFlowUuid = dependencyExecutionPlan.getFlowUuid();
            Long subFlowRunningId = runningExecutionPlanService.getOrCreateRunningExecutionPlan(dependencyExecutionPlan);
            runningPlansIds.put(subFlowUuid, subFlowRunningId);
            beginStepsIds.put(subFlowUuid, executionPlan.getBeginStep());
        }
        systemContext.put(ExecutionConstants.RUNNING_PLANS_MAP, (Serializable) runningPlansIds);
        systemContext.put(ExecutionConstants.BEGIN_STEPS_MAP, (Serializable) beginStepsIds);

        return runningExecutionPlanService.getOrCreateRunningExecutionPlan(executionPlan);
    }

    private void enqueue(ExecutionMessage... messages) {
        queueDispatcher.dispatch(Arrays.asList(messages));
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
}
