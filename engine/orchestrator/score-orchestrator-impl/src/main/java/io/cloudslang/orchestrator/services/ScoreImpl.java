/*
 * Copyright © 2014-2017 EntIT Software LLC, a Micro Focus company (L.P.)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.cloudslang.orchestrator.services;

import io.cloudslang.score.api.Score;
import io.cloudslang.score.api.TriggeringProperties;
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
