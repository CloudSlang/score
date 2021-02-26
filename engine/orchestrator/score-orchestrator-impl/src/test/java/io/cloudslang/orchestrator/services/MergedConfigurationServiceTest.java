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

import com.google.common.collect.Lists;
import io.cloudslang.engine.node.services.WorkerNodeService;
import io.cloudslang.engine.queue.entities.ExecutionMessageConverter;
import io.cloudslang.engine.queue.services.QueueDispatcherService;
import io.cloudslang.orchestrator.entities.MergedConfigurationDataContainer;
import io.cloudslang.orchestrator.model.MergedConfigurationHolder;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

@SuppressWarnings({"SpringContextConfigurationInspection"})
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = MergedConfigurationServiceTest.Configurator.class)
public class MergedConfigurationServiceTest {

    @Autowired
    private MergedConfigurationServiceImpl mergedConfigurationService;

    @Autowired
    private CancelExecutionService cancelExecutionService;

    @Autowired
    private PauseResumeService pauseResumeService;

    @Autowired
    private WorkerNodeService workerNodeService;

    @AfterClass
    public static void cleanup() {
        System.clearProperty("worker.mergedConfiguration.refreshDelayMillis");
        System.clearProperty("worker.mergedConfiguration.initialDelayMillis");
        System.clearProperty("worker.uuid");
    }

    @Test
    public void testCancelledFlows() {
        Long cancelledExecution1 = 11112222L;
        Long cancelledExecution2 = 22221111L;
        ArrayList<Long> cancelledFlows = Lists.newArrayList(cancelledExecution1, cancelledExecution2);

        doReturn(cancelledFlows).when(cancelExecutionService).readCanceledExecutionsIds();
        doReturn(Collections.emptySet()).when(pauseResumeService).readAllPausedExecutionBranchIdsNoCache();
        doReturn(Collections.emptyMap()).when(workerNodeService).readWorkerGroupsMap();

        String workerUuid = getWorkerUuid();
        final MergedConfigurationHolder oldValue = mergedConfigurationService.getMergedConfigHolderReference().get();
        await().atMost(200, MILLISECONDS).until(() -> mergedConfigurationService.getMergedConfigHolderReference().get() != oldValue);

        // tested call
        MergedConfigurationDataContainer mergedConfig = mergedConfigurationService.fetchMergedConfiguration(workerUuid);

        Set<Long> cancelledExecutions = mergedConfig.getCancelledExecutions();
        assertThat(cancelledExecutions, containsInAnyOrder(cancelledExecution1, cancelledExecution2));
        assertThat(cancelledExecutions.size(), is(2));
    }

    @Test
    public void testPausedFlow() {
        String pausedExecutionId = "22223333";
        HashSet<String> pausedFlows = newHashSet(pausedExecutionId);

        doReturn(pausedFlows).when(pauseResumeService).readAllPausedExecutionBranchIdsNoCache();

        String workerUuid = getWorkerUuid();
        final MergedConfigurationHolder oldValue = mergedConfigurationService.getMergedConfigHolderReference().get();
        await().atMost(200, MILLISECONDS).until(() -> mergedConfigurationService.getMergedConfigHolderReference().get() != oldValue);

        // tested call
        MergedConfigurationDataContainer mergedConfig = mergedConfigurationService.fetchMergedConfiguration(workerUuid);

        Set<String> pausedExecutions = mergedConfig.getPausedExecutions();
        assertThat(pausedExecutions, containsInAnyOrder(pausedExecutionId));
        assertThat(pausedExecutions.size(), is(1));
    }

    @Test
    public void testPausedCancelledFlows() {
        String pausedExecutionId1 = "22223333";
        String pausedExecutionId2 = "22223339";
        HashSet<String> pausedFlows = newHashSet(pausedExecutionId1, pausedExecutionId2);
        Long cancelledExecution1 = 11112222L;
        Long cancelledExecution2 = 22221111L;
        ArrayList<Long> cancelledFlows = newArrayList(cancelledExecution1, cancelledExecution2);

        doReturn(pausedFlows).when(pauseResumeService).readAllPausedExecutionBranchIdsNoCache();
        doReturn(cancelledFlows).when(cancelExecutionService).readCanceledExecutionsIds();

        String workerUuid = getWorkerUuid();
        final MergedConfigurationHolder oldValue = mergedConfigurationService.getMergedConfigHolderReference().get();
        await().atMost(200, MILLISECONDS).until(() -> mergedConfigurationService.getMergedConfigHolderReference().get() != oldValue);

        // tested call
        MergedConfigurationDataContainer mergedConfig = mergedConfigurationService.fetchMergedConfiguration(workerUuid);

        Set<Long> cancelledExecutions = mergedConfig.getCancelledExecutions();
        assertThat(cancelledExecutions, containsInAnyOrder(cancelledExecution1, cancelledExecution2));
        assertThat(cancelledExecutions.size(), is(2));

        Set<String> pausedExecutions = mergedConfig.getPausedExecutions();
        assertThat(pausedExecutions, containsInAnyOrder(pausedExecutionId1, pausedExecutionId2));
        assertThat(pausedExecutions.size(), is(2));
    }

