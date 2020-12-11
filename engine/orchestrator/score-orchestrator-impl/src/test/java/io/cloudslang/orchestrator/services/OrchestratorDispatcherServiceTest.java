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

package io.cloudslang.orchestrator.services;

import io.cloudslang.engine.node.entities.WorkerNode;
import io.cloudslang.engine.node.services.WorkerLockService;
import io.cloudslang.engine.node.services.WorkerNodeService;
import io.cloudslang.engine.queue.entities.ExecutionMessage;
import io.cloudslang.engine.queue.services.QueueDispatcherService;
import io.cloudslang.orchestrator.entities.Message;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

/**
 * Created with IntelliJ IDEA.
 * User: kravtsov
 * Date: 24/07/14
 * Time: 08:58
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class OrchestratorDispatcherServiceTest {

    @InjectMocks
    private OrchestratorDispatcherService orchestratorDispatcherService = new OrchestratorDispatcherServiceImpl();

    @Mock
    WorkerNodeService workerNodeService;

    @Mock
    WorkerLockService workerLockService;

    @Mock
    private QueueDispatcherService queueDispatcher;

    @Mock
    private SplitJoinService splitJoinService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void simpleDispatchTest() throws Exception {
        List<Message> messages = new ArrayList<>();
        messages.add(new ExecutionMessage());

        String newBulkNumber = "2";
        String uuid = "123";

        WorkerNode node = new WorkerNode();
        node.setBulkNumber("1");
        node.setWorkerRecoveryVersion("1");

        when(workerNodeService.readByUUID(anyString())).thenReturn(node);

        orchestratorDispatcherService.dispatch(messages, newBulkNumber, "1", uuid);
        Mockito.verify(workerLockService, times(1)).lock(uuid);
        Mockito.verify(queueDispatcher, times(1)).dispatch(anyList());
        Mockito.verify(workerNodeService, times(1)).updateBulkNumber(uuid, newBulkNumber);
    }

    @Test
    public void firstDispatchTest() throws Exception {
        List<Message> messages = new ArrayList<>();
        messages.add(new ExecutionMessage());

        String newBulkNumber = "2";
        String uuid = "123";

        WorkerNode node = new WorkerNode(); //node with NULL bulk number since it is first dispatch
        node.setWorkerRecoveryVersion("1");

        when(workerNodeService.readByUUID(anyString())).thenReturn(node);

        orchestratorDispatcherService.dispatch(messages, newBulkNumber, "1", uuid);
        Mockito.verify(workerLockService, times(1)).lock(uuid);
        Mockito.verify(queueDispatcher, times(1)).dispatch(anyList());
        Mockito.verify(workerNodeService, times(1)).updateBulkNumber(uuid, newBulkNumber);
    }

    @Test
    public void sameBulkDispatchTest() throws Exception {
        List<Message> messages = new ArrayList<>();
        messages.add(new ExecutionMessage());

        String newBulkNumber = "1";
        String uuid = "123";

        WorkerNode node = new WorkerNode();
        node.setBulkNumber("1");
        node.setWorkerRecoveryVersion("1");

        when(workerNodeService.readByUUID(anyString())).thenReturn(node);

        orchestratorDispatcherService.dispatch(messages, newBulkNumber, "1", uuid);
        Mockito.verify(workerLockService, times(1)).lock(uuid);
        Mockito.verify(queueDispatcher, times(0)).dispatch(anyList());
        Mockito.verify(workerNodeService, times(0)).updateBulkNumber(uuid, newBulkNumber);
    }
    
    @Test(expected = Exception.class)
    
        public void dispatchAfterRecoveryTest() throws Exception {
            List<Message> messages = new ArrayList<>();
            messages.add(new ExecutionMessage());
    
            String newBulkNumber = "2";
            String uuid = "123";
    
            WorkerNode node = new WorkerNode();
            node.setBulkNumber("1");
            node.setWorkerRecoveryVersion("1");
    
            when(workerNodeService.readByUUID(anyString())).thenReturn(node);
    
            //worker dispatches with wrong WRV
            orchestratorDispatcherService.dispatch(messages, newBulkNumber, "0", uuid);
    
            Mockito.verify(queueDispatcher, times(0)).dispatch(anyList());
            Mockito.verify(workerNodeService, times(0)).updateBulkNumber(uuid, newBulkNumber);
        }

    @Configuration
    static class EmptyConfig {

    }

}
