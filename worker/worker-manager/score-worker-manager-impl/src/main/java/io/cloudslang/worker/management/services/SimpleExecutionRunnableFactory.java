/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package io.cloudslang.worker.management.services;

import io.cloudslang.worker.execution.services.ExecutionService;
import io.cloudslang.engine.queue.entities.ExecutionMessageConverter;
import io.cloudslang.engine.queue.services.QueueStateIdGeneratorService;

import io.cloudslang.worker.management.WorkerConfigurationService;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;

/**
 * Created by IntelliJ IDEA.
 * User:
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

    @Autowired
    private WorkerManager workerManager;

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
                workerConfigurationService,
                workerManager
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
