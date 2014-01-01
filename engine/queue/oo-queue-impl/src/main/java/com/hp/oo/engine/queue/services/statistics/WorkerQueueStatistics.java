package com.hp.oo.engine.queue.services.statistics;

/**
 * User: Amit Levin
 * Date: 10/09/12
 * Time: 09:53
 */
public interface WorkerQueueStatistics {

	public long getNumOfEvents(String workerId);

	public long getFinalCounter();

	public long getFinishedCounter();
}
