/*
 * Licensed to Hewlett-Packard Development Company, L.P. under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
*/
package com.hp.score.engine.queue.services.statistics;

import com.hp.score.engine.queue.entities.ExecStatus;
import com.hp.score.engine.queue.entities.ExecutionMessage;
import com.hp.score.engine.queue.services.QueueListener;

import java.util.List;

/**
 * User: Amit Levin
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
