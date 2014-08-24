package com.hp.oo.engine.queue.services.statistics;

import com.hp.oo.engine.queue.entities.ExecStatus;
import com.hp.oo.engine.queue.entities.ExecutionMessage;
import com.hp.oo.engine.queue.services.QueueListener;

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
