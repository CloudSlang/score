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
package io.cloudslang.engine.queue.services.assigner;

import io.cloudslang.engine.queue.entities.ExecutionMessage;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User:
 * Date: 19/11/12
 *
 * Responsible for assigning messages to workers while considering the workers groups
 */
public interface ExecutionAssignerService {

    /**
     *
     * assigns a list of {@link io.cloudslang.engine.queue.entities.ExecutionMessage} to
     * workers
     *
     * @param messages List of {@link io.cloudslang.engine.queue.entities.ExecutionMessage} to assign
     * @return List of assigned {@link io.cloudslang.engine.queue.entities.ExecutionMessage}
     */
    List<ExecutionMessage> assignWorkers(List<ExecutionMessage> messages);
}
