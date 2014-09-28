package com.hp.score.api;

/**
 * Created by peerme on 23/07/2014.
 */
//TODO: Add Javadoc
@Deprecated
public interface ScoreDeprecated {

    public Long generateExecutionId();

    public Long trigger(Long executionId, TriggeringProperties triggeringProperties);
}
