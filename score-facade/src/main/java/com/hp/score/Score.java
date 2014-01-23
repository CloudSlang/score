package com.hp.score;

import com.hp.oo.internal.sdk.execution.ExecutionPlan;

import java.io.Serializable;
import java.util.Map;

/**
 * User: wahnonm
 * Date: 20/01/14
 * Time: 17:09
 */
public interface Score {

    public Long trigger(ExecutionPlan executionPlan);

    public Long trigger(ExecutionPlan executionPlan, Map<String,Serializable> context, Map<String,Serializable> systemContext, Long startStep);

    public void pauseExecution(Long executionId);

    public void resumeExecution(Long executionId);

    public void cancelExecution(Long executionId);

}
