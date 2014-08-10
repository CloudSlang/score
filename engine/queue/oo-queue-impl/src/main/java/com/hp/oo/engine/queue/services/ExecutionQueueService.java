package com.hp.oo.engine.queue.services;

import com.hp.oo.engine.queue.entities.ExecStatus;
import com.hp.oo.engine.queue.entities.ExecutionMessage;
import com.hp.oo.engine.queue.entities.Payload;
import com.hp.oo.engine.queue.services.statistics.WorkerQueueStatistics;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * User: Amit Levin
 * Date: 10/09/12
 */
public interface ExecutionQueueService {

	void enqueue(List<ExecutionMessage> messages);

    List<ExecutionMessage> poll(Date createDate, String workerId, int maxSize, ExecStatus... statuses);

	List<ExecutionMessage> poll(String workerId, int maxSize, ExecStatus... statuses);

	List<ExecutionMessage> pollMessagesWithoutAck(int maxSize,long minVersionAllowed);

	WorkerQueueStatistics getNumOfEvents(ExecStatus status,String workerId);

	Map<Long,Payload> readPayloadByExecutionIds(Long... executionIds);

	List<ExecutionMessage> readMessagesByStatus(int maxSize, ExecStatus... statuses);

    int countMessagesWithoutAckForWorker(int maxSize,long minVersionAllowed, String workerUuid);
}
