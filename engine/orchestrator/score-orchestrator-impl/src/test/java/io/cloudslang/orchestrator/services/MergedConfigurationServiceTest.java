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

import io.cloudslang.engine.node.services.WorkerNodeService;
import io.cloudslang.engine.queue.entities.ExecutionMessageConverter;
import io.cloudslang.engine.queue.services.QueueDispatcherService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings({"SpringContextConfigurationInspection"})
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = MergedConfigurationServiceTest.Configurator.class)
public class MergedConfigurationServiceTest {


    @Autowired
    private MergedConfigurationService mergedConfigurationService;
    @Autowired
    private CancelExecutionService cancelExecutionService;
    @Autowired
    private PauseResumeService pauseResumeService;
    @Autowired
    private WorkerNodeService workerNodeService;



@Test
public void testCancelledFlows(){
    Long cancelledExecution1 = 11112222L;
    Long cancelledExecution2 = 22221111L;
    ArrayList cancelledFlows= new ArrayList();
    cancelledFlows.add(cancelledExecution1);
    cancelledFlows.add(cancelledExecution2);
    when(cancelExecutionService.readCanceledExecutionsIds()).thenReturn(cancelledFlows);
    assertTrue(mergedConfigurationService.fetchMergedConfiguration(getWorkerUuid()).getCancelledExecutions().contains(cancelledExecution1));
    assertTrue(mergedConfigurationService.fetchMergedConfiguration(getWorkerUuid()).getCancelledExecutions().contains(cancelledExecution2));
}

    @Test
    public void testPausedFlow(){
        String pausedExecutionId = "22223333";
        HashSet<String> pausedFlows= new HashSet<>();
        pausedFlows.add(pausedExecutionId);
        when(pauseResumeService.readAllPausedExecutionBranchIds()).thenReturn(pausedFlows);
        assertTrue(mergedConfigurationService.fetchMergedConfiguration(getWorkerUuid()).getPausedExecutions().contains(pausedExecutionId));
    }

    @Test
    public void testPausedCancelledFlows(){
        String pausedExecutionId = "22223333";
        HashSet<String> pausedFlows= new HashSet<>();
        pausedFlows.add(pausedExecutionId);
        Long cancelledExecution1 = 11112222L;
        Long cancelledExecution2 = 22221111L;
        ArrayList cancelledFlows= new ArrayList();
        cancelledFlows.add(cancelledExecution1);
        cancelledFlows.add(cancelledExecution2);
        when(pauseResumeService.readAllPausedExecutionBranchIds()).thenReturn(pausedFlows);
        when(cancelExecutionService.readCanceledExecutionsIds()).thenReturn(cancelledFlows);
        assertTrue(mergedConfigurationService.fetchMergedConfiguration(getWorkerUuid()).getPausedExecutions().contains(pausedExecutionId));
        assertTrue(mergedConfigurationService.fetchMergedConfiguration(getWorkerUuid()).getCancelledExecutions().contains(cancelledExecution1));
        assertTrue(mergedConfigurationService.fetchMergedConfiguration(getWorkerUuid()).getCancelledExecutions().contains(cancelledExecution2));
    }



    @Configuration
    static class Configurator {

        @Bean
        MergedConfigurationService mergedConfigurationService() {
            return new MergedConfigurationServiceImpl();
        }

        @Bean
        ExecutionMessageConverter executionMessageConverter() {
            return new ExecutionMessageConverter();
        }

        @Bean
        ExecutionSerializationUtil executionSerializationUtil() {
            return mock(ExecutionSerializationUtil.class);
        }

        @Bean
        ExecutionStateService runService() {
            return mock(ExecutionStateService.class);
        }

        @Bean
        QueueDispatcherService queueDispatcherService() {
            return mock(QueueDispatcherService.class);
        }

        @Bean
        CancelExecutionService cancelExecutionService() {
            return mock(CancelExecutionService.class);
        }

        @Bean
        PauseResumeService pauseResumeService() {
            return mock(PauseResumeService.class);
        }

        @Bean
        WorkerNodeService workerNodeService() {
            return mock(WorkerNodeService.class);
        }

    }

    protected static String getWorkerUuid() {
        return System.getProperty("worker.uuid");
    }

}
