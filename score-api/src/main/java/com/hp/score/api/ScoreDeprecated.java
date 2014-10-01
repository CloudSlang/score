package com.hp.score.api;

/**
 * Created by peerme on 23/07/2014.
 */
@Deprecated
public interface ScoreDeprecated {

    /***
     * for cases you need the executionId before triggering
     * this method generate executionId
     * @return  the executionId generated
     */
    public Long generateExecutionId();

    /**
     * for cases you need the executionId before triggering
     * trigger run with pre-generated executionId (by using generateExecutionId() method...)
     * @param executionId  - the executionId for the run
     * @param triggeringProperties   object holding all the properties needed for the trigger
     * @return the give executionId
     */
    public Long trigger(Long executionId, TriggeringProperties triggeringProperties);
}
