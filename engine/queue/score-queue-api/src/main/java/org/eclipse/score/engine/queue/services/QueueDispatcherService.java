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
     * @param uuid the worker id
     * @param maxSize max size of the poll bulk
     * @param createDate the first message create date
     * @return a list of {@link org.eclipse.score.engine.queue.entities.ExecutionMessage}
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
