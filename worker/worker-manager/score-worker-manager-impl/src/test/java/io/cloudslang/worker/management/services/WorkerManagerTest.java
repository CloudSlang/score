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

import io.cloudslang.engine.node.services.WorkerNodeService;
import io.cloudslang.orchestrator.services.EngineVersionService;
import io.cloudslang.worker.management.WorkerConfigurationService;
import io.cloudslang.worker.management.monitor.WorkerStateUpdateService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.LinkedBlockingQueue;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Created with IntelliJ IDEA.
 * User: kravtsov
 * Date: 29/05/13
 * Time: 09:18
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = WorkerManagerTest.Configurator.class)
public class WorkerManagerTest {

	@Autowired
	private WorkerManager workerManager;

	@Autowired
	private WorkerNodeService workerNodeService;

	@Autowired
	private WorkerRecoveryManager workerRecoveryManager;

	@Autowired
	private EngineVersionService engineVersionService;

	static final String CREDENTIAL_UUID = "uuid";

	@Before
	public void setup() throws Exception {
		reset(workerNodeService, workerRecoveryManager);
		startWorker();
	}

	@After
	public void shutdownWorker() throws Exception {
		shutdownWorkerAndWait();
	}

	private void startWorker() throws InterruptedException {
		final long TIME_OUT = 2000L;
		workerManager.onApplicationEvent(mock(ContextRefreshedEvent.class));
		long t = System.currentTimeMillis();
		while (!workerManager.isUp()) {
			if (System.currentTimeMillis() - t > TIME_OUT) {
				throw fail("The worker has failed start on timeout: " + TIME_OUT + " ms");
			} else {
				Thread.sleep(100L);
			}
		}
	}

	private void shutdownWorkerAndWait() throws InterruptedException {
		final long TIME_OUT = 3000L;
		workerManager.onApplicationEvent(mock(ContextClosedEvent.class));
		long t = System.currentTimeMillis();
		while (workerManager.isUp()) {
			if (System.currentTimeMillis() - t > TIME_OUT) {
				throw fail("The worker has failed to shut down on timeout: " + TIME_OUT + " ms");
			} else {
				Thread.sleep(100L);
			}
		}
	}

	@Test
	public void testResolveDotnetVersion() {
		String version = WorkerManager.resolveDotNetVersion();
		System.out.println("Dotnet version is: " + version);
		assertThat(version).isNotEmpty();
	}

	@Test
	public void startUp() throws Exception {
		//shutting the service down
		workerManager.onApplicationEvent(mock(ContextClosedEvent.class));
		assertThat(workerManager.isUp()).isFalse();
		reset(workerNodeService);

		workerManager.onApplicationEvent(mock(ContextRefreshedEvent.class));
		Thread.sleep(1000L); // must sleep some time since the start up is being processed in a new thread
		verify(workerNodeService,atLeastOnce()).up(CREDENTIAL_UUID, "version", "123");
		assertThat(workerManager.isUp()).isTrue();
	}

	@Test
	public void startUpWrongVersion() throws Exception {

		//shutting the service down
		workerManager.onApplicationEvent(mock(ContextClosedEvent.class));
		assertThat(workerManager.isUp()).isFalse();
		reset(workerNodeService);

		reset(engineVersionService);
		when(engineVersionService.getEngineVersionId()).thenReturn("666");

		//starting it again
		workerManager.onApplicationEvent(mock(ContextRefreshedEvent.class));
		Thread.sleep(1000L); // must sleep some time since the start up is being processed in a new thread
		assertThat(workerManager.isUp()).isFalse();

		reset(engineVersionService);
		when(engineVersionService.getEngineVersionId()).thenReturn("123");
	}


	@Test
	public void startUpWithFailure() throws Exception {
		//shutting the service down
		workerManager.onApplicationEvent(mock(ContextClosedEvent.class));
		assertThat(workerManager.isUp()).isFalse();
		reset(workerNodeService);

		doThrow(new RuntimeException("try 1"))
				.doThrow(new RuntimeException("try 2"))
				.doThrow(new RuntimeException("try 3"))
				.doReturn("1")
				.when(workerNodeService).up(CREDENTIAL_UUID, "version", "123");

		//start again
		workerManager.onApplicationEvent(mock(ContextRefreshedEvent.class));

		Thread.sleep(2000L); // must sleep some time since the start up is being processed in a new thread
		verify(workerNodeService, times(4)).up(CREDENTIAL_UUID, "version", "123");
		assertThat(workerManager.isUp()).isTrue();
	}

	@Test(timeout = 10000)
	public void testKeepAliveFailTriggerRecovery() {
		doThrow(new RuntimeException("Network Error")).when(workerNodeService).keepAlive(CREDENTIAL_UUID);
		for (int i = 0; i < 5; i++) {
			workerManager.workerKeepAlive();
		}
		verify(workerRecoveryManager, times(1)).doRecovery();

		reset(workerNodeService);
	}

	@Test
	public void shutDown() {
		assertThat(workerManager.isUp()).isTrue();
		reset(workerNodeService);

		//shut down
		workerManager.onApplicationEvent(mock(ContextClosedEvent.class));
		assertThat(workerManager.isUp()).isFalse();
	}

	@Configuration
	static class Configurator {

        @Bean
        SynchronizationManager synchronizationManager(){
            return new SynchronizationManagerImpl();
        }

		@Bean
		WorkerManager workerManager() {
			return new WorkerManager();
		}

		@Bean
		String workerUuid() {
			return CREDENTIAL_UUID;
		}

		@Bean
		WorkerNodeService workerNodeService() {
			return mock(WorkerNodeService.class);
		}

		@Bean
		WorkerConfigurationService workerConfigurationService() {
			return mock(WorkerConfigurationService.class);
		}

		@Bean
		WorkerRecoveryManager workerRecoveryManager() {
			return mock(WorkerRecoveryManager.class);
		}

		@Bean
		Integer numberOfExecutionThreads() {
			return 2;
		}

		@Bean
		Double pollingMemoryRatio() {
			return 0.2D;
		}

		@Bean
		Long initStartUpSleep() {
			return 10L;
		}

		@Bean
		Long maxStartUpSleep() {
			return 100L;
		}

		@Bean
		WorkerConfigurationUtils workerConfigurationUtils() {
			WorkerConfigurationUtils workerConfigurationUtils = mock(WorkerConfigurationUtils.class);
			doReturn(mock(LinkedBlockingQueue.class)).when(workerConfigurationUtils).getBlockingQueue(anyInt(), anyInt());
			return workerConfigurationUtils;
		}

		@Bean
		WorkerStateUpdateService workerStateUpdateService() {
			return mock(WorkerStateUpdateService.class);
		}

		@Bean
		Integer inBufferCapacity() {
			return 20;
		}

		@Bean
		WorkerVersionService workerVersionService() {
			WorkerVersionService service =  mock(WorkerVersionService.class);
			when(service.getWorkerVersion()).thenReturn("version");
			when(service.getWorkerVersionId()).thenReturn("123");
			return service;
		}

		@Bean
		EngineVersionService engineVersionService() {
			EngineVersionService service =  mock(EngineVersionService.class);
			when(service.getEngineVersionId()).thenReturn("123");
			return service;
		}
	}
}
