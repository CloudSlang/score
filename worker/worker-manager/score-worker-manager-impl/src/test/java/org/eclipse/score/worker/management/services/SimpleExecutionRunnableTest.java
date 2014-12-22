/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package org.eclipse.score.worker.management.services;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.score.worker.execution.services.ExecutionService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.eclipse.score.worker.management.WorkerConfigurationService;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import org.openscore.engine.queue.entities.ExecStatus;
import org.openscore.engine.queue.entities.ExecutionMessage;
import org.openscore.engine.queue.entities.ExecutionMessageConverter;
import org.openscore.engine.queue.entities.Payload;
import org.openscore.engine.queue.services.QueueStateIdGeneratorService;
import org.eclipse.score.facade.entities.Execution;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created with IntelliJ IDEA.
 * User: wahnonm
 * Date: 8/13/13
 * Time: 10:24 AM
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
                inBuffer, converter, endExecutionCallback, queueStateIdGenerator, "stam",workerConfigurationService);
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
			    for (Object message: invocation.getArguments()){
			        buffer.add((ExecutionMessage) message);
			    }
			    return null;
		    }
	    }).when(outBuffer).put(any(ExecutionMessage[].class));

        SimpleExecutionRunnable simpleExecutionRunnable = new SimpleExecutionRunnable(executionService, outBuffer,
                inBuffer, converter, endExecutionCallback, queueStateIdGenerator, "stam",workerConfigurationService);

        simpleExecutionRunnable.setExecutionMessage(new ExecutionMessage());
        simpleExecutionRunnable.run();
        verify(executionService, times(1)).execute(execution);

        Assert.assertFalse(buffer.isEmpty());
        Assert.assertEquals(ExecStatus.FINISHED, buffer.get(0).getStatus());

        Assert.assertEquals(ExecStatus.FINISHED, buffer.get(0).getStatus());
        Assert.assertEquals(0, executionMessage.getMsgSeqId());
    }
}
