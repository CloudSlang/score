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
package org.eclipse.score.orchestrator.services;

import org.eclipse.score.facade.entities.RunningExecutionPlan;
import org.eclipse.score.facade.services.RunningExecutionPlanService;
import org.eclipse.score.engine.queue.entities.ExecutionMessage;
import org.eclipse.score.engine.queue.entities.ExecutionMessageConverter;
import org.eclipse.score.engine.queue.services.QueueDispatcherService;
import org.eclipse.score.api.ExecutionPlan;
import org.eclipse.score.api.TriggeringProperties;
import org.eclipse.score.engine.data.IdentityGenerator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * User: wahnonm
 * Date: 30/01/14
 * Time: 14:23
 */
public class ScoreTriggeringTest {

    @InjectMocks
    private ScoreTriggering scoreTrigger = new ScoreTriggeringImpl();

    @Mock
    private RunningExecutionPlanService runningExecutionPlanService;

    @Mock
    private IdentityGenerator idGenerator;

    @Mock
    private QueueDispatcherService queueDispatcher;

    @Mock
    private ExecutionMessageConverter executionMessageConverter;

    @Mock
    private ExecutionStateService executionStateService;

    @Before
    public void resetMocks() {
        MockitoAnnotations.initMocks(this);
        RunningExecutionPlan runningExecutionPlan = mock(RunningExecutionPlan.class);
        when(runningExecutionPlan.getId()).thenReturn(2L);
        when(runningExecutionPlanService.createRunningExecutionPlan((any(RunningExecutionPlan.class)))).thenReturn(runningExecutionPlan);
    }

    @Test
    public void testTrigger2() throws Exception {
        ExecutionPlan ep = new ExecutionPlan();
        ep.setBeginStep(1L);
        TriggeringProperties triggeringProperties = TriggeringProperties.create(ep);
        scoreTrigger.trigger(triggeringProperties);

        verify(queueDispatcher, times(1)).dispatch(anyListOf(ExecutionMessage.class));
    }

    @Test
    public void testSaveOfRunningEP() throws Exception {
        ExecutionPlan ep = new ExecutionPlan();
        ep.setBeginStep(1L);
        TriggeringProperties triggeringProperties = TriggeringProperties.create(ep);
        scoreTrigger.trigger(triggeringProperties);

        verify(runningExecutionPlanService, times(1)).getOrCreateRunningExecutionPlan(any(ExecutionPlan.class));
    }

    @Test
    public void testSaveOfRunningEPComplex() throws Exception {
        ExecutionPlan ep = new ExecutionPlan();
        Map<String, ExecutionPlan> dep = new HashMap<>();
        dep.put("subflowEP", new ExecutionPlan());
        ep.setBeginStep(1L);
        TriggeringProperties triggeringProperties = TriggeringProperties.create(ep).setDependencies(dep);
        scoreTrigger.trigger(triggeringProperties);

        verify(runningExecutionPlanService, times(2)).getOrCreateRunningExecutionPlan(any(ExecutionPlan.class));
    }

}
