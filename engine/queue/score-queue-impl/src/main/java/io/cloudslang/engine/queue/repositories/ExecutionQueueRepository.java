/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package io.cloudslang.engine.queue.repositories;

import io.cloudslang.engine.queue.entities.ExecStatus;
import io.cloudslang.engine.queue.entities.ExecutionMessage;
import io.cloudslang.engine.queue.entities.Payload;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * User:
 * Date: 20/09/12
 * Time: 15:03
 */
public interface ExecutionQueueRepository {

    List<ExecutionMessage> poll(Date createDate, String workerId, int maxSize, ExecStatus... statuses);

	List<ExecutionMessage> poll(String workerId, int maxSize, ExecStatus... statuses);

	List<ExecutionMessage> pollMessagesWithoutAck(int maxSize,long minVersionAllowed);

    Integer countMessagesWithoutAckForWorker(int maxSize, long minVersionAllowed, String workerUuid);

	long generateExecStateId();

	void insertExecutionStates(final List<ExecutionMessage> stateMessages);

	void insertExecutionQueue(final List<ExecutionMessage> messages,long version);

	Map<Long,Payload> findPayloadByExecutionIds(Long ... ids);

    void deleteFinishedSteps(Set<Long> ids);

    Set<Long> getFinishedExecStateIds();

	List<ExecutionMessage> findByStatuses(int maxSize, ExecStatus... statuses);

	List<String> getBusyWorkers(ExecStatus... statuses);
}
