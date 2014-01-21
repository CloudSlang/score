package com.hp.score;

import com.hp.oo.internal.sdk.execution.ExecutionPlan;

import java.io.Serializable;
import java.util.Map;

/**
 * User: wahnonm
 * Date: 20/01/14
 * Time: 17:09
 */
public interface ScoreManager {

    Long trigger(ExecutionPlan executionPlan);

    Long trigger(ExecutionPlan executionPlan, Map<String,Serializable> context, Map<String,Serializable> systemContext, Long startStep);

    Boolean pauseExecution(Long executionId);

    Boolean resumeExecution(Long executionId);

    Boolean cancelExecution(Long executionId);

}
