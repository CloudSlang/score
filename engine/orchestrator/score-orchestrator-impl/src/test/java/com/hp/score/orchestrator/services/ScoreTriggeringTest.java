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
package com.hp.score.orchestrator.services;

import com.hp.score.facade.entities.RunningExecutionPlan;
import com.hp.score.facade.services.RunningExecutionPlanService;
import com.hp.score.engine.queue.entities.ExecutionMessage;
import com.hp.score.engine.queue.entities.ExecutionMessageConverter;
import com.hp.score.engine.queue.services.QueueDispatcherService;
import com.hp.score.api.ExecutionPlan;
import com.hp.score.api.TriggeringProperties;
import com.hp.score.engine.data.IdentityGenerator;
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
