/*
 * Licensed to Hewlett-Packard Development Company, L.P. under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
*/
package org.eclipse.score.worker.management.services;

import org.eclipse.score.worker.execution.services.ExecutionService;
import org.eclipse.score.engine.queue.entities.ExecutionMessageConverter;
import org.eclipse.score.engine.queue.services.QueueStateIdGeneratorService;

import org.eclipse.score.worker.management.WorkerConfigurationService;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;

/**
 * Created by IntelliJ IDEA.
 * User: Amit Levin
 * Date: 19/12/12
 */
public class SimpleExecutionRunnableFactory implements FactoryBean<SimpleExecutionRunnable> {

	@Autowired
	private ExecutionService executionService;

	@Autowired
	private OutboundBuffer outBuffer;

    @Autowired
   	private InBuffer inBuffer;

	@Autowired
	private ExecutionMessageConverter converter;

	@Autowired
	private EndExecutionCallback endExecutionCallback;

    @Autowired
    private QueueStateIdGeneratorService queueStateIdGeneratorService;

    @Autowired
    private WorkerConfigurationService workerConfigurationService;

    @Resource
	private String workerUuid;

	@Override
	public SimpleExecutionRunnable getObject() {
		return new SimpleExecutionRunnable(
                executionService,
                outBuffer,
                inBuffer,
                converter,
                endExecutionCallback,
                queueStateIdGeneratorService,
                workerUuid,
                workerConfigurationService
        );
	}

	@Override
	public Class<?> getObjectType() {
		return SimpleExecutionRunnable.class;
	}

	@Override
	public boolean isSingleton() {
		return false;
	}
}
