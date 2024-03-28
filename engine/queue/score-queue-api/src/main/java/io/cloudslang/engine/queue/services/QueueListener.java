/*
 * Copyright © 2014-2017 EntIT Software LLC, a Micro Focus company (L.P.)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.cloudslang.engine.queue.services;

import io.cloudslang.engine.queue.entities.ExecutionMessage;

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
     * A callback that will be called when messages are required to be persisted in addition to the queue
     *
     * @param messages the to be persisted messages
     */
	void onPersistMessage(List<ExecutionMessage> messages);

}
