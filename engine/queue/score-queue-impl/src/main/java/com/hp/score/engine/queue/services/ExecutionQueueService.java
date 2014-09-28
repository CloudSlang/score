package com.hp.score.engine.queue.services;

import com.hp.score.engine.queue.entities.ExecStatus;
import com.hp.score.engine.queue.entities.ExecutionMessage;
import com.hp.score.engine.queue.entities.Payload;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * User: Amit Levin
 * Date: 10/09/12
 */
//TODO: Add Javadoc
//TODO: move to api module
public interface ExecutionQueueService {

	void enqueue(List<ExecutionMessage> messages);

    List<ExecutionMessage> poll(Date createDate, String workerId, int maxSize, ExecStatus... statuses);

	List<ExecutionMessage> poll(String workerId, int maxSize, ExecStatus... statuses);

	List<ExecutionMessage> pollMessagesWithoutAck(int maxSize,long minVersionAllowed);

	Map<Long,Payload> readPayloadByExecutionIds(Long... executionIds);

	List<ExecutionMessage> readMessagesByStatus(int maxSize, ExecStatus... statuses);

    int countMessagesWithoutAckForWorker(int maxSize,long minVersionAllowed, String workerUuid);
}
