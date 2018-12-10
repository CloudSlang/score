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
package io.cloudslang.worker.management.services;

import io.cloudslang.engine.queue.entities.ExecutionMessage;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

public class ItpaMessageHandler implements MessageHandler {

    @Autowired
    private WorkerManager workerManager;

    @Autowired
    private SimpleExecutionRunnableFactory simpleExecutionRunnableFactory;

    @Override
    public void handle(ExecutionMessage executionMessage) {
        SimpleExecutionRunnable simpleExecutionRunnable = simpleExecutionRunnableFactory.getObject();
        simpleExecutionRunnable.setExecutionMessage(executionMessage);
        Long executionId = null;
        if (!StringUtils.isEmpty(executionMessage.getMsgId())) {
            executionId = Long.valueOf(executionMessage.getMsgId());
        }
        workerManager.addExecution(executionId, simpleExecutionRunnable);
    }
}
