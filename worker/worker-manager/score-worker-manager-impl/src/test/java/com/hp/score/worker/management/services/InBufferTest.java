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
package com.hp.score.worker.management.services;

import com.hp.score.engine.queue.services.QueueDispatcherService;
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
