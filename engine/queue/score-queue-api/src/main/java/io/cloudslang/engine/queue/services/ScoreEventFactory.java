/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package io.cloudslang.engine.queue.services;

import io.cloudslang.facade.entities.Execution;
import io.cloudslang.events.ScoreEvent;

/**
 * User:
 * Date: 30/07/2014
 *
 * A factory to create {@link io.cloudslang.events.ScoreEvent}
 *
 */
public interface ScoreEventFactory {

    /**
     *
     * Creates a {@link io.cloudslang.events.ScoreEvent}
     * for finished execution state
     *
     * @param execution the execution to create the event from
     * @return {@link io.cloudslang.events.ScoreEvent} of the finished state
     */
	public ScoreEvent createFinishedEvent(Execution execution);

    /**
     *
     * Creates a {@link io.cloudslang.events.ScoreEvent}
     * for failed branch execution state
     *
     * @param execution the execution to create the event from
     * @return {@link io.cloudslang.events.ScoreEvent} of the failed branch state
     */
	public ScoreEvent createFailedBranchEvent(Execution execution);

    /**
     *
     * Creates a {@link io.cloudslang.events.ScoreEvent}
     * for failure execution state
     *
     * @param execution the execution to create the event from
     * @return {@link io.cloudslang.events.ScoreEvent} of the failure state
     */
	public ScoreEvent createFailureEvent(Execution execution);

    /**
     *
     * Creates a {@link io.cloudslang.events.ScoreEvent}
     * for no worker execution state
     *
     * @param execution the execution to create the event from
     * @return {@link io.cloudslang.events.ScoreEvent} of the no worker state
     */
	public ScoreEvent createNoWorkerEvent(Execution execution, Long pauseId);

    /**
     *
     * Creates a {@link io.cloudslang.events.ScoreEvent}
     * for finished branch execution state
     *
     * @param execution the execution to create the event from
     * @return {@link io.cloudslang.events.ScoreEvent} of the finished branch state
     */
    public ScoreEvent createFinishedBranchEvent(Execution execution);
}
