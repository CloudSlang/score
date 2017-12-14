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

import io.cloudslang.engine.node.services.WorkerNodeService;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.mockito.Mockito.*;

/**
 * User: wahnonm
 * Date: 12/08/13
 * Time: 10:46
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class WorkerRecoveryManagerImplTest {

    @Autowired
    private WorkerRecoveryManager workerRecoveryManager;

    @Autowired
    private WorkerNodeService workerNodeService;

    @Autowired
    private RetryTemplate retryTemplate;

    @Autowired
    private List<WorkerRecoveryListener> workerRecoveryListeners;

    @Autowired
    private SynchronizationManager synchronizationManager;

    @Configuration
    static class config {

        @Bean
        public SynchronizationManager synchronizationManager(){
            return new SynchronizationManagerImpl();
        }

        @Bean
        public WorkerRecoveryManager workerRecoveryManager(){
            return new WorkerRecoveryManagerImpl();
        }

        @Bean
        public WorkerRecoveryListener workerRecoveryListener(){
            return mock(WorkerRecoveryListener.class);
        }

        @Bean
        public WorkerRecoveryListener workerRecoveryListener2(){
            return mock(WorkerRecoveryListener.class);
        }

        @Bean
        public WorkerNodeService workerNodeService(){
            return mock(WorkerNodeService.class);
        }

        @Bean
        public WorkerVersionService workerVersionService(){
            return mock(WorkerVersionService.class);
        }

        @Bean
        public RetryTemplate retryTemplate(){
            return mock(RetryTemplate.class);
        }
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testDoRecovery() throws Exception {
        Assert.assertEquals(2,workerRecoveryListeners.size());

        workerRecoveryManager.doRecovery();

        verify(workerRecoveryListeners.get(0),times(1)).doRecovery();
        verify(workerRecoveryListeners.get(1),times(1)).doRecovery();
    }

    @Test
    public void testIsInRecovery() throws Exception {
        Assert.assertFalse(workerRecoveryManager.isInRecovery());
    }
}
