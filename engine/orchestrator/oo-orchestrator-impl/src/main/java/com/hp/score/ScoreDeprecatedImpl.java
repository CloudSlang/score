package com.hp.score;

import com.hp.score.api.ExecutionPlan;
import com.hp.score.api.ScoreDeprecated;
import com.hp.score.api.TriggeringProperties;
import com.hp.score.engine.data.IdentityGenerator;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by peerme on 23/07/2014.
 */
public class ScoreDeprecatedImpl implements ScoreDeprecated {

    @Autowired
    private ScoreTriggering scoreTriggering;

    @Autowired
    private IdentityGenerator idGenerator;

    @Override
    public Long generateExecutionId() {
        Long executionId = idGenerator.next();
        return executionId;
    }

    @Override
    public Long trigger(Long executionId, TriggeringProperties triggeringProperties) {
         return scoreTriggering.trigger(executionId, triggeringProperties);
    }

}
