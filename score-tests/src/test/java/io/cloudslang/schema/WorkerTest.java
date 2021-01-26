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

package io.cloudslang.schema;

import io.cloudslang.engine.queue.services.ExecutionQueueService;
import io.cloudslang.orchestrator.services.*;
import io.cloudslang.worker.management.queue.WorkerQueueDetailsContainer;
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

		@Bean
		ExecutionQueueService executionQueueService() {
			return mock(ExecutionQueueService.class);
		}

		@Bean
		SuspendedExecutionService suspendedExecution() {
			return mock(SuspendedExecutionService.class);
		}

		@Bean
		public WorkerQueueDetailsContainer workerQueueDetailsContainer() {
			return mock(WorkerQueueDetailsContainer.class);
		}
	}

}
