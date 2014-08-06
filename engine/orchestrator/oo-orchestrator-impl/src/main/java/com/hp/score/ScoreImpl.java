package com.hp.score;

import com.hp.score.api.ExecutionPlan;
import com.hp.score.api.Score;
import com.hp.score.api.TriggeringProperties;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * User: wahnonm
 * Date: 21/01/14
 * Time: 17:39
 */
public class ScoreImpl implements Score {

    @Autowired
    private ScoreTriggering scoreTriggering;

    @Autowired
    private ScorePauseResume scorePauseResume;

    @Override
    public Long trigger(TriggeringProperties triggeringProperties) {
        return scoreTriggering.trigger(triggeringProperties);
    }

    @Override
    public boolean pauseExecution(Long executionId) {
        return scorePauseResume.pauseExecution(executionId);
    }

    @Override
    public void resumeExecution(Long executionId, Map<String, Serializable> context, Map<String, Serializable> runtimeValues) {
        scorePauseResume.resumeExecution(executionId, context, runtimeValues);
    }

    @Override
    public void cancelExecution(Long executionId) {
        //TODO - impl this
    }

}
