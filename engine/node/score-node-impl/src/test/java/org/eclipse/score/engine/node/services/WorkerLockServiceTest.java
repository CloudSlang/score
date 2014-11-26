/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package org.eclipse.score.engine.node.services;

import org.eclipse.score.engine.node.entities.WorkerLock;
import org.eclipse.score.engine.node.repositories.WorkerLockRepository;
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
