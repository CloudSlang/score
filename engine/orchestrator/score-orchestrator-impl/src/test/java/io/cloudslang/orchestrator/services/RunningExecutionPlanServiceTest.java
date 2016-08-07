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

        Long id = runningExecutionPlanService.getOrCreateRunningExecutionPlan(executionPlan);
        Assert.assertEquals((Long) 5L, id);
    }
}
