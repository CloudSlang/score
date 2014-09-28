package com.hp.score.engine.queue.services;

import com.hp.score.facade.entities.Execution;
import com.hp.score.events.ScoreEvent;

/**
 * User: maromg
 * Date: 30/07/2014
 */
//TODO: Add Javadoc
//TODO: move to api module
public interface ScoreEventFactory {

	public ScoreEvent createFinishedEvent(Execution execution);

	public ScoreEvent createFailedBranchEvent(Execution execution);

	public ScoreEvent createFailureEvent(Execution execution);

	public ScoreEvent createNoWorkerEvent(Execution execution, Long pauseId);

    public ScoreEvent createFinishedBranchEvent(Execution execution);
}
