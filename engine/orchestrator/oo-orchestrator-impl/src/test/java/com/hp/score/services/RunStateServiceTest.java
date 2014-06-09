package com.hp.score.services;

import com.hp.oo.enginefacade.execution.ExecutionEnums;
import com.hp.oo.internal.sdk.execution.Execution;
import com.hp.oo.orchestrator.services.ExecutionSerializationUtil;
import com.hp.score.entities.RunState;
import com.hp.score.repositories.RunStateRepository;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * User: maromg
 * Date: 29/05/2014
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class RunStateServiceTest {

    @Autowired
    private RunStateService runStateService;

    @Autowired
    private RunStateRepository runStateRepository;

    @Autowired
    private ExecutionSerializationUtil executionSerializationUtil;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testReadByRunId_NullValue() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("runId cannot be null or empty");
        runStateService.readByRunId(null);
    }

    @Test
    public void testReadByRunId_EmptyValue() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("runId cannot be null or empty");
        runStateService.readByRunId("     ");
    }

    @Test
    public void testReadByRunId() {
        String runId = UUID.randomUUID().toString();
        RunState expectedRunState = new RunState();
        when(runStateRepository.findByRunId(runId)).thenReturn(Arrays.asList(expectedRunState));
        List<RunState> actualRunStates = runStateService.readByRunId(runId);
        assertThat(actualRunStates).containsExactly(expectedRunState);
    }

    @Test
    public void testReadByRunIdAndBranchId_RunIdNull() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("runId cannot be null or empty");
        runStateService.readByRunIdAndBranchId(null, "asd");
    }

    @Test
    public void testReadByRunIdAndBranchId_RunIdEmpty() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("runId cannot be null or empty");
        runStateService.readByRunIdAndBranchId("     ", "asd");
    }

    @Test
    public void testReadByRunIdAndBranchId_BranchIdNull() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("branchId cannot be null or empty");
        runStateService.readByRunIdAndBranchId("asd", null);
    }

    @Test
    public void testReadByRunIdAndBranchId_BranchIdEmpty() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("branchId cannot be null or empty");
        runStateService.readByRunIdAndBranchId("asd", "      ");
    }

    @Test
    public void testReadByRunIdAndBranchId() {
        String runId = UUID.randomUUID().toString();
        String branchId = UUID.randomUUID().toString();
        RunState expectedRunState = new RunState();
        when(runStateRepository.findByRunIdAndBranchId(runId, branchId)).thenReturn(expectedRunState);
        RunState actualRunState = runStateService.readByRunIdAndBranchId(runId, branchId);
        assertThat(actualRunState).isSameAs(expectedRunState);
    }

    @Test
    public void readRunIdAndBranchIdByStatuses_NullList() {
        when(runStateRepository.findRunIdByStatuses(null)).thenReturn(new ArrayList<String>());
        List<String> actualRunIds = runStateService.readRunIdByStatuses(null);
        assertThat(actualRunIds).hasSize(0);
    }

    @Test
    public void readRunIdAndBranchIdByStatuses_EmptyList() {
        when(runStateRepository.findRunIdByStatuses(anyListOf(ExecutionEnums.ExecutionStatus.class))).thenReturn(new ArrayList<String>());
        List<String> actualRunIds = runStateService.readRunIdByStatuses(new ArrayList<ExecutionEnums.ExecutionStatus>());
        assertThat(actualRunIds).hasSize(0);
    }

    @Test
    public void readRunIdAndBranchIdByStatuses() {
        List<ExecutionEnums.ExecutionStatus> statuses = Arrays.asList(ExecutionEnums.ExecutionStatus.COMPLETED, ExecutionEnums.ExecutionStatus.CANCELED);
        List<String> expectedRunIds = Arrays.asList("a", "b");
        when(runStateRepository.findRunIdByStatuses(statuses)).thenReturn(expectedRunIds);
        List<String> actualRunIds = runStateService.readRunIdByStatuses(statuses);
        assertThat(actualRunIds).containsExactly(expectedRunIds.toArray());
    }

    @Test
    public void testReadCancelledRun_NullValue() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("runId cannot be null or empty");
        runStateService.readCancelledRun(null);
    }

    @Test
    public void testReadCancelledRun_EmptyValue() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("runId cannot be null or empty");
        runStateService.readCancelledRun("      ");
    }

    @Test
    public void testReadCancelledRun() {
        String runId = UUID.randomUUID().toString();
        List<ExecutionEnums.ExecutionStatus> statuses = Arrays.asList(ExecutionEnums.ExecutionStatus.CANCELED, ExecutionEnums.ExecutionStatus.PENDING_CANCEL);
        RunState expectedRunState = new RunState();
        when(runStateRepository.findByRunIdAndBranchIdAndStatusIn(runId, RunState.EMPTY_BRANCH, statuses)).thenReturn(expectedRunState);
        RunState actualRunState = runStateService.readCancelledRun(runId);
        assertThat(actualRunState).isSameAs(expectedRunState);
    }

    @Test
    public void testCreateParentRun_NullRunId() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("runId cannot be null or empty");
        runStateService.createParentRun(null);
    }

    @Test
    public void testCreateParentRun_EmptyRunId() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("runId cannot be null or empty");
        runStateService.createParentRun("           ");
    }

    @Test
    public void testCreateParentRun() {
        String runId = UUID.randomUUID().toString();
        RunState runState = runStateService.createParentRun(runId);
        assertThat(runState.getRunId()).isEqualTo(runId);
    }

    @Test
    public void testCreateRunState_NullRunId() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("runId cannot be null or empty");
        runStateService.createRunState(null, "Asdfsdf");
    }

    @Test
    public void testCreateRunState_EmptyRunId() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("runId cannot be null or empty");
        runStateService.createRunState("         ", "Asdfsdf");
    }

    @Test
    public void testCreateRunState_NullBranchId() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("branchId cannot be null or empty");
        runStateService.createRunState("Asdasd", null);
    }

    @Test
    public void testCreateRunState_EmptyBranchId() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("branchId cannot be null or empty");
        runStateService.createRunState("Asdasd", "          ");
    }

    @Test
    public void testCreateRunState() {
        String runId = UUID.randomUUID().toString();
        String branchId = UUID.randomUUID().toString();
        RunState runState = runStateService.createRunState(runId, branchId);
        assertThat(runState.getRunId()).isEqualTo(runId);
        assertThat(runState.getBranchId()).isEqualTo(branchId);
    }

    @Test
    public void testReadRunObject_NullRunId() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("runId cannot be null or empty");
        runStateService.readRunObject(null, "Asdfsdf");
    }

    @Test
    public void testReadRunObject_EmptyRunId() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("runId cannot be null or empty");
        runStateService.readRunObject("         ", "Asdfsdf");
    }

    @Test
    public void testReadRunObject_NullBranchId() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("branchId cannot be null or empty");
        runStateService.readRunObject("Asdasd", null);
    }

    @Test
    public void testReadRunObject_EmptyBranchId() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("branchId cannot be null or empty");
        runStateService.readRunObject("Asdasd", "          ");
    }

    @Test
    public void testReadRunObject() {
        String runId = UUID.randomUUID().toString();
        String branchId = UUID.randomUUID().toString();

        Execution expectedExecution = new Execution();
        byte[] runObjectBytes = new byte[] {0,0,0};
        RunState runState = new RunState();
        runState.setRunObject(runObjectBytes);

        when(runStateRepository.findByRunIdAndBranchId(runId, branchId)).thenReturn(runState);
        when(executionSerializationUtil.objFromBytes(runObjectBytes)).thenReturn(expectedExecution);

        Execution actualExecution = runStateService.readRunObject(runId, branchId);
        assertThat(actualExecution).isSameAs(expectedExecution);
    }

    @Test
    public void testUpdateRunObject_NullRunId() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("runId cannot be null or empty");
        runStateService.updateRunObject(null, "Asdfsdf", null);
    }

    @Test
    public void testUpdateRunObject_EmptyRunId() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("runId cannot be null or empty");
        runStateService.updateRunObject("         ", "Asdfsdf", null);
    }

    @Test
    public void testUpdateRunObject_NullBranchId() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("branchId cannot be null or empty");
        runStateService.updateRunObject("Asdasd", null, null);
    }

    @Test
    public void testUpdateRunObject_EmptyBranchId() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("branchId cannot be null or empty");
        runStateService.updateRunObject("Asdasd", "          ", null);
    }

    @Test
    public void testUpdateRunObject() {
        String runId = UUID.randomUUID().toString();
        String branchId = UUID.randomUUID().toString();
        Execution execution = new Execution();

        byte[] runObjectBytes = new byte[] {0,0,0};
        RunState runState = Mockito.mock(RunState.class);

        when(runStateRepository.findByRunIdAndBranchId(runId, branchId)).thenReturn(runState);
        when(executionSerializationUtil.objToBytes(execution)).thenReturn(runObjectBytes);

        runStateService.updateRunObject(runId, branchId, execution);
        verify(runState, times(1)).setRunObject(runObjectBytes);
    }

    @Test
    public void testUpdateRunStateStatus_NullRunId() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("runId cannot be null or empty");
        runStateService.updateRunStateStatus(null, "Asdfsdf", null);
    }

    @Test
    public void testUpdateRunStateStatus_EmptyRunId() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("runId cannot be null or empty");
        runStateService.updateRunStateStatus("         ", "Asdfsdf", null);
    }

    @Test
    public void testUpdateRunStateStatus_NullBranchId() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("branchId cannot be null or empty");
        runStateService.updateRunStateStatus("Asdasd", null, null);
    }

    @Test
    public void testUpdateRunStateStatus_EmptyBranchId() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("branchId cannot be null or empty");
        runStateService.updateRunStateStatus("Asdasd", "          ", null);
    }

    @Test
    public void testUpdateRunStateStatus_NullStatus() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("status cannot be null");
        runStateService.updateRunStateStatus("Asdasd", "asdasd", null);
    }

    @Test
    public void testUpdateRunStateStatus() {
        String runId = UUID.randomUUID().toString();
        String branchId = UUID.randomUUID().toString();
        ExecutionEnums.ExecutionStatus status = ExecutionEnums.ExecutionStatus.PAUSED;

        RunState runState = Mockito.mock(RunState.class);

        when(runStateRepository.findByRunIdAndBranchId(runId, branchId)).thenReturn(runState);

        runStateService.updateRunStateStatus(runId, branchId, status);
        verify(runState, times(1)).setStatus(status);
    }

    @Test
    public void testDeleteRunState_NullRunId() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("runId cannot be null or empty");
        runStateService.deleteRunState(null, "Asdfsdf");
    }

    @Test
    public void testDeleteRunState_EmptyRunId() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("runId cannot be null or empty");
        runStateService.deleteRunState("         ", "Asdfsdf");
    }

    @Test
    public void testDeleteRunState_NullBranchId() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("branchId cannot be null or empty");
        runStateService.deleteRunState("Asdasd", null);
    }

    @Test
    public void testDeleteRunState_EmptyBranchId() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("branchId cannot be null or empty");
        runStateService.deleteRunState("Asdasd", "          ");
    }

    @Test
    @DirtiesContext
    public void testDeleteRunState() {
        Mockito.reset(runStateRepository);
        String runId = UUID.randomUUID().toString();
        String branchId = UUID.randomUUID().toString();

        RunState runState = new RunState();

        when(runStateRepository.findByRunIdAndBranchId(runId, branchId)).thenReturn(runState);

        runStateService.deleteRunState(runId, branchId);
        verify(runStateRepository, times(1)).delete(runState);
    }

    @Configuration
    static class RunStateServiceTestContext {

        @Bean
        public RunStateService runStateService() {
            return new RunStateServiceImpl();
        }

        @Bean
        public RunStateRepository runStateRepository() {
            RunStateRepository runStateRepositoryMock = Mockito.mock(RunStateRepository.class);
            when(runStateRepositoryMock.save(any(RunState.class))).thenAnswer(new Answer<RunState>() {
                @Override
                public RunState answer(InvocationOnMock invocation) throws Throwable {
                    return (RunState) invocation.getArguments()[0];
                }
            });
            return runStateRepositoryMock;
        }

        @Bean
        public ExecutionSerializationUtil executionSerializationUtil() {
            return Mockito.mock(ExecutionSerializationUtil.class);
        }

    }

}
