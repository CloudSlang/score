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
import com.hp.score.api.ExecutionPlan;
import com.hp.score.engine.data.IdentityGenerator;
import com.hp.score.lang.SystemContext;
import com.hp.score.services.ExecutionStateService;
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
    private ExecutionStateService executionStateService;

    @Override
    public Long trigger(ExecutionPlan executionPlan, Map<String, ? extends Serializable> context, Map<String, ? extends Serializable> runtimeValues, Long startStep) {
        SystemContext scoreSystemContext = new SystemContext(runtimeValues);
        Long runningExecutionPlanId = saveRunningExecutionPlan(executionPlan, scoreSystemContext);
        Long executionId = (Long) idGenerator.next();
        scoreSystemContext.put(ExecutionConstants.EXECUTION_ID_CONTEXT, executionId);
        Execution execution = new Execution(executionId, runningExecutionPlanId, startStep, context, scoreSystemContext);

        // create execution record in ExecutionSummary table
        executionStateService.createParentExecution(execution.getExecutionId());

        // create execution message
        ExecutionMessage message = createExecutionMessage(execution);
        enqueue(message);
        return executionId;
    }

    private Long saveRunningExecutionPlan(ExecutionPlan executionPlan, SystemContext systemContext) {
        Map<String, Long> runningPlansIds = new HashMap<>();
        Map<String, Long> beginStepsIds = new HashMap<>();

        for (ExecutionPlan dependencyExecutionPlan : executionPlan.getDependencies().values()) {
            String subFlowUuid = dependencyExecutionPlan.getFlowUuid();
            Long subFlowRunningId = runningExecutionPlanService.getOrCreateRunningExecutionPlan(dependencyExecutionPlan);
            runningPlansIds.put(subFlowUuid, subFlowRunningId);
            beginStepsIds.put(subFlowUuid, dependencyExecutionPlan.getBeginStep());
        }

        // Adding the ids of the running execution plan of the parent + its begin step
        // since this map should contain all the ids of the running plans
        Long runningPlanId =  runningExecutionPlanService.getOrCreateRunningExecutionPlan(executionPlan);
        runningPlansIds.put(executionPlan.getFlowUuid(), runningPlanId);
        beginStepsIds.put(executionPlan.getFlowUuid(), executionPlan.getBeginStep());

        systemContext.put(ExecutionConstants.RUNNING_PLANS_MAP, (Serializable) runningPlansIds);
        systemContext.put(ExecutionConstants.BEGIN_STEPS_MAP, (Serializable) beginStepsIds);

        return runningPlanId;
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
