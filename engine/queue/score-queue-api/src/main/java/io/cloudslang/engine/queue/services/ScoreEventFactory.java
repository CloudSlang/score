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

package io.cloudslang.engine.queue.services;

import io.cloudslang.score.facade.entities.Execution;
import io.cloudslang.score.events.ScoreEvent;

/**
 * User:
 * Date: 30/07/2014
 *
 * A factory to create {@link io.cloudslang.score.events.ScoreEvent}
 *
 */
public interface ScoreEventFactory {

    /**
     *
     * Creates a {@link io.cloudslang.score.events.ScoreEvent}
     * for finished execution state
     *
     * @param execution the execution to create the event from
     * @return {@link io.cloudslang.score.events.ScoreEvent} of the finished state
     */
	public ScoreEvent createFinishedEvent(Execution execution);

    /**
     *
     * Creates a {@link io.cloudslang.score.events.ScoreEvent}
     * for failed branch execution state
     *
     * @param execution the execution to create the event from
     * @return {@link io.cloudslang.score.events.ScoreEvent} of the failed branch state
     */
	public ScoreEvent createFailedBranchEvent(Execution execution);

    /**
     *
     * Creates a {@link io.cloudslang.score.events.ScoreEvent}
     * for failure execution state
     *
     * @param execution the execution to create the event from
     * @return {@link io.cloudslang.score.events.ScoreEvent} of the failure state
     */
	public ScoreEvent createFailureEvent(Execution execution);

    /**
     *
     * Creates a {@link io.cloudslang.score.events.ScoreEvent}
     * for no worker execution state
     *
     * @param execution the execution to create the event from
     * @return {@link io.cloudslang.score.events.ScoreEvent} of the no worker state
     */
	public ScoreEvent createNoWorkerEvent(Execution execution, Long pauseId);

    /**
     *
     * Creates a {@link io.cloudslang.score.events.ScoreEvent}
     * for finished branch execution state
     *
     * @param execution the execution to create the event from
     * @return {@link io.cloudslang.score.events.ScoreEvent} of the finished branch state
     */
    public ScoreEvent createFinishedBranchEvent(Execution execution);
}
