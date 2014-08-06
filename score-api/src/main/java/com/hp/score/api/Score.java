package com.hp.score.api;

import java.io.Serializable;
import java.util.Map;

/**
 * User: wahnonm
 * Date: 20/01/14
 * Time: 17:09
 */
public interface Score {

    public Long trigger(TriggeringProperties triggeringProperties);

    public boolean pauseExecution(Long executionId);

    public void resumeExecution(Long executionId, Map<String, Serializable> context, Map<String, Serializable> runtimeValues);

    public void cancelExecution(Long executionId);

}
