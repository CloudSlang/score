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

import io.cloudslang.score.api.TriggeringProperties;

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
