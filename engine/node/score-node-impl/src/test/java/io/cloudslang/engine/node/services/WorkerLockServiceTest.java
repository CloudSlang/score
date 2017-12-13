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

package io.cloudslang.engine.node.services;

import io.cloudslang.engine.node.entities.WorkerLock;
import io.cloudslang.engine.node.repositories.WorkerLockRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * User: varelasa
 * Date: 21/07/14
 * Time: 11:02
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class WorkerLockServiceTest {

    @InjectMocks
    private WorkerLockService workerLockService = new WorkerLockServiceImpl();

    @Mock
    private WorkerLockRepository workerLockRepository;

    @Test
    public void createTest(){

        String uuid = "uuid";
        WorkerLock workerLock = new WorkerLock();
        workerLock.setUuid(uuid);
        workerLockService.create(uuid);
        verify(workerLockRepository).save(workerLock);
    }

    @Test
    public void lockTest(){

        String uuid = "uuid";
        when(workerLockRepository.lock(uuid)).thenReturn(1);
        workerLockService.lock(uuid);
        verify(workerLockRepository).lock(uuid);
    }

    @Test(expected = IllegalStateException.class)
    public void lockFailedTest(){

        String uuid = "uuid";
        when(workerLockRepository.lock(uuid)).thenReturn(0);
        workerLockService.lock(uuid);

    }

    @Test
    public void deleteTest(){
        String uuid = "uuid";
        workerLockService.delete(uuid);
        verify(workerLockRepository).deleteByUuid(uuid);
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Configuration
    static class EmptyConfig {
    }
}
