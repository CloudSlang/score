/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package org.openscore.orchestrator.services;

import org.openscore.api.Score;
import org.openscore.api.TriggeringProperties;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
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

    @Autowired
    private CancelExecutionService cancelExecutionService;

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
        cancelExecutionService.requestCancelExecution(executionId);
    }

}
