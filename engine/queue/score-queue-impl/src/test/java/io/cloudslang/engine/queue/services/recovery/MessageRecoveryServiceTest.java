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

package io.cloudslang.engine.queue.services.recovery;

import io.cloudslang.engine.queue.entities.ExecStatus;
import io.cloudslang.engine.queue.entities.ExecutionMessage;
import io.cloudslang.engine.queue.services.ExecutionQueueService;
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
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * User: varelasa
 * Date: 24/07/14
 * Time: 13:29
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class MessageRecoveryServiceTest {

    @InjectMocks
    private MessageRecoveryService messageRecoveryService = new MessageRecoveryServiceImpl();

    @Mock
    private ExecutionQueueService executionQueueService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void recoverMessagesBulkNoMsg() {

        String uuid = "uuid1";
        int poolSize = 5;
        List<ExecutionMessage> messages = new ArrayList<>();
        when(executionQueueService.pollRecovery(uuid, poolSize, ExecStatus.ASSIGNED, ExecStatus.IN_PROGRESS)).thenReturn(messages);
        boolean toContinue = messageRecoveryService.recoverMessagesBulk(uuid, poolSize);
        assertThat("no messages, should not continue", toContinue, is(Boolean.FALSE));
        verify(executionQueueService, never()).enqueue(anyList());

    }

    @Test
    public void recoverMessagesBulkSmallBulk() {

        String uuid = "uuid1";
        int poolSize = 5;
        List<ExecutionMessage> messages = new ArrayList<>();
        messages.add(mock(ExecutionMessage.class));
        when(executionQueueService.pollRecovery(uuid, poolSize, ExecStatus.ASSIGNED, ExecStatus.SENT, ExecStatus.IN_PROGRESS)).thenReturn(messages);
        boolean toContinue = messageRecoveryService.recoverMessagesBulk(uuid, poolSize);
        setMessageListToRecovered(messages);
        assertThat("no messages to continue , should not continue", toContinue, is(Boolean.FALSE));
        verify(executionQueueService, times(1)).enqueue(messages);
    }

    @Test
    public void recoverMessagesBulkBigBulk() {

        String uuid = "uuid1";
        int poolSize = 1;
        List<ExecutionMessage> messages = new ArrayList<>();
        messages.add(mock(ExecutionMessage.class));
        messages.add(mock(ExecutionMessage.class));
        when(executionQueueService.pollRecovery(uuid, poolSize, ExecStatus.ASSIGNED, ExecStatus.SENT, ExecStatus.IN_PROGRESS)).thenReturn(messages);
        boolean toContinue = messageRecoveryService.recoverMessagesBulk(uuid, poolSize);
        setMessageListToRecovered(messages);
        assertThat("no messages, should not continue", toContinue, is(Boolean.TRUE));
        verify(executionQueueService, times(1)).enqueue(messages);
    }

    @Test
    public void enqueuMessagesTest() {

        List<ExecutionMessage> messages = new ArrayList<>();
        messages.add(new ExecutionMessage());
        messages.add(new ExecutionMessage());
        messageRecoveryService.enqueueMessages(messages, ExecStatus.RECOVERED);
        setMessageListToRecovered(messages);
        verify(executionQueueService, times(1)).enqueue(messages);
    }

    private void setMessageListToRecovered(List<ExecutionMessage> messages) {
        for (ExecutionMessage msg : messages) {
            msg.setStatus(ExecStatus.RECOVERED);
            msg.incMsgSeqId();
        }

    }

    @Configuration
    static class EmptyConfig {
    }
}
