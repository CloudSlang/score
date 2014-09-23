package com.hp.score.orchestrator.services;

import com.hp.score.api.TriggeringProperties;

/**
 * User: wahnonm
 * Date: 27/01/14
 * Time: 11:20
 */
interface ScoreTriggering {

    /**
     * Trigger a flow by score & return the execution ID
     *
     * @param triggeringProperties object holding all the properties needed for the trigger
     * @return the execution ID
     */
    Long trigger(TriggeringProperties triggeringProperties);

    /**
     * Trigger a flow by score & return the execution ID
     *
     * @param executionId the execution ID we want to assign to the triggered execution
     * @param triggeringProperties object holding all the properties needed for the trigger
     * @return the execution ID
     */
    Long trigger(Long executionId, TriggeringProperties triggeringProperties);
}
