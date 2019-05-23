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

import io.cloudslang.engine.queue.entities.ExecStatus;
import io.cloudslang.engine.queue.entities.ExecutionMessage;
import io.cloudslang.engine.queue.entities.ExecutionMessageConverter;
import io.cloudslang.engine.queue.entities.Payload;
import io.cloudslang.engine.queue.services.QueueStateIdGeneratorService;
import io.cloudslang.score.facade.entities.Execution;
import io.cloudslang.worker.execution.services.ExecutionService;
import io.cloudslang.worker.management.WorkerConfigurationService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created with IntelliJ IDEA. User: wahnonm Date: 8/13/13 Time: 10:24 AM
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class SimpleExecutionRunnableTest {

    @Mock
    private ExecutionService executionService;

    @Mock
    private OutboundBuffer outBuffer;

    @Mock
    private InBuffer inBuffer;

    @Mock
    private ExecutionMessageConverter converter;

    @Mock
    private EndExecutionCallback endExecutionCallback;

    @Mock
    private ExecutionMessage executionMessage;

    @Mock
    private AtomicBoolean recoveryFlag;

    @Mock
    private QueueStateIdGeneratorService queueStateIdGenerator;

    @Mock
    private WorkerConfigurationService workerConfigurationService;

    @Mock
    private WorkerManager workerManager;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Configuration
    static class EmptyConfig {

    }

    @Test
    public void testGetExecutionMessage() throws Exception {
        SimpleExecutionRunnable simpleExecutionRunnable = new SimpleExecutionRunnable(executionService, outBuffer,
                inBuffer, converter, endExecutionCallback, queueStateIdGenerator,
                "stam", workerConfigurationService, workerManager, new SimpleRunnableContinuationDelegate());
        ExecutionMessage executionMessage = simpleExecutionRunnable.getExecutionMessage();
        Assert.assertNull(executionMessage);

        simpleExecutionRunnable.setExecutionMessage(new ExecutionMessage());
        executionMessage = simpleExecutionRunnable.getExecutionMessage();
        Assert.assertNotNull(executionMessage);
    }


    @Test
    public void testRun() throws Exception {
        Execution execution = new Execution();
        when(converter.extractExecution(any(Payload.class))).thenReturn(execution);

        final List<ExecutionMessage> buffer = new ArrayList<>();

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                for (Object message : invocation.getArguments()) {
                    buffer.add((ExecutionMessage) message);
                }
                return null;
            }
        }).when(outBuffer).put(any(ExecutionMessage[].class));

        when(workerManager.isFromCurrentThreadPool(anyString())).thenReturn(true);

        SimpleRunnableContinuationDelegate simpleRunnableContinuation = new SimpleRunnableContinuationDelegate();
        SimpleExecutionRunnable simpleExecutionRunnable = new SimpleExecutionRunnable(executionService, outBuffer,
                inBuffer, converter, endExecutionCallback, queueStateIdGenerator,
                "stam", workerConfigurationService, workerManager, simpleRunnableContinuation);

        ExecutionMessage executionMessage = new ExecutionMessage();
        executionMessage.setMsgId(String.valueOf(100L));
        simpleExecutionRunnable.setExecutionMessage(executionMessage);
        simpleExecutionRunnable.run();
        verify(executionService, times(1)).execute(execution);

        simpleRunnableContinuation.continueAsync(() -> {});

        Assert.assertFalse(buffer.isEmpty());
        Assert.assertEquals(ExecStatus.FINISHED, buffer.get(0).getStatus());

        Assert.assertEquals(ExecStatus.FINISHED, buffer.get(0).getStatus());
        Assert.assertEquals(0, this.executionMessage.getMsgSeqId());
    }
}
