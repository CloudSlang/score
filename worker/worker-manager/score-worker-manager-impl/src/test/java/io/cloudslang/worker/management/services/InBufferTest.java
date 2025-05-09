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

import io.cloudslang.engine.queue.services.QueueDispatcherService;
import io.cloudslang.worker.management.monitor.WorkerStateUpdateService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.Collections;

import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

public class InBufferTest {

    @InjectMocks
    private InBuffer inBuffer;

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

    @Mock
    private WorkerStateUpdateService workerStateUpdateService;

    @Mock
    private WorkerConfigurationUtils workerConfigurationUtils;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testRunAfterCtxClosedEvent() throws Exception {
        ContextClosedEvent event = mock(ContextClosedEvent.class);
        inBuffer.onApplicationEvent(event);
        inBuffer.run();
        verifyNoInteractions(queueDispatcher);
    }

    @Test(timeout = 5000)
    public void testRunBeforeCtxClosedEvent() throws Exception {
        ContextRefreshedEvent refreshEvent = mock(ContextRefreshedEvent.class);
        inBuffer.onApplicationEvent(refreshEvent);

        ContextClosedEvent event = mock(ContextClosedEvent.class);
        when(workerManager.isUp()).thenReturn(true);
        doReturn(true).when(workerStateUpdateService).isWorkerEnabled();
        Thread thread = new Thread(inBuffer);
        thread.start();

        verify(workerManager, timeout(1000).atLeastOnce()).getInBufferSize();

        inBuffer.onApplicationEvent(event);
        while (thread.isAlive()) {
            Thread.sleep(100L);
        }
    }

    @Test(timeout = 5000)
    public void testPollingBehaviourOnWorkerEnabled() throws Exception {
        System.setProperty("worker.inbuffer.capacity", "20");

        try {
            doReturn(true).when(workerManager).isUp();
            doReturn(true).when(workerStateUpdateService).isWorkerEnabled();
            doReturn(1).when(workerManager).getInBufferSize();

            doNothing().when(synchronizationManager).finishGetMessages();
            doNothing().when(synchronizationManager).startGetMessages();
            doReturn(false).when(workerConfigurationUtils).isNewInbuffer();
            doReturn(0.1).when(workerConfigurationUtils).getWorkerMemoryRatio();
            doReturn(Collections.emptyList()).when(queueDispatcher).poll(anyString(), anyInt(), anyLong());

            inBuffer.init();
            Thread thread = new Thread(inBuffer);
            thread.start();

            // Wait 1 second
            Thread.sleep(1000);

            // stop InBuffer operation
            new Thread(() -> {
                ContextClosedEvent contextClosedEvent = mock(ContextClosedEvent.class);
                inBuffer.onApplicationEvent(contextClosedEvent);
            }).start();

            // Wait for inbuffer to die
            while (thread.isAlive()) {
                Thread.sleep(50L);
            }
            verify(queueDispatcher, atLeastOnce()).poll(eq(null), eq(19), anyLong());
            verify(synchronizationManager, atLeastOnce()).finishGetMessages();
        } finally {
            System.clearProperty("worker.inbuffer.capacity");
        }
    }

    @Test(timeout = 5000)
    public void testPollingBehaviourOnWorkerDisabled() throws Exception {
        System.setProperty("worker.inbuffer.capacity", "20");
        try {

            doReturn(true).when(workerManager).isUp();
            doReturn(false).when(workerStateUpdateService).isWorkerEnabled();
            doReturn(1).when(workerManager).getInBufferSize();

            doNothing().when(synchronizationManager).finishGetMessages();
            doNothing().when(synchronizationManager).startGetMessages();
            doReturn(false).when(workerConfigurationUtils).isNewInbuffer();
            doReturn(0.1).when(workerConfigurationUtils).getWorkerMemoryRatio();
            doReturn(Collections.emptyList()).when(queueDispatcher).poll(anyString(), anyInt(), anyLong());

            inBuffer.init();
            Thread thread = new Thread(inBuffer);
            thread.start();

            // Wait 1 second
            Thread.sleep(1000);

            // stop InBuffer operation
            new Thread(() -> {
                ContextClosedEvent contextClosedEvent = mock(ContextClosedEvent.class);
                inBuffer.onApplicationEvent(contextClosedEvent);
            }).start();

            // Wait for inbuffer to die
            while (thread.isAlive()) {
                Thread.sleep(50L);
            }
            verify(queueDispatcher, never()).poll(anyString(), anyInt(), anyLong());
            verify(synchronizationManager, atLeastOnce()).finishGetMessages();
        } finally {
            System.clearProperty("worker.inbuffer.capacity");
        }

    }


}
