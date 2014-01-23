package com.hp.score;

import com.hp.oo.broker.entities.RunningExecutionPlan;
import com.hp.oo.broker.services.RunningExecutionPlanService;
import com.hp.oo.engine.node.entities.WorkerNode;
import com.hp.oo.engine.queue.entities.ExecStatus;
import com.hp.oo.engine.queue.entities.ExecutionMessage;
import com.hp.oo.engine.queue.entities.ExecutionMessageConverter;
import com.hp.oo.engine.queue.entities.Payload;
import com.hp.oo.engine.queue.services.QueueDispatcherService;
import com.hp.oo.internal.sdk.execution.Execution;
import com.hp.oo.internal.sdk.execution.ExecutionPlan;
import com.hp.score.engine.data.IdentityGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: wahnonm
 * Date: 21/01/14
 * Time: 17:39
 */
public class ScoreImpl implements Score {

    @Autowired
    private RunningExecutionPlanService runningExecutionPlanService;


    @Autowired
    private IdentityGenerator idGenerator;


    @Autowired
    private QueueDispatcherService queueDispatcher;

    @Autowired
    private ExecutionMessageConverter executionMessageConverter;

    @Override
    public Long trigger(ExecutionPlan executionPlan) {
        return trigger(executionPlan,new HashMap<String,Serializable>(),new HashMap<String,Serializable>(),executionPlan.getBeginStep());
    }

    @Override
    public Long trigger(ExecutionPlan executionPlan, Map<String, Serializable> context, Map<String, Serializable> systemContext, Long startStep) {
        Long runningExecutionPlanId = getOrCreateRunningExecutionPlan(executionPlan);
        Long executionId = (Long)idGenerator.next();
        Execution execution = new Execution(executionId, runningExecutionPlanId, startStep,context, systemContext);

        // create execution message
        ExecutionMessage message = createExecutionMessage(execution);
        enqueue(message);
        return executionId;
    }

    @Override
    public void pauseExecution(Long executionId) {
        //TODO - impl this
}

    @Override
    public void resumeExecution(Long executionId) {
         //TODO - impl this
    }

    @Override
    public void cancelExecution(Long executionId) {
         //TODO - impl this
    }

    private Long getOrCreateRunningExecutionPlan(ExecutionPlan executionPlan) {//TODO - move this to RunningExecutionPlan Service
        List<RunningExecutionPlan> existingRunningPlans = runningExecutionPlanService.readByFlowId(executionPlan.getFlowUuid());

        //If no running execution plan existsByUuid for this UUID - create new
        if (CollectionUtils.isEmpty(existingRunningPlans)) {
            return createNewRunningExecutionPlan(executionPlan);
        }
        //If existsByUuid - check if the plans are equal
        else {
            for (RunningExecutionPlan existingRunningPlan : existingRunningPlans) {
                if (existingRunningPlan.getExecutionPlan().equals(executionPlan)) {
                    return existingRunningPlan.getId();
                }
            }
            return createNewRunningExecutionPlan(executionPlan);
        }
    }

    private Long createNewRunningExecutionPlan(ExecutionPlan executionPlan) {   //TODO - move this to RunningExecutionPlan Service
        //Create new and save in DB
        RunningExecutionPlan runningExecutionPlan = new RunningExecutionPlan();
        runningExecutionPlan.setFlowUUID(executionPlan.getFlowUuid());
        runningExecutionPlan.setExecutionPlan(executionPlan);
        runningExecutionPlan = runningExecutionPlanService.createRunningExecutionPlan(runningExecutionPlan);

        return runningExecutionPlan.getId();
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
