/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package org.eclipse.score.engine.queue.services;

import org.eclipse.score.facade.entities.Execution;
import org.eclipse.score.events.ScoreEvent;

/**
 * User:
 * Date: 30/07/2014
 */
//TODO: Add Javadoc
public interface ScoreEventFactory {

	public ScoreEvent createFinishedEvent(Execution execution);

	public ScoreEvent createFailedBranchEvent(Execution execution);

	public ScoreEvent createFailureEvent(Execution execution);

	public ScoreEvent createNoWorkerEvent(Execution execution, Long pauseId);

    public ScoreEvent createFinishedBranchEvent(Execution execution);
}
