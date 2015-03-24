/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package io.cloudslang.orchestrator.services;

import org.openscore.api.ExecutionPlan;
import org.openscore.api.Score;
import org.openscore.api.TriggeringProperties;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * User: wahnonm
 * Date: 22/01/14
 * Time: 17:01
 */
public class ScoreTest {

    @InjectMocks
    private Score score = new ScoreImpl();

    @Mock
    private ScoreTriggering scoreTriggering;

    @Before
    public void resetMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testTrigger() throws Exception {
        ExecutionPlan ep = new ExecutionPlan();
        ep.setBeginStep(1L);
        TriggeringProperties triggeringProperties = TriggeringProperties.create(ep);
        score.trigger(triggeringProperties);

        verify(scoreTriggering, times(1)).trigger(any(TriggeringProperties.class));
    }

    @Test
    public void testTrigger2() throws Exception {
        ExecutionPlan ep = new ExecutionPlan();
        ep.setBeginStep(1L);
        TriggeringProperties triggeringProperties = TriggeringProperties.create(ep);
        score.trigger(triggeringProperties);

        verify(scoreTriggering, times(1)).trigger(any(TriggeringProperties.class));
    }

}
