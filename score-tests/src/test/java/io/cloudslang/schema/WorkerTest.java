/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package io.cloudslang.schema;

import io.cloudslang.orchestrator.services.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import io.cloudslang.engine.node.services.WorkerNodeService;
import io.cloudslang.engine.queue.entities.ExecutionMessageConverter;
import io.cloudslang.engine.queue.services.QueueDispatcherService;
import io.cloudslang.engine.queue.services.QueueStateIdGeneratorService;
import io.cloudslang.engine.queue.services.ScoreEventFactory;
import io.cloudslang.worker.management.services.WorkerManager;
import io.cloudslang.worker.management.services.dbsupport.WorkerDbSupportService;

import static org.mockito.Mockito.mock;

/**
 * Date: 1/21/14
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class WorkerTest {

	@Autowired
	private WorkerManager workerManager;

	@Test
    //This test starts an engine process, uncomment the line if you want the process to run, please note it is endless...
	public void baseEngineTest() throws InterruptedException {
//		Thread.currentThread().join();
	}

	@Configuration
	@ImportResource("classpath:META-INF/spring/schema/schemaWorkerTestContext.xml")
	static class Context{

		@Bean
		EngineVersionService versionService(){
			return mock(EngineVersionService.class);
		}

		@Bean
		OrchestratorDispatcherService orchestratorDispatcherService(){
			return mock(OrchestratorDispatcherService.class);
		}

		@Bean
		QueueDispatcherService queueDispatcherService(){
			return mock(QueueDispatcherService.class);
		}

		@Bean
		PauseResumeService pauseResumeService(){
			return mock(PauseResumeService.class);
		}

		@Bean
         WorkerNodeService workerNodeService(){
            return mock(WorkerNodeService.class);
        }

		@Bean
		WorkerDbSupportService workerDbSupportService(){
			return mock(WorkerDbSupportService.class);
		}

		@Bean
		CancelExecutionService cancelExecutionService(){
			return mock(CancelExecutionService.class);
		}

		@Bean
		MergedConfigurationService mergedConfigurationService(){
			return mock(MergedConfigurationService.class);
		}

		@Bean
		ExecutionMessageConverter executionMessageConverter(){
			return mock(ExecutionMessageConverter.class);
		}

		@Bean
		ScoreEventFactory scoreEventFactory() {
			return mock(ScoreEventFactory.class);
		}

		@Bean
		QueueStateIdGeneratorService queueStateIdGeneratorService(){
			return mock(QueueStateIdGeneratorService.class);
		}
	}

}
