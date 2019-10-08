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

package io.cloudslang.engine.queue.services;

import io.cloudslang.engine.queue.entities.ExecStatus;
import io.cloudslang.engine.queue.entities.ExecutionMessage;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.*;

/**
 * User: wahnonm
 * Date: 07/08/13
 * Time: 16:21
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class QueueDispatcherServiceTest {

    private static final int WORKER_FREE_MEMORY = 200000000; //bytes

    @Mock
    private ExecutionQueueService executionQueueService;

    @Mock
    private BusyWorkersService busyWorkersService;

    @InjectMocks
    private QueueDispatcherService queueDispatcherService = new QueueDispatcherServiceImpl();

    @Configuration
    static class EmptyConfig {}

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testDispatchEmptyValues() throws Exception {
        List<ExecutionMessage> msg = new ArrayList<>();
        queueDispatcherService.dispatch(msg);
        verify(executionQueueService,never()).enqueue(anyList());

        queueDispatcherService.dispatch(null);
        verify(executionQueueService,never()).enqueue(anyList());
    }

    @Test
    public void testDispatch() throws Exception {
        List<ExecutionMessage> msg = new ArrayList<>();
        msg.add(new ExecutionMessage());

        queueDispatcherService.dispatch(msg);
        verify(executionQueueService,times(1)).enqueue(anyList());
    }

    @Test
    public void testPoll() throws Exception {
        Date now = new Date();
        when(busyWorkersService.isWorkerBusy("workerId")).thenReturn(true);
        queueDispatcherService.poll("workerId",5, WORKER_FREE_MEMORY);
        verify(executionQueueService,times(1)).poll("workerId", 5, WORKER_FREE_MEMORY, ExecStatus.ASSIGNED);
    }

    @Test
    public void testPollEmptyResult() throws Exception {
        Date now = new Date();
        List<ExecutionMessage> msg = new ArrayList<>();

        when(executionQueueService.poll("workerId", 5, WORKER_FREE_MEMORY, ExecStatus.ASSIGNED)).thenReturn(msg);
        List<ExecutionMessage> result = queueDispatcherService.poll("workerId",5, WORKER_FREE_MEMORY);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void testPollWithResult() throws Exception {
        Date now = new Date();
        List<ExecutionMessage> msg = new ArrayList<>();

        msg.add(new ExecutionMessage());
        msg.get(0).setMsgId("id1");

        msg.add(new ExecutionMessage());
        msg.get(1).setMsgId("id2");

        when(executionQueueService.poll("workerId", 5, WORKER_FREE_MEMORY, ExecStatus.ASSIGNED)).thenReturn(msg);
        List<ExecutionMessage> result = queueDispatcherService.poll("workerId",5, WORKER_FREE_MEMORY);
        Assert.assertEquals(2,result.size());
        Assert.assertEquals("id1",result.get(0).getMsgId());
        Assert.assertEquals("id2",result.get(1).getMsgId());
    }
}
