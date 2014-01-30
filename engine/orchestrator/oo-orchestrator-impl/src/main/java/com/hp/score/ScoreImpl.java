package com.hp.score;

import com.hp.oo.internal.sdk.execution.ExecutionPlan;
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

    @Override
    public Long trigger(ExecutionPlan executionPlan) {
        return trigger(executionPlan,new HashMap<String,Serializable>(),new HashMap<String,Serializable>(),executionPlan.getBeginStep());
    }

    @Override
    public Long trigger(ExecutionPlan executionPlan, Map<String, Serializable> context, Map<String, Serializable> systemContext, Long startStep) {
        return scoreTriggering.trigger(executionPlan,context,systemContext,startStep);
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
}
