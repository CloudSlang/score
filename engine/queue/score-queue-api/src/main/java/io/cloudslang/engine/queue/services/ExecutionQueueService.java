/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package io.cloudslang.engine.queue.services;

import io.cloudslang.engine.queue.entities.ExecStatus;
import io.cloudslang.engine.queue.entities.ExecutionMessage;
import io.cloudslang.engine.queue.entities.Payload;

import java.util.List;
import java.util.Map;

/**
 * User:
 * Date: 10/09/12
 *
 * Responsible for the Execution Queue
 *
 */
public interface ExecutionQueueService {

    /**
     *
     * enqueue messages to the queue
     *
     * @param messages the messages to enqueue
     */
	void enqueue(List<ExecutionMessage> messages);

    /**
     *
     * polls messages from the queue
     *
     * @param workerId the id of the worker
     * @param maxSize max size of the poll bulk
     * @param statuses requested messages statuses
     * @return a List of {@link io.cloudslang.engine.queue.entities.ExecutionMessage} requested
     */
    List<ExecutionMessage> poll(String workerId, int maxSize, ExecStatus... statuses);

    /**
     *
     * polls messages from the queue
     *
     * @param workerId the id of the worker
     * @param maxSize max size of the poll bulk
     * @param statuses requested messages statuses
     * @return a List of {@link io.cloudslang.engine.queue.entities.ExecutionMessage} requested
     */
	List<ExecutionMessage> pollRecovery(String workerId, int maxSize, ExecStatus... statuses);

    /**
     *
     * polls messages that didn't receive ack yet
     *
     * @param maxSize max size of the poll bulk
     * @param minVersionAllowed min version that the messages didn't send ack
     * @return a List of {@link io.cloudslang.engine.queue.entities.ExecutionMessage} requested
     */
	List<ExecutionMessage> pollMessagesWithoutAck(int maxSize,long minVersionAllowed);

    /**
     *
     * get the payloads for requested execution ids
     *
     * @param executionIds the execution ids to get payload for
     * @return a map of the execution id and its payload
     */
	Map<Long,Payload> readPayloadByExecutionIds(Long... executionIds);

    /**
     *
     * @param maxSize max size of the poll bulk
     * @param statuses the requested statuses of the messages
     * @return a List of {@link io.cloudslang.engine.queue.entities.ExecutionMessage} requested
     */
	List<ExecutionMessage> readMessagesByStatus(int maxSize, ExecStatus... statuses);

    /**
     *
     * polls the count of messages that didn't receive ack for a number of recovery versions
     *
     * @param maxSize max size of the poll bulk
     * @param minVersionAllowed min version that the messages didn't send ack
     * @param workerUuid the id of the associated worker
     * @return the number of messages that didn't receive ack for a number of recovery versions
     */
    int countMessagesWithoutAckForWorker(int maxSize,long minVersionAllowed, String workerUuid);
}
