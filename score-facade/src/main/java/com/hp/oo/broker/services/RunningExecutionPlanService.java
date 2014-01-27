package com.hp.oo.broker.services;

import com.hp.oo.broker.entities.RunningExecutionPlan;
import com.hp.oo.internal.sdk.execution.ExecutionPlan;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: lernery
 * Date: 11/24/11
 * Time: 10:54 AM
 */
public interface RunningExecutionPlanService {
	RunningExecutionPlan createRunningExecutionPlan(RunningExecutionPlan runningExecutionPlan);
	RunningExecutionPlan readExecutionPlanById(Long id);
	Long saveExecutionPlan(RunningExecutionPlan runningExecutionPlan);
	byte[] getZippedExecutionPlan(Long id);
	List<RunningExecutionPlan> readByFlowId(String flowId);
    Long getOrCreateRunningExecutionPlan(ExecutionPlan executionPlan);
}
