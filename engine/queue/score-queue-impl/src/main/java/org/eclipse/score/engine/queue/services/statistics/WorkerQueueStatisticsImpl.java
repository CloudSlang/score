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
package org.eclipse.score.engine.queue.services.statistics;

import org.eclipse.score.engine.queue.entities.ExecStatus;
import org.eclipse.score.engine.queue.entities.ExecutionMessage;
import org.eclipse.score.engine.queue.services.QueueListener;

import java.util.List;

/**
 * User:
 * Date: 10/09/12
 * Time: 09:57
 */
@SuppressWarnings("unused")
public class WorkerQueueStatisticsImpl implements WorkerQueueStatistics , QueueListener {

	private long eventsCount;
	private long finishedCounter = 0;
	private long finalCounter = 0;

	public WorkerQueueStatisticsImpl(){
		eventsCount = 0;
	    finishedCounter = 0;
	    finalCounter = 0;
	}

	public WorkerQueueStatisticsImpl(long eventsCount){
		this.eventsCount =  eventsCount;
	}
	
	@Override
	public long getNumOfEvents(String workerId) {
		return eventsCount;
	}

	@Override
	public long getFinalCounter(){
		return finalCounter;
	}

	@Override
	public long getFinishedCounter(){
		return finishedCounter;
	}

	@Override
	public void onEnqueue(List<ExecutionMessage> messages, int queueSize) {
		for(ExecutionMessage msg: messages) {
			if (msg.getStatus().equals(ExecStatus.FINISHED))
				finishedCounter = finishedCounter + 1;
			else if (msg.getStatus().equals(ExecStatus.TERMINATED))
				finalCounter = finalCounter + 1;
 		}
	}

	@Override
	public void onPoll(List<ExecutionMessage> messages, int queueSize) {
	}

	@Override
	public void onTerminated(List<ExecutionMessage> messages) {
	}

	@Override
	public void onFailed(List<ExecutionMessage> messages) {
	}
}
