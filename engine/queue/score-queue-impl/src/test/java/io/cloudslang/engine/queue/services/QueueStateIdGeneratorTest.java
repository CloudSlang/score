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

package io.cloudslang.engine.queue.services;

import io.cloudslang.engine.data.IdentityGenerator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.anyInt;
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
