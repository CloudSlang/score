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
package org.eclipse.score.engine.queue.services.recovery;

import org.eclipse.score.engine.node.services.WorkerLockService;
import org.eclipse.score.engine.node.services.WorkerNodeService;
import org.eclipse.score.engine.queue.entities.ExecStatus;
import org.eclipse.score.engine.queue.entities.ExecutionMessage;
import org.eclipse.score.engine.queue.services.ExecutionQueueService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

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
	private ExecutionRecoveryServiceImpl executionRecoveryService = new ExecutionRecoveryServiceImpl();

	@Autowired
	private WorkerNodeService workerNodeService;

    @Autowired
    private WorkerRecoveryService workerRecoveryService;

    @Autowired
    private MessageRecoveryService messageRecoveryService;

	@Autowired
	private ExecutionQueueService executionQueueService;

    @Before
    public void setUp() {
        reset(workerNodeService, executionQueueService, messageRecoveryService);
    }

    @Test
    public void testRecoverWorkers() throws Exception {
        when(workerNodeService.readAllWorkersUuids()).thenReturn(getWorkers());
        executionRecoveryService.recoverWorkers();

        verify(workerRecoveryService, times(1)).doWorkerAndMessageRecovery("123");
        verify(workerRecoveryService, times(1)).doWorkerAndMessageRecovery("456");
        verify(workerRecoveryService, times(1)).doWorkerAndMessageRecovery("789");
    }

    private List<String> getWorkers(){
        List<String> allWorkers = new ArrayList<>();

        allWorkers.add("123");
        allWorkers.add("456");
        allWorkers.add("789");

        return allWorkers;
    }

    @Test
    public void testAssignRecoveredMessages() throws Exception {

        //5 RECOVERED messages in queue
        List<ExecutionMessage> recoveredMessages = new ArrayList<>();
        recoveredMessages.add(new ExecutionMessage());
        recoveredMessages.add(new ExecutionMessage());
        recoveredMessages.add(new ExecutionMessage());
        recoveredMessages.add(new ExecutionMessage());
        recoveredMessages.add(new ExecutionMessage());

		when(executionQueueService.readMessagesByStatus(1000, ExecStatus.RECOVERED)).thenReturn(recoveredMessages);
        executionRecoveryService.assignRecoveredMessages();
        verify(messageRecoveryService).enqueueMessages(recoveredMessages, ExecStatus.PENDING);
	}

	@Configuration
	static class EmptyConfig {
		@Bean ExecutionRecoveryService executionRecoveryService(){
			return new ExecutionRecoveryServiceImpl();
		}

		@Bean WorkerNodeService workerNodeService(){
			return mock(WorkerNodeService.class);
		}

        @Bean WorkerRecoveryService workerRecoveryService(){
            return mock(WorkerRecoveryService.class);
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
	}
}
