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
package io.cloudslang.engine.queue.repositories;

import io.cloudslang.engine.queue.entities.LargeExecutionMessage;

import java.util.List;

public interface LargeExecutionMessagesRepository {

    LargeExecutionMessage find(long id);

    List<LargeExecutionMessage> findAll();

    void add(List<LargeExecutionMessage> messages);

    void updateCount(List<LargeExecutionMessage> messages);

    void delete(long id);

    long getMessageRunningExecutionId(long execStateId);

    void clearAssignedWorker(long id);
}
