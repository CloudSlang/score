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
package org.eclipse.score.engine.queue.services;

import org.eclipse.score.engine.queue.entities.ExecStatus;
import org.eclipse.score.engine.queue.entities.ExecutionMessage;
import org.eclipse.score.engine.queue.entities.Payload;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * User:
 * Date: 10/09/12
 */
//TODO: Add Javadoc
public interface ExecutionQueueService {

	void enqueue(List<ExecutionMessage> messages);

    List<ExecutionMessage> poll(Date createDate, String workerId, int maxSize, ExecStatus... statuses);

	List<ExecutionMessage> poll(String workerId, int maxSize, ExecStatus... statuses);

	List<ExecutionMessage> pollMessagesWithoutAck(int maxSize,long minVersionAllowed);

	Map<Long,Payload> readPayloadByExecutionIds(Long... executionIds);

	List<ExecutionMessage> readMessagesByStatus(int maxSize, ExecStatus... statuses);

    int countMessagesWithoutAckForWorker(int maxSize,long minVersionAllowed, String workerUuid);
}
