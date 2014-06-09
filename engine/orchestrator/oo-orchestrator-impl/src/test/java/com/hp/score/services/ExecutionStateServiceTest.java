package com.hp.score.services;

import com.hp.oo.enginefacade.execution.ExecutionEnums;
import com.hp.oo.internal.sdk.execution.Execution;
import com.hp.oo.orchestrator.services.ExecutionSerializationUtil;
import com.hp.score.entities.ExecutionState;
import com.hp.score.repositories.ExecutionStateRepository;
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
public class ExecutionStateServiceTest {

    @Autowired
    private ExecutionStateService executionStateService;

    @Autowired
    private ExecutionStateRepository executionStateRepository;

    @Autowired
    private ExecutionSerializationUtil executionSerializationUtil;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testReadByExecutionId_NullValue() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("executionId cannot be null or empty");
        executionStateService.readByExecutionId(null);
    }

    @Test
    public void testReadByExecutionId_EmptyValue() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("executionId cannot be null or empty");
        executionStateService.readByExecutionId("     ");
    }

    @Test
    public void testReadByExecutionId() {
        String executionId = UUID.randomUUID().toString();
        ExecutionState expectedExecutionState = new ExecutionState();
        when(executionStateRepository.findByExecutionId(executionId)).thenReturn(Arrays.asList(expectedExecutionState));
        List<ExecutionState> actualExecutionStates = executionStateService.readByExecutionId(executionId);
        assertThat(actualExecutionStates).containsExactly(expectedExecutionState);
    }

    @Test
    public void testReadByExecutionIdAndBranchId_ExecutionIdNull() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("executionId cannot be null or empty");
        executionStateService.readByExecutionIdAndBranchId(null, "asd");
    }

    @Test
    public void testReadByExecutionIdAndBranchId_ExecutionIdEmpty() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("executionId cannot be null or empty");
        executionStateService.readByExecutionIdAndBranchId("     ", "asd");
    }

    @Test
    public void testReadByExecutionIdAndBranchId_BranchIdNull() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("branchId cannot be null or empty");
        executionStateService.readByExecutionIdAndBranchId("asd", null);
    }

    @Test
    public void testReadByExecutionIdAndBranchId_BranchIdEmpty() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("branchId cannot be null or empty");
        executionStateService.readByExecutionIdAndBranchId("asd", "      ");
    }

    @Test
    public void testReadByExecutionIdAndBranchId() {
        String executionId = UUID.randomUUID().toString();
        String branchId = UUID.randomUUID().toString();
        ExecutionState expectedExecutionState = new ExecutionState();
        when(executionStateRepository.findByExecutionIdAndBranchId(executionId, branchId)).thenReturn(expectedExecutionState);
        ExecutionState actualExecutionState = executionStateService.readByExecutionIdAndBranchId(executionId, branchId);
        assertThat(actualExecutionState).isSameAs(expectedExecutionState);
    }

    @Test
    public void readExecutionIdAndBranchIdByStatuses_NullList() {
        when(executionStateRepository.findExecutionIdByStatuses(null)).thenReturn(new ArrayList<String>());
        List<String> actualExecutionIds = executionStateService.readExecutionIdByStatuses(null);
        assertThat(actualExecutionIds).hasSize(0);
    }

    @Test
    public void readExecutionIdAndBranchIdByStatuses_EmptyList() {
        when(executionStateRepository.findExecutionIdByStatuses(anyListOf(ExecutionEnums.ExecutionStatus.class))).thenReturn(new ArrayList<String>());
        List<String> actualExecutionIds = executionStateService.readExecutionIdByStatuses(new ArrayList<ExecutionEnums.ExecutionStatus>());
        assertThat(actualExecutionIds).hasSize(0);
    }

    @Test
    public void readExecutionIdAndBranchIdByStatuses() {
        List<ExecutionEnums.ExecutionStatus> statuses = Arrays.asList(ExecutionEnums.ExecutionStatus.COMPLETED, ExecutionEnums.ExecutionStatus.CANCELED);
        List<String> expectedExecutionIds = Arrays.asList("a", "b");
        when(executionStateRepository.findExecutionIdByStatuses(statuses)).thenReturn(expectedExecutionIds);
        List<String> actualExecutionIds = executionStateService.readExecutionIdByStatuses(statuses);
        assertThat(actualExecutionIds).containsExactly(expectedExecutionIds.toArray());
    }

    @Test
    public void testreadCancelledExecution_NullValue() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("executionId cannot be null or empty");
        executionStateService.readCancelledExecution(null);
    }

    @Test
    public void testreadCancelledExecution_EmptyValue() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("executionId cannot be null or empty");
        executionStateService.readCancelledExecution("      ");
    }

    @Test
    public void testreadCancelledExecution() {
        String executionId = UUID.randomUUID().toString();
        List<ExecutionEnums.ExecutionStatus> statuses = Arrays.asList(ExecutionEnums.ExecutionStatus.CANCELED, ExecutionEnums.ExecutionStatus.PENDING_CANCEL);
        ExecutionState expectedExecutionState = new ExecutionState();
        when(executionStateRepository.findByExecutionIdAndBranchIdAndStatusIn(executionId, ExecutionState.EMPTY_BRANCH, statuses)).thenReturn(expectedExecutionState);
        ExecutionState actualExecutionState = executionStateService.readCancelledExecution(executionId);
        assertThat(actualExecutionState).isSameAs(expectedExecutionState);
    }

    @Test
    public void testcreateParentExecution_NullExecutionId() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("executionId cannot be null or empty");
        executionStateService.createParentExecution(null);
    }

    @Test
    public void testcreateParentExecution_EmptyExecutionId() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("executionId cannot be null or empty");
        executionStateService.createParentExecution("           ");
    }

    @Test
    public void testcreateParentExecution() {
        String executionId = UUID.randomUUID().toString();
        ExecutionState executionState = executionStateService.createParentExecution(executionId);
        assertThat(executionState.getExecutionId()).isEqualTo(executionId);
    }

    @Test
    public void testCreateExecutionState_NullExecutionId() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("executionId cannot be null or empty");
        executionStateService.createExecutionState(null, "Asdfsdf");
    }

    @Test
    public void testCreateExecutionState_EmptyExecutionId() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("executionId cannot be null or empty");
        executionStateService.createExecutionState("         ", "Asdfsdf");
    }

    @Test
    public void testCreateExecutionState_NullBranchId() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("branchId cannot be null or empty");
        executionStateService.createExecutionState("Asdasd", null);
    }

    @Test
    public void testCreateExecutionState_EmptyBranchId() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("branchId cannot be null or empty");
        executionStateService.createExecutionState("Asdasd", "          ");
    }

    @Test
    public void testCreateExecutionState() {
        String executionId = UUID.randomUUID().toString();
        String branchId = UUID.randomUUID().toString();
        ExecutionState executionState = executionStateService.createExecutionState(executionId, branchId);
        assertThat(executionState.getExecutionId()).isEqualTo(executionId);
        assertThat(executionState.getBranchId()).isEqualTo(branchId);
    }

    @Test
    public void testreadExecutionObject_NullExecutionId() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("executionId cannot be null or empty");
        executionStateService.readExecutionObject(null, "Asdfsdf");
    }

    @Test
    public void testreadExecutionObject_EmptyExecutionId() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("executionId cannot be null or empty");
        executionStateService.readExecutionObject("         ", "Asdfsdf");
    }

    @Test
    public void testreadExecutionObject_NullBranchId() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("branchId cannot be null or empty");
        executionStateService.readExecutionObject("Asdasd", null);
    }

    @Test
    public void testreadExecutionObject_EmptyBranchId() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("branchId cannot be null or empty");
        executionStateService.readExecutionObject("Asdasd", "          ");
    }

    @Test
    public void testreadExecutionObject() {
        String executionId = UUID.randomUUID().toString();
        String branchId = UUID.randomUUID().toString();

        Execution expectedExecution = new Execution();
        byte[] runObjectBytes = new byte[] {0,0,0};
        ExecutionState executionState = new ExecutionState();
        executionState.setExecutionObject(runObjectBytes);

        when(executionStateRepository.findByExecutionIdAndBranchId(executionId, branchId)).thenReturn(executionState);
        when(executionSerializationUtil.objFromBytes(runObjectBytes)).thenReturn(expectedExecution);

        Execution actualExecution = executionStateService.readExecutionObject(executionId, branchId);
        assertThat(actualExecution).isSameAs(expectedExecution);
    }

    @Test
    public void testupdateExecutionObject_NullExecutionId() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("executionId cannot be null or empty");
        executionStateService.updateExecutionObject(null, "Asdfsdf", null);
    }

    @Test
    public void testupdateExecutionObject_EmptyExecutionId() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("executionId cannot be null or empty");
        executionStateService.updateExecutionObject("         ", "Asdfsdf", null);
    }

    @Test
    public void testupdateExecutionObject_NullBranchId() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("branchId cannot be null or empty");
        executionStateService.updateExecutionObject("Asdasd", null, null);
    }

    @Test
    public void testupdateExecutionObject_EmptyBranchId() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("branchId cannot be null or empty");
        executionStateService.updateExecutionObject("Asdasd", "          ", null);
    }

    @Test
    public void testupdateExecutionObject() {
        String executionId = UUID.randomUUID().toString();
        String branchId = UUID.randomUUID().toString();
        Execution execution = new Execution();

        byte[] runObjectBytes = new byte[] {0,0,0};
        ExecutionState executionState = Mockito.mock(ExecutionState.class);

        when(executionStateRepository.findByExecutionIdAndBranchId(executionId, branchId)).thenReturn(executionState);
        when(executionSerializationUtil.objToBytes(execution)).thenReturn(runObjectBytes);

        executionStateService.updateExecutionObject(executionId, branchId, execution);
        verify(executionState, times(1)).setExecutionObject(runObjectBytes);
    }

    @Test
    public void testUpdateExecutionStateStatus_NullExecutionId() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("executionId cannot be null or empty");
        executionStateService.updateExecutionStateStatus(null, "Asdfsdf", null);
    }

    @Test
    public void testUpdateExecutionStateStatus_EmptyExecutionId() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("executionId cannot be null or empty");
        executionStateService.updateExecutionStateStatus("         ", "Asdfsdf", null);
    }

    @Test
    public void testUpdateExecutionStateStatus_NullBranchId() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("branchId cannot be null or empty");
        executionStateService.updateExecutionStateStatus("Asdasd", null, null);
    }

    @Test
    public void testUpdateExecutionStateStatus_EmptyBranchId() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("branchId cannot be null or empty");
        executionStateService.updateExecutionStateStatus("Asdasd", "          ", null);
    }

    @Test
    public void testUpdateExecutionStateStatus_NullStatus() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("status cannot be null");
        executionStateService.updateExecutionStateStatus("Asdasd", "asdasd", null);
    }

    @Test
    public void testUpdateExecutionStateStatus() {
        String executionId = UUID.randomUUID().toString();
        String branchId = UUID.randomUUID().toString();
        ExecutionEnums.ExecutionStatus status = ExecutionEnums.ExecutionStatus.PAUSED;

        ExecutionState executionState = Mockito.mock(ExecutionState.class);

        when(executionStateRepository.findByExecutionIdAndBranchId(executionId, branchId)).thenReturn(executionState);

        executionStateService.updateExecutionStateStatus(executionId, branchId, status);
        verify(executionState, times(1)).setStatus(status);
    }

    @Test
    public void testDeleteExecutionState_NullExecutionId() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("executionId cannot be null or empty");
        executionStateService.deleteExecutionState(null, "Asdfsdf");
    }

    @Test
    public void testDeleteExecutionState_EmptyExecutionId() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("executionId cannot be null or empty");
        executionStateService.deleteExecutionState("         ", "Asdfsdf");
    }

    @Test
    public void testDeleteExecutionState_NullBranchId() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("branchId cannot be null or empty");
        executionStateService.deleteExecutionState("Asdasd", null);
    }

    @Test
    public void testDeleteExecutionState_EmptyBranchId() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("branchId cannot be null or empty");
        executionStateService.deleteExecutionState("Asdasd", "          ");
    }

    @Test
    @DirtiesContext
    public void testDeleteExecutionState() {
        Mockito.reset(executionStateRepository);
        String executionId = UUID.randomUUID().toString();
        String branchId = UUID.randomUUID().toString();

        ExecutionState executionState = new ExecutionState();

        when(executionStateRepository.findByExecutionIdAndBranchId(executionId, branchId)).thenReturn(executionState);

        executionStateService.deleteExecutionState(executionId, branchId);
        verify(executionStateRepository, times(1)).delete(executionState);
    }

    @Configuration
    static class ExecutionStateServiceTestContext {

        @Bean
        public ExecutionStateService executionStateService() {
            return new ExecutionStateServiceImpl();
        }

        @Bean
        public ExecutionStateRepository executionStateRepository() {
            ExecutionStateRepository executionStateRepositoryMock = Mockito.mock(ExecutionStateRepository.class);
            when(executionStateRepositoryMock.save(any(ExecutionState.class))).thenAnswer(new Answer<ExecutionState>() {
                @Override
                public ExecutionState answer(InvocationOnMock invocation) throws Throwable {
                    return (ExecutionState) invocation.getArguments()[0];
                }
            });
            return executionStateRepositoryMock;
        }

        @Bean
        public ExecutionSerializationUtil executionSerializationUtil() {
            return Mockito.mock(ExecutionSerializationUtil.class);
        }

    }

}
