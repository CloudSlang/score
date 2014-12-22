/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package org.openscore.engine.queue.services;

import org.openscore.engine.data.IdentityGenerator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openscore.engine.queue.services.QueueStateIdGeneratorService;
import org.openscore.engine.queue.services.QueueStateIdGeneratorServiceImpl;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * User: wahnonm
 * Date: 07/08/13
 * Time: 16:21
 */
public class QueueStateIdGeneratorTest {

    @Mock
    private IdentityGenerator identityGenerator;

    @InjectMocks
    private QueueStateIdGeneratorService queueStateIdGeneratorService =
            new QueueStateIdGeneratorServiceImpl();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void verifyIdentityGeneratorNextWasCalled() {
        queueStateIdGeneratorService.generateStateId();
        verify(identityGenerator, times(1)).next();
    }

    @Test
    public void verifyIdentityGeneratorBulkWasNotCalled() {
        queueStateIdGeneratorService.generateStateId();
        verify(identityGenerator, times(0)).bulk(anyInt());
    }
}
