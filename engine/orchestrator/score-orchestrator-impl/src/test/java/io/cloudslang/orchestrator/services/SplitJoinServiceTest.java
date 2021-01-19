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

import io.cloudslang.engine.queue.entities.ExecStatus;
import io.cloudslang.engine.queue.entities.ExecutionMessage;
import io.cloudslang.engine.queue.entities.ExecutionMessageConverter;
import io.cloudslang.engine.queue.repositories.ExecutionQueueRepository;
import io.cloudslang.engine.queue.services.QueueDispatcherService;
import io.cloudslang.score.events.FastEventBus;
import io.cloudslang.score.facade.entities.Execution;
import io.cloudslang.orchestrator.entities.BranchContexts;
import io.cloudslang.orchestrator.entities.FinishedBranch;
import io.cloudslang.orchestrator.entities.SplitMessage;
import io.cloudslang.orchestrator.entities.SuspendedExecution;
import io.cloudslang.orchestrator.repositories.FinishedBranchRepository;
import io.cloudslang.orchestrator.repositories.SuspendedExecutionsRepository;
import io.cloudslang.score.api.EndBranchDataContainer;
import io.cloudslang.score.facade.entities.Execution;
import io.cloudslang.score.lang.SystemContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import static ch.lambdaj.Lambda.select;
import static io.cloudslang.orchestrator.enums.SuspendedExecutionReason.NON_BLOCKING;
import static io.cloudslang.orchestrator.enums.SuspendedExecutionReason.PARALLEL;
import static io.cloudslang.orchestrator.enums.SuspendedExecutionReason.PARALLEL_LOOP;
import static java.util.EnumSet.of;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.any;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class SplitJoinServiceTest {
    @InjectMocks
    private SplitJoinService splitJoinService = new SplitJoinServiceImpl();

    @Mock
    private SuspendedExecutionsRepository suspendedExecutionsRepository;

    @Mock
    private FinishedBranchRepository finishedBranchRepository;

    @Mock
    private ExecutionQueueRepository executionQueueRepository;

    @Mock
    private QueueDispatcherService queueDispatcherService;

    @Mock
    private FastEventBus fastEventBus;

    @Mock
    private ExecutionMessageConverter converter;

    @Mock
    private AplsLicensingService aplsLicensingService;

    @Captor
    private ArgumentCaptor<List<ExecutionMessage>> queueDispatcherDispatchCaptor;

    @Captor
    private ArgumentCaptor<List<SuspendedExecution>> suspendedExecutionsSaveCaptor;

    @Captor
    private ArgumentCaptor<SuspendedExecution> suspendedExecutionsSingleSaveCaptor;

    @Captor
    private ArgumentCaptor<Execution> converterCaptor;

    @Configuration
    static class EmptyConfig {
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void triggerChildrenSplitTest() {
        String splitId = UUID.randomUUID().toString();
        SplitMessage splitMessage = createSplitMessage(splitId, "MULTI_INSTANCE");

        splitJoinService.split(Arrays.asList(splitMessage));
        Mockito.verify(queueDispatcherService).dispatch(queueDispatcherDispatchCaptor.capture());
        List<ExecutionMessage> argument = queueDispatcherDispatchCaptor.getValue();

        List<ExecutionMessage> branchMessages = select(argument, having(on(ExecutionMessage.class).getStatus(), is(ExecStatus.PENDING)));

        assertThat("exactly one branch should be triggered", branchMessages.size(), is(1));
        // validate the branch ExecutionMessage has the proper payload
        Mockito.verify(converter).createPayload(splitMessage.getChildren().get(0));
    }

    @Test
    public void suspendParentSplitTest() {
        String splitId = UUID.randomUUID().toString();
        SplitMessage splitMessage = createSplitMessage(splitId, "MULTI_INSTANCE");

        splitJoinService.split(Arrays.asList(splitMessage));
        Mockito.verify(suspendedExecutionsRepository).saveAll(suspendedExecutionsSaveCaptor.capture());
        List<SuspendedExecution> value = suspendedExecutionsSaveCaptor.getValue();

        assertThat("exactly one suspended entity must be created", value.size(), is(1));
        assertThat("suspended execution entity created with incorrect split id", value.get(0).getSplitId(), is(splitId));
        assertThat("suspended entity has incorrect execution object", value.get(0).getExecutionObj(), is(splitMessage.getParent()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullParamSplitTest() {
        splitJoinService.split(null);
    }

    @Test
    public void missingSuspendedExecutionEndBranchTest() {
        String splitId1 = UUID.randomUUID().toString();
        String splitId2 = UUID.randomUUID().toString();
        Execution branch1 = Mockito.mock(Execution.class);
        Execution branch2 = Mockito.mock(Execution.class);
        SystemContext systemContext1 = Mockito.mock(SystemContext.class);
        SystemContext systemContext2 = Mockito.mock(SystemContext.class);
        Mockito.when(branch1.getSystemContext()).thenReturn(systemContext1);
        Mockito.when(branch2.getSystemContext()).thenReturn(systemContext2);
        Mockito.when(systemContext1.getSplitId()).thenReturn(splitId1);
        Mockito.when(systemContext2.getSplitId()).thenReturn(splitId2);

        Mockito.when(branch1.getPosition()).thenReturn(null);
        Mockito.when(branch2.getPosition()).thenReturn(null);

        // find only a a partial amount of the required suspended execution entities
        Mockito.when(suspendedExecutionsRepository.findBySplitIdIn(Arrays.asList(splitId1, splitId2))).thenReturn(Arrays.asList(createSuspendedExecution(splitId1, 1)));

        // must not throw an exception
        splitJoinService.endBranch(Arrays.asList(branch1, branch2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullParamEndBranchTest() {
        splitJoinService.endBranch(null);
    }

    // ***************************************
    // * SplitJoinService.joinFinishedSplits() *
    // ***************************************
    @Test
    public void triggerParentJoinFinishedSplitsTest() {
        String splitId = UUID.randomUUID().toString();
        SuspendedExecution suspendedExecution = createSuspendedExecution(splitId, 1);
        HashMap<String, Serializable> context = new HashMap<>();
        context.put("someData", "1");

        suspendedExecution.getFinishedBranches().add(createFinishedBranch(splitId, splitId + "1", context, new HashMap<String, Serializable>()));
        Mockito.when(suspendedExecutionsRepository.findFinishedSuspendedExecutions(eq(of(PARALLEL, NON_BLOCKING, PARALLEL_LOOP)), any(Pageable.class))).thenReturn(Arrays.asList(suspendedExecution));

        int joinedSplits = splitJoinService.joinFinishedSplits(1);
        assertThat(joinedSplits, is(1));

        Mockito.verify(converter).createPayload(suspendedExecution.getExecutionObj());
        Mockito.verify(queueDispatcherService).dispatch(queueDispatcherDispatchCaptor.capture());

        List<ExecutionMessage> argument = queueDispatcherDispatchCaptor.getValue();
        assertThat("exactly one execution should be dispatched", argument.size(), is(1));
        assertThat("parent sent back to the queue should be in status pending", argument.get(0).getStatus(), is(ExecStatus.PENDING));
        assertThat("ExecutionMessage has a different msg id then the execution object's execution id", argument.get(0).getMsgId(), is(suspendedExecution.getExecutionObj().getExecutionId().toString()));
    }

    @Test
    public void deleteParentJoinFinishedSplitsTest() {
        String splitId = UUID.randomUUID().toString();
        SuspendedExecution suspendedExecution = createSuspendedExecution(splitId, 1);
        suspendedExecution.getFinishedBranches().add(createFinishedBranch(splitId, splitId + "1", new HashMap<String, Serializable>(), new HashMap<String, Serializable>()));
        Mockito.when(suspendedExecutionsRepository.findFinishedSuspendedExecutions(eq(of(PARALLEL, NON_BLOCKING, PARALLEL_LOOP)), any(Pageable.class))).thenReturn(Arrays.asList(suspendedExecution));

        int joinedSplits = splitJoinService.joinFinishedSplits(1);
        assertThat(joinedSplits, is(1));

        Mockito.verify(suspendedExecutionsRepository).deleteAll(Arrays.asList(suspendedExecution));
    }

    @Test
    public void insertBranchesToParentJoinFinishedSplitsTest() {
        String splitId = UUID.randomUUID().toString();
        SuspendedExecution suspendedExecution = createSuspendedExecution(splitId, 1);
        HashMap<String, Serializable> context = new HashMap<>();
        Map<String, Serializable> branchSystemContext = new HashMap<>();
        context.put("haha", "lala");

        suspendedExecution.getFinishedBranches().add(createFinishedBranch(splitId, splitId + "1", context, branchSystemContext));
        Mockito.when(suspendedExecutionsRepository.findFinishedSuspendedExecutions(eq(of(PARALLEL, NON_BLOCKING, PARALLEL_LOOP)), any(Pageable.class))).thenReturn(Arrays.asList(suspendedExecution));

        int joinedSplits = splitJoinService.joinFinishedSplits(1);
        assertThat(joinedSplits, is(1));

        Mockito.verify(converter).createPayload(converterCaptor.capture());
        Execution value = converterCaptor.getValue();

        List<EndBranchDataContainer> finishedChildContexts = value.getSystemContext().getFinishedChildBranchesData();

        Map<String, Serializable> ooContexts = suspendedExecution.getFinishedBranches().get(0).getBranchContexts().getContexts();
        Map<String, Serializable> systemContext = suspendedExecution.getFinishedBranches().get(0).getBranchContexts().getSystemContext();
        assertThat("parent execution must contain children maps", finishedChildContexts, is(Arrays.asList(
                new EndBranchDataContainer(ooContexts, systemContext, null))));
    }

    // private helpers
    private Execution createExecution(Long id) {
        Execution res = new Execution(id, null, null, null, new SystemContext());
        return res;
    }

    private SplitMessage createSplitMessage(String splitId, String stepType) {
        SplitMessage splitMessage = new SplitMessage(splitId, createExecution(1L), Arrays.asList(createExecution(2L)), 12, true);
        SystemContext systemContext = splitMessage.getParent().getSystemContext();
        systemContext.put("STEP_TYPE", stepType);
        return splitMessage;
    }

    private SuspendedExecution createSuspendedExecution(String splitId, int numOfBranches) {
        return new SuspendedExecution(1 + "", splitId, numOfBranches, createExecution(1L), PARALLEL, false);
    }

    private FinishedBranch createFinishedBranch(String splitId, String branchId, HashMap<String, Serializable> context, Map<String, Serializable> systemContext) {
        HashMap<String, Serializable> contexts = new HashMap<>();
        contexts.put("haha", context);
        return new FinishedBranch(null, branchId, splitId, null, new BranchContexts(false, contexts, systemContext));
    }
}