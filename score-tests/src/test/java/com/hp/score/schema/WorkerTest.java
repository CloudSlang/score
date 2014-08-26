package com.hp.score.schema;

import com.hp.oo.broker.services.RuntimeValueService;
import com.hp.oo.engine.node.services.WorkerNodeService;
import com.hp.score.engine.queue.entities.ExecutionMessageConverter;
import com.hp.score.engine.queue.services.QueueDispatcherService;
import com.hp.score.engine.queue.services.QueueStateIdGeneratorService;
import com.hp.score.engine.queue.services.ScoreEventFactory;
import com.hp.oo.execution.services.WorkerManager;
import com.hp.oo.execution.services.dbsupport.WorkerDbSupportService;
import com.hp.oo.orchestrator.services.CancelExecutionService;
import com.hp.oo.orchestrator.services.OrchestratorDispatcherService;
import com.hp.oo.orchestrator.services.PauseResumeService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.score.worker.execution.WorkerConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.mockito.Mockito.mock;


/**
 * Date: 1/21/14
 *
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
		WorkerConfigurationService workerConfigurationService(){
			return mock(WorkerConfigurationService.class);
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
		RuntimeValueService runtimeValueService(){
			return mock(RuntimeValueService.class);
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
