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

import io.cloudslang.engine.queue.entities.ExecStatus;
import io.cloudslang.engine.queue.entities.ExecutionMessage;
import io.cloudslang.engine.queue.entities.Payload;

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
     * @return a list of {@link io.cloudslang.engine.queue.entities.ExecutionMessage}
     */
	List<ExecutionMessage> poll(String uuid, int maxSize, long workerPollingMemory);

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
