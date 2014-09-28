package com.hp.score.facade.services;

import com.hp.score.facade.entities.RunningExecutionPlan;
import com.hp.score.api.ExecutionPlan;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: lernery
 * Date: 11/24/11
 * Time: 10:54 AM
 */
//TODO: Add Javadoc
public interface RunningExecutionPlanService {
    RunningExecutionPlan createRunningExecutionPlan(RunningExecutionPlan runningExecutionPlan);

    RunningExecutionPlan readExecutionPlanById(Long id);

    Long saveExecutionPlan(RunningExecutionPlan runningExecutionPlan);

    byte[] getZippedExecutionPlan(Long id);

    List<RunningExecutionPlan> readByFlowId(String flowId);

    Long getOrCreateRunningExecutionPlan(ExecutionPlan executionPlan);

    String getFlowUuidByRunningExecutionPlanId(Long runningExecutionPlanId);
}
