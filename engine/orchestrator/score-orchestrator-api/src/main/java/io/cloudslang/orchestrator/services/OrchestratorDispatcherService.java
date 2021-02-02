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

package io.cloudslang.orchestrator.services;

import java.io.Serializable;
import java.util.List;

/**
 * Date: 12/1/13
 *
 * Responsible for handling dispatching of messages from the queue consumer
 *
 */
public interface OrchestratorDispatcherService {

    /**
     *
     * Dispatch messges to the queue from the consumer
     *
     * @param messages the messages to dispatch
     * @param bulkNumber an identifier of the dispatch bulk, needed for recovery
     * @param wrv the worker recovery version, needed for recovery
     * @param workerUuid the id of the dispatching worker
     */
	void dispatch(List<? extends Serializable> messages, String bulkNumber, String wrv, String workerUuid) throws WorkerAlreadyRecoveredException;
}
