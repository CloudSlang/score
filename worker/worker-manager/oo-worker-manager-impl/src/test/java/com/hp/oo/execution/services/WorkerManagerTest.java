package com.hp.oo.execution.services;

import com.hp.oo.engine.node.services.WorkerNodeService;
import com.hp.oo.orchestrator.services.configuration.WorkerConfigurationService;
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

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;
import static org.mockito.Mockito.*;


/**
 * Created with IntelliJ IDEA.
 * User: kravtsov
 * Date: 29/05/13
 * Time: 09:18
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class WorkerManagerTest {

	@Autowired
	private WorkerManager workerManager;

	@Autowired
	private WorkerNodeService workerNodeService;

	@Autowired
	private WorkerRecoveryManager workerRecoveryManager;

	static final String CREDENTIAL_UUID = "uuid";

	@Before
	public void setup() throws Exception {
		reset(workerNodeService, workerRecoveryManager);
		startWorker();
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

	@Test
	public void testResolveDotnetVersion() {
		String version = workerManager.resolveDotNetVersion();
		System.out.println("Dotnet version is: " + version);
		assertThat(version).isNotEmpty();
	}

	@Test
	public void startUp() throws Exception {
		workerManager.onApplicationEvent(mock(ContextClosedEvent.class));
		assertThat(workerManager.isUp()).isFalse();
		reset(workerNodeService);

		workerManager.onApplicationEvent(mock(ContextRefreshedEvent.class));
		Thread.sleep(1000L); // must sleep some time since the start up is being processed in a new thread
		verify(workerNodeService,atLeastOnce()).up(CREDENTIAL_UUID);
		assertThat(workerManager.isUp()).isTrue();
	}

	@Test
	public void startUpWithFailure() throws Exception {
		workerManager.onApplicationEvent(mock(ContextClosedEvent.class));
		assertThat(workerManager.isUp()).isFalse();
		reset(workerNodeService);

		doThrow(new RuntimeException("try 1"))
				.doThrow(new RuntimeException("try 2"))
				.doThrow(new RuntimeException("try 3"))
				.doReturn("1")
				.when(workerNodeService).up(CREDENTIAL_UUID);

		workerManager.onApplicationEvent(mock(ContextRefreshedEvent.class));

		Thread.sleep(2000L); // must sleep some time since the start up is being processed in a new thread
		verify(workerNodeService, times(4)).up(CREDENTIAL_UUID);
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
		workerManager.onApplicationEvent(mock(ContextRefreshedEvent.class));
		assertThat(workerManager.isUp()).isTrue();
		reset(workerNodeService);

		workerManager.onApplicationEvent(mock(ContextClosedEvent.class));
		verify(workerNodeService).down(CREDENTIAL_UUID);
		assertThat(workerManager.isUp()).isFalse();
	}

	@Configuration
	static class Configurator {

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
		Long initStartUpSleep() {
			return 10L;
		}

		@Bean
		Long maxStartUpSleep() {
			return 100L;
		}
	}
}