    @Test
    public void testPausedCancelledAndGroups() {
        String pausedExecutionId1 = "22223333";
        String pausedExecutionId2 = "22223339";
        HashSet<String> pausedFlows = newHashSet(pausedExecutionId1, pausedExecutionId2);
        Long cancelledExecution1 = 11112222L;
        Long cancelledExecution2 = 22221111L;
        ArrayList<Long> cancelledFlows = newArrayList(cancelledExecution1, cancelledExecution2);
        HashMap<String, Set<String>> workerGroupMap = new HashMap<>();
        workerGroupMap.put("0979f11c-226f-11eb-adc1-0242ac120002", newHashSet("abc", "def", "group1"));
        workerGroupMap.put("60883752-226f-11eb-adc1-0242ac120002", newHashSet("ras1", "central1"));

        doReturn(pausedFlows).when(pauseResumeService).readAllPausedExecutionBranchIdsNoCache();
        doReturn(cancelledFlows).when(cancelExecutionService).readCanceledExecutionsIds();
        doReturn(workerGroupMap).when(workerNodeService).readWorkerGroupsMap();

        String workerUuid = getWorkerUuid();
        final MergedConfigurationHolder oldValue = mergedConfigurationService.getMergedConfigHolderReference().get();
        await().atMost(200, MILLISECONDS).until(() -> mergedConfigurationService.getMergedConfigHolderReference().get() != oldValue);

        // tested call
        MergedConfigurationDataContainer mergedConfig = mergedConfigurationService.fetchMergedConfiguration(workerUuid);

        Set<Long> cancelledExecutions = mergedConfig.getCancelledExecutions();
        assertThat(cancelledExecutions, containsInAnyOrder(cancelledExecution1, cancelledExecution2));
        assertThat(cancelledExecutions.size(), is(2));

        Set<String> pausedExecutions = mergedConfig.getPausedExecutions();
        assertThat(pausedExecutions, containsInAnyOrder(pausedExecutionId1, pausedExecutionId2));
        assertThat(pausedExecutions.size(), is(2));

        Set<String> workerGroups = mergedConfig.getWorkerGroups();
        assertThat(workerGroups, containsInAnyOrder("abc", "def", "group1"));
        assertThat(workerGroups.size(), is(3));
    }


    @Configuration
    static class Configurator {

        @Bean
        public MergedConfigurationServiceImpl mergedConfigurationService() {
            System.setProperty("worker.mergedConfiguration.refreshDelayMillis", "50");
            System.setProperty("worker.mergedConfiguration.initialDelayMillis", "0");
            System.setProperty("worker.uuid", "0979f11c-226f-11eb-adc1-0242ac120002");
            return spy(new MergedConfigurationServiceImpl());
        }

        @Bean
        public ExecutionMessageConverter executionMessageConverter() {
            return new ExecutionMessageConverter();
        }

        @Bean
        public ExecutionSerializationUtil executionSerializationUtil() {
            return mock(ExecutionSerializationUtil.class);
        }

        @Bean
        public ExecutionStateService runService() {
            return mock(ExecutionStateService.class);
        }

        @Bean
        public QueueDispatcherService queueDispatcherService() {
            return mock(QueueDispatcherService.class);
        }

        @Bean
        public CancelExecutionService cancelExecutionService() {
            return mock(CancelExecutionService.class);
        }

        @Bean
        public PauseResumeService pauseResumeService() {
            return mock(PauseResumeService.class);
        }

        @Bean
        public WorkerNodeService workerNodeService() {
            return mock(WorkerNodeService.class);
        }

    }

    protected static String getWorkerUuid() {
        return System.getProperty("worker.uuid");
    }

}
