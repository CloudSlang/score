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

package io.cloudslang.orchestrator.services;

import io.cloudslang.score.facade.entities.RunningExecutionPlan;
import io.cloudslang.score.facade.services.RunningExecutionPlanService;
import io.cloudslang.score.api.ExecutionPlan;
import io.cloudslang.orchestrator.repositories.RunningExecutionPlanRepository;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * User: wahnonm
 * Date: 26/01/14
 * Time: 17:45
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class RunningExecutionPlanServiceTest {

    @InjectMocks
    private RunningExecutionPlanService runningExecutionPlanService = new RunningExecutionPlanServiceImpl();

    @Mock
    private RunningExecutionPlanRepository runningExecutionPlanRepository;

    @Configuration
    static class EmptyConfig {
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCreateRunningExecutionPlan() throws InterruptedException {
        ExecutionPlan executionPlan = new ExecutionPlan();
        executionPlan.setFlowUuid("uuid");
        RunningExecutionPlan oldRunningExecutionPlan = new RunningExecutionPlan();
        oldRunningExecutionPlan.setId(3L);

        ExecutionPlan diffExecutionPlan = new ExecutionPlan();
        diffExecutionPlan.setFlowUuid("diff uuid");
        oldRunningExecutionPlan.setExecutionPlan(diffExecutionPlan);

        RunningExecutionPlan runningExecutionPlan = new RunningExecutionPlan();
        runningExecutionPlan.setId(5L);

        when(runningExecutionPlanRepository.findByUuidCached(anyString())).thenReturn(Arrays.asList(oldRunningExecutionPlan));
        when(runningExecutionPlanRepository.save(any(RunningExecutionPlan.class))).
                                                                                          thenReturn(runningExecutionPlan);

        Long id = runningExecutionPlanService.createRunningExecutionPlan(executionPlan, "11");
        Assert.assertEquals((Long) 5L, id);
    }
}
