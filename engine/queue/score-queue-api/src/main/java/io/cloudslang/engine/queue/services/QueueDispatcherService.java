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
import io.cloudslang.orchestrator.entities.Message;

import java.util.Date;
import java.util.List;

/**
 * User:
 * Date: 10/09/12
 * Time: 11:00
 *
 * handles dispatching adn polling messages from the queue
 *
 */
public interface QueueDispatcherService {

    /**
     *
     * dispatch messages to the queue
     *
     * @param messages the messages to dispatch
     */
	void dispatch(List<ExecutionMessage> messages);

    /**
     *
     * terminate Exception messages to prevent endless recovery
     *
     * @param message the messages to dispatch
     */
	void terminateCorruptedMessage(Message message);

    /**
     *
     * @param uuid the worker id
     * @param maxSize max size of the poll bulk
     * @param createDate the first message create date
     * @return a list of {@link io.cloudslang.engine.queue.entities.ExecutionMessage}
     */
	List<ExecutionMessage> poll(String uuid, int maxSize, Date createDate);

    /**
     *
     * Dispatch one message to the queue
     *
     * @param messageId the id of the message
     * @param group the group associated with the message
     * @param status the message status
     * @param payload the message payload
     */
	void dispatch(String messageId, String group, ExecStatus status, Payload payload);
}
