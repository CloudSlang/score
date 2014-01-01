package com.hp.oo.engine.queue.repositories;

import com.hp.oo.engine.queue.entities.ExecStatus;
import com.hp.oo.engine.queue.entities.ExecutionMessage;
import com.hp.oo.engine.queue.entities.Payload;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * User: Amit Levin
 * Date: 20/09/12
 * Time: 15:03
 */
public interface ExecutionQueueRepository {

    List<ExecutionMessage> poll(Date createDate, String workerId, int maxSize, ExecStatus... statuses);

	List<ExecutionMessage> poll(String workerId, int maxSize, ExecStatus... statuses);

	List<ExecutionMessage> pollMessagesWithoutAck(int maxSize,long minVersionAllowed);

	long generateExecStateId();

	void insertExecutionStates(final List<ExecutionMessage> stateMessages);

	void insertExecutionQueue(final List<ExecutionMessage> messages,long version);

	Map<Long,Payload> findPayloadByExecutionIds(Long ... ids);

    void deleteFinishedSteps(Set<Long> ids);

    Set<Long> getFinishedExecStateIds();

	List<ExecutionMessage> findByStatuses(int maxSize, ExecStatus... statuses);
}
