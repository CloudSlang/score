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

import io.cloudslang.engine.queue.entities.ExecutionMessage;
import io.cloudslang.orchestrator.entities.Message;

import java.util.List;

/**
 * User:
 * Date: 19/09/12
 * Time: 15:08
 *
 * A listener interface for score queue events.
 *
 */
public interface QueueListener {

    /**
     *
     * A callback that will be called when messages are inserted to the queue
     * before they are persisted to the DB
     *
     * @param messages the messages that are inserted to the queue
     */
	void prePersist(List<ExecutionMessage> messages);

    /**
     *
     * A callback that will be called when messages are inserted to the queue
     *
     * @param messages the messages that are inserted to the queue
     * @param queueSize  the size of the inserted messages
     */
	void onEnqueue(List<ExecutionMessage> messages,int queueSize);

    /**
     *
     * A callback that will be called when messages are polled from the queue
     *
     * @param messages the messages that are polled from the queue
     * @param queueSize the size of the polled messages
     */
	void onPoll(List<ExecutionMessage> messages,int queueSize);

    /**
     *
     * A callback that will be called when messages are in status of terminated
     *
     * @param messages the terminated messages
     */
	void onTerminated(List<ExecutionMessage> messages);

    /**
     *
     * A callback that will be called when messages are in status of failed
     *
     * @param messages the failed messages
     */
	void onFailed(List<ExecutionMessage> messages);

    /**
     *
     * @param message - execution message
     */
    void onCorrupted(Message message);
}
