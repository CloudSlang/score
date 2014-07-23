package com.hp.score.api;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by peerme on 23/07/2014.
 */
@Deprecated
public interface ScoreDeprecated {

    public Long generateExecutionId();

    public Long trigger(Long executionId, ExecutionPlan executionPlan, Map<String, ? extends Serializable> context, Map<String, ? extends Serializable> runtimeValues, Long startStep);

}
