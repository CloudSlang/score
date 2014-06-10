package com.hp.score;

import com.hp.oo.internal.sdk.execution.ExecutionPlan;

import java.io.Serializable;
import java.util.Map;

/**
 * User: wahnonm
 * Date: 27/01/14
 * Time: 11:20
 */
interface ScoreTriggering {
    Long trigger(ExecutionPlan executionPlan, Map<String, Serializable> context, Map<String, Serializable> runtimeValues, Long startStep);
}
