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
package org.eclipse.score.schema;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import org.eclipse.score.engine.node.services.WorkerNodeService;
import org.eclipse.score.engine.queue.entities.ExecutionMessageConverter;
import org.eclipse.score.engine.queue.services.QueueDispatcherService;
import org.eclipse.score.engine.queue.services.QueueStateIdGeneratorService;
import org.eclipse.score.engine.queue.services.ScoreEventFactory;
import org.eclipse.score.orchestrator.services.CancelExecutionService;
import org.eclipse.score.orchestrator.services.OrchestratorDispatcherService;
import org.eclipse.score.orchestrator.services.PauseResumeService;
import org.eclipse.score.worker.management.services.WorkerManager;
import org.eclipse.score.worker.management.services.dbsupport.WorkerDbSupportService;

import static org.mockito.Mockito.mock;

/**
 * Date: 1/21/14
 * @author Dima Rassin
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
