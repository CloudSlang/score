package com.hp.oo.execution.services;

import com.hp.oo.engine.node.services.WorkerNodeService;
import com.hp.oo.orchestrator.services.configuration.WorkerConfigurationService;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;


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
	static final String CREDENTIAL_PASS = "pass";

	@Test
	public void testResolveDotnetVersion() {
		String version = workerManager.resolveDotNetVersion();
		System.out.println("Dotnet version is: " + version);
		assertThat(version).isNotEmpty();
	}

	@Test
    @Ignore
	public void startUp() throws Exception {
		workerManager.onApplicationEvent(mock(ContextClosedEvent.class));
		assertThat(workerManager.isUp()).isFalse();
		reset(workerNodeService);

		workerManager.onApplicationEvent(mock(ContextRefreshedEvent.class));
		Thread.sleep(1000L); // must sleep some time since the start up is being processed in a new thread
		verify(workerNodeService).up(CREDENTIAL_UUID);
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
				.doNothing()
				.when(workerNodeService).up(CREDENTIAL_UUID);

		workerManager.onApplicationEvent(mock(ContextRefreshedEvent.class));

		Thread.sleep(2000L); // must sleep some time since the start up is being processed in a new thread
		verify(workerNodeService, times(4)).up(CREDENTIAL_UUID);
		assertThat(workerManager.isUp()).isTrue();
	}

    @Test
    public void testKeepAlive(){
        reset(workerNodeService);
        workerManager.workerKeepAlive();
        verify(workerNodeService,times(1)).keepAlive(CREDENTIAL_UUID);
    }

    @Test
    public void testKeepAliveFailTriggerRecovery(){
        doThrow(new RuntimeException("Network Error")).when(workerNodeService).keepAlive(CREDENTIAL_UUID);
        for(int i=0 ; i<5 ; i++){
            workerManager.workerKeepAlive();
        }
        verify(workerRecoveryManager,times(1)).doRecovery();
        reset(workerNodeService);
    }

	@Test
    @Ignore
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
		@Bean WorkerManager workerManager() {
            WorkerManager workerManagerBean = new WorkerManager();
            workerManagerBean.setWorkerUuid(CREDENTIAL_UUID);
            return workerManagerBean;
		}

		@Bean WorkerNodeService workerNodeService() {
			return mock(WorkerNodeService.class);
		}

		@Bean WorkerConfigurationService workerConfigurationService() {
			return mock(WorkerConfigurationService.class);
		}

		@Bean WorkerRecoveryManager workerRecoveryManager() {
			return mock(WorkerRecoveryManager.class);
		}

		@Bean(name = "numberOfExecutionThreads")
		Integer numberOfExecutionThreads() {return 2;}

		@Bean(name = "initStartUpSleep")
		Long initStartUpSleep() {return 10L;}

		@Bean(name = "maxStartUpSleep")
		Long maxStartUpSleep() {return 100L;}
	}
}
