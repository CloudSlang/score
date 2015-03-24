/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package io.cloudslang.worker.management.services;

import io.cloudslang.engine.queue.services.QueueDispatcherService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.mockito.Mockito.*;

/**
* User: wahnonm
* Date: 15/08/13
* Time: 11:32
*/
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class InBufferTest {

    @InjectMocks
    private InBuffer inBuffer = new InBuffer();

    @Mock
    private QueueDispatcherService queueDispatcher;

    @Mock
    private WorkerManager workerManager;

    @Mock
    private SimpleExecutionRunnableFactory simpleExecutionRunnableFactory;

    @Mock
    private OutboundBuffer outBuffer;

    @Mock
    private WorkerRecoveryManagerImpl recoveryManager;

    @Mock
    private SynchronizationManager synchronizationManager;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Configuration
    static class EmptyConfig {}

    @Test
    public void testRunAfterCtxClosedEvent() throws Exception {
        ContextClosedEvent event = mock(ContextClosedEvent.class);
        inBuffer.onApplicationEvent(event);
        inBuffer.run();
        verifyZeroInteractions(queueDispatcher);
    }

    @Test(timeout = 5000)
    public void testRunBeforeCtxClosedEvent() throws Exception {
        ContextRefreshedEvent refreshEvent =  mock(ContextRefreshedEvent.class);
        inBuffer.onApplicationEvent(refreshEvent);

        ContextClosedEvent event = mock(ContextClosedEvent.class);
        when(workerManager.isUp()).thenReturn(true);
        Thread thread = new Thread(inBuffer);
        thread.start();

        verify(workerManager,timeout(1000).atLeastOnce()).getInBufferSize();

        inBuffer.onApplicationEvent(event);
        while(thread.isAlive()){
	        Thread.sleep(100L);
        }
    }
}
