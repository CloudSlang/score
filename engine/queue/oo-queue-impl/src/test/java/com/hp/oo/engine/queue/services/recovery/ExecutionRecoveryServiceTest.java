package com.hp.oo.engine.queue.services.recovery;

import com.hp.oo.engine.node.services.WorkerLockService;
import com.hp.oo.engine.node.services.WorkerNodeService;
import com.hp.oo.engine.queue.entities.ExecStatus;
import com.hp.oo.engine.queue.entities.ExecutionMessage;
import com.hp.oo.engine.queue.services.CounterNames;
import com.hp.oo.engine.queue.services.ExecutionQueueService;
import com.hp.oo.engine.versioning.services.VersionService;
import com.hp.oo.enginefacade.Worker;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * User: wahnonm
 * Date: 08/08/13
 * Time: 10:37
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class ExecutionRecoveryServiceTest {

	@Autowired
	private ExecutionRecoveryService executionRecoveryService = new ExecutionRecoveryServiceImpl();

	@Autowired
	private WorkerNodeService workerNodeService;

    @Autowired
    private WorkerLockService workerLockService;

    @Autowired
    private MessageRecoveryService messageRecoveryService;

	@Autowired
	private ExecutionQueueService executionQueueService;

	@Autowired
	private VersionService versionService;

	@Autowired
	private TransactionTemplate transactionTemplate;

	@Before
	public void setUp() {
		reset(workerNodeService, executionQueueService, versionService, transactionTemplate,messageRecoveryService);
		when(versionService.getCurrentVersion(CounterNames.MSG_RECOVERY_VERSION.name())).thenReturn(0L);
		when(transactionTemplate.execute(any(TransactionCallbackWithoutResult.class))).thenAnswer(new Answer<Object>() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				TransactionCallbackWithoutResult callback = (TransactionCallbackWithoutResult)invocation.getArguments()[0];
				if (callback != null) return callback.doInTransaction(null);
				else return null;
			}
		});
	}

	@Test
	public void testDoRecoveryWithNoNonRespondingWorkers() throws Exception {
		when(workerNodeService.readNonRespondingWorkers()).thenReturn(new ArrayList<String>());
		executionRecoveryService.doRecovery();
		verify(workerNodeService, never()).updateStatusInSeparateTransaction(anyString(), (Worker.Status) anyObject());
	}

	@Test
	public void testDoRecoveryWithNonRespondingWorkers() throws Exception {
		List<String> nonRespondingWorkers = new ArrayList<>();
		nonRespondingWorkers.add("worker1");
		when(workerNodeService.readNonRespondingWorkers()).thenReturn(nonRespondingWorkers);
		executionRecoveryService.doRecovery();
        verify(workerLockService, times(1)).lock("worker1");
		verify(workerNodeService, times(1)).updateStatusInSeparateTransaction("worker1", Worker.Status.IN_RECOVERY);
		verify(workerNodeService, times(1)).updateStatusInSeparateTransaction("worker1", Worker.Status.RECOVERED);

	}

	@Test
	public void testDoMsgRecovery() throws Exception {
		when(workerNodeService.readNonRespondingWorkers()).thenReturn(new ArrayList<String>());

		List<ExecutionMessage> msgWithNoAck = new ArrayList<>();
		msgWithNoAck.add(new ExecutionMessage());

		when(executionQueueService.pollMessagesWithoutAck(anyInt(), anyLong())).thenReturn(msgWithNoAck);
		executionRecoveryService.doRecovery();
        verify(messageRecoveryService).enqueueMessages(msgWithNoAck, ExecStatus.RECOVERED);

	}

	@Configuration
	static class EmptyConfig {
		@Bean ExecutionRecoveryService executionRecoveryService(){
			return new ExecutionRecoveryServiceImpl();
		}

		@Bean WorkerNodeService workerNodeService(){
			return mock(WorkerNodeService.class);
		}

        @Bean WorkerLockService workerLockService(){
            return mock(WorkerLockService.class);
        }

        @Bean MessageRecoveryService messageRecoveryService(){
            return mock(MessageRecoveryService.class);
        }

		@Bean ExecutionQueueService executionQueueService(){
			return mock(ExecutionQueueService.class);
		}

		@Bean VersionService versionService(){
			return mock(VersionService.class);
		}

		@Bean TransactionTemplate transactionTemplate(){
			return mock(TransactionTemplate.class);
		}
	}
}
