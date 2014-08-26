package com.hp.score.engine.queue.services;

import com.hp.score.engine.queue.entities.ExecutionMessage;

import java.util.List;

/**
 * User: Amit Levin
 * Date: 19/09/12
 * Time: 15:08
 */
public interface QueueListener {

	void onEnqueue(List<ExecutionMessage> messages,int queueSize);

	void onPoll(List<ExecutionMessage> messages,int queueSize);

	void onTerminated(List<ExecutionMessage> messages);

	void onFailed(List<ExecutionMessage> messages);
}
