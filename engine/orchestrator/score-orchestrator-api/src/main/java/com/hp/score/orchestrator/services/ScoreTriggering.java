package com.hp.score.orchestrator.services;

import com.hp.score.api.TriggeringProperties;

/**
 * User: wahnonm
 * Date: 27/01/14
 * Time: 11:20
 */
interface ScoreTriggering {
    Long trigger(TriggeringProperties triggeringProperties);
    Long trigger(Long executionId, TriggeringProperties triggeringProperties);
}
