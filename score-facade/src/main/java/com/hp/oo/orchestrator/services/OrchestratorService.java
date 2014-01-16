package com.hp.oo.orchestrator.services;

import com.hp.oo.enginefacade.execution.ExecutionEnums;
import com.hp.oo.internal.sdk.execution.Execution;
import com.hp.oo.internal.sdk.execution.ExecutionPlan;

import java.util.List;
import java.util.Map;

/**
 * Date: 10/17/12
 *
 * @author Dima Rassin
 */
public interface OrchestratorService {

    void triggerFlow(String flowUuid, String triggerType, String executionName, String flowPath, String flowInputsContextName, String triggeredBy, String triggeringSource, Execution execution, Map<String, String> executionConfiguration);

    Long getOrCreateRunningExecutionPlan(ExecutionPlan executionPlan);

    String getFlowUuidByRunningExecutionPlanId(Long runningExecutionPlanId);

    Execution createExecution(Long runningExecutionPlanId, Long position, List<String> contextsNames, ExecutionEnums.LogLevel logLevel);
}
