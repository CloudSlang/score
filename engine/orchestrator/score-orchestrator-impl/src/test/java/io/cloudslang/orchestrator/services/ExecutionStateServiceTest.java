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


import io.cloudslang.orchestrator.entities.ExecutionState;
import io.cloudslang.orchestrator.repositories.ExecutionStateRepository;
import io.cloudslang.score.facade.entities.Execution;
import io.cloudslang.score.facade.execution.ExecutionStatus;
import org.junit.Assert;
import org.junit.Test;
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
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * User:
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

    @Test
    public void testReadByExecutionId_NullValue() {
        IllegalArgumentException exception = Assert.assertThrows(IllegalArgumentException.class,
                () -> executionStateService.readByExecutionId(null));
        Assert.assertEquals("executionId cannot be null or empty", exception.getMessage());
    }

    @Test
    public void testReadByExecutionId_EmptyValue() {
        IllegalArgumentException exception = Assert.assertThrows(IllegalArgumentException.class,
                () -> executionStateService.readByExecutionId(null));
        Assert.assertEquals("executionId cannot be null or empty", exception.getMessage());
    }

    @Test
    public void testReadByExecutionId() {
        Long executionId = 123L;
        ExecutionState expectedExecutionState = new ExecutionState();
        when(executionStateRepository.findByExecutionId(executionId)).thenReturn(Arrays.asList(expectedExecutionState));
        List<ExecutionState> actualExecutionStates = executionStateService.readByExecutionId(executionId);
        assertThat(actualExecutionStates).containsExactly(expectedExecutionState);
    }

    @Test
    public void testReadByExecutionIdAndBranchId_ExecutionIdNull() {
        IllegalArgumentException exception = Assert.assertThrows(IllegalArgumentException.class,
                () -> executionStateService.readByExecutionIdAndBranchId(null, "asd"));
        Assert.assertEquals("executionId cannot be null or empty", exception.getMessage());
    }

    @Test
    public void testReadByExecutionIdAndBranchId_ExecutionIdEmpty() {
        IllegalArgumentException exception = Assert.assertThrows(IllegalArgumentException.class,
                () -> executionStateService.readByExecutionIdAndBranchId(null, "asd"));
        Assert.assertEquals("executionId cannot be null or empty", exception.getMessage());
    }

    @Test
    public void testReadByExecutionIdAndBranchId_BranchIdNull() {
        IllegalArgumentException exception = Assert.assertThrows(IllegalArgumentException.class,
                () -> executionStateService.readByExecutionIdAndBranchId(123L, null));
        Assert.assertEquals("branchId cannot be null or empty", exception.getMessage());
    }

    @Test
    public void testReadByExecutionIdAndBranchId_BranchIdEmpty() {
        IllegalArgumentException exception = Assert.assertThrows(IllegalArgumentException.class,
                () -> executionStateService.readByExecutionIdAndBranchId(123L, "      "));
        Assert.assertEquals("branchId cannot be null or empty", exception.getMessage());
    }

    @Test
    public void testReadByExecutionIdAndBranchId() {
        Long executionId = 123L;
        String branchId = UUID.randomUUID().toString();
        ExecutionState expectedExecutionState = new ExecutionState();
        when(executionStateRepository.findByExecutionIdAndBranchId(executionId, branchId)).thenReturn(expectedExecutionState);
        ExecutionState actualExecutionState = executionStateService.readByExecutionIdAndBranchId(executionId, branchId);
        assertThat(actualExecutionState).isSameAs(expectedExecutionState);
    }

    @Test
    public void readExecutionIdAndBranchIdByStatuses_NullList() {
        when(executionStateRepository.findExecutionIdByStatuses(null)).thenReturn(new ArrayList<Long>());
        List<Long> actualExecutionIds = executionStateService.readExecutionIdByStatuses(null);
        assertThat(actualExecutionIds).hasSize(0);
    }

    @Test
    public void readExecutionIdAndBranchIdByStatuses_EmptyList() {
        when(executionStateRepository.findExecutionIdByStatuses(anyList())).thenReturn(new ArrayList<Long>());
        List<Long> actualExecutionIds = executionStateService.readExecutionIdByStatuses(new ArrayList<ExecutionStatus>());
        assertThat(actualExecutionIds).hasSize(0);
    }

    @Test
    public void readExecutionIdAndBranchIdByStatuses() {
        List<ExecutionStatus> statuses = Arrays.asList(ExecutionStatus.COMPLETED, ExecutionStatus.CANCELED);
        List<Long> expectedExecutionIds = Arrays.asList(1L, 2L);
        when(executionStateRepository.findExecutionIdByStatuses(statuses)).thenReturn(expectedExecutionIds);
        List<Long> actualExecutionIds = executionStateService.readExecutionIdByStatuses(statuses);
        assertThat(actualExecutionIds).containsExactly(expectedExecutionIds.toArray());
    }

    @Test
    public void testreadCancelledExecution_NullValue() {
        IllegalArgumentException exception = Assert.assertThrows(IllegalArgumentException.class,
                () -> executionStateService.readCancelledExecution(null));
        Assert.assertEquals("executionId cannot be null or empty", exception.getMessage());
    }

    @Test
    public void testreadCancelledExecution_EmptyValue() {
        IllegalArgumentException exception = Assert.assertThrows(IllegalArgumentException.class,
                () -> executionStateService.readCancelledExecution(null));
        Assert.assertEquals("executionId cannot be null or empty", exception.getMessage());
    }

    @Test
    public void testreadCancelledExecution() {
        Long executionId = 123L;
        List<ExecutionStatus> statuses = Arrays.asList(ExecutionStatus.CANCELED, ExecutionStatus.PENDING_CANCEL);
        ExecutionState expectedExecutionState = new ExecutionState();
        when(executionStateRepository.findByExecutionIdAndBranchIdAndStatusIn(executionId, ExecutionState.EMPTY_BRANCH, statuses)).thenReturn(expectedExecutionState);
        ExecutionState actualExecutionState = executionStateService.readCancelledExecution(executionId);
        assertThat(actualExecutionState).isSameAs(expectedExecutionState);
    }

    @Test
    public void testcreateParentExecution_NullExecutionId() {
        IllegalArgumentException exception = Assert.assertThrows(IllegalArgumentException.class,
                () -> executionStateService.createParentExecution(null));
        Assert.assertEquals("executionId cannot be null or empty", exception.getMessage());
    }

    @Test
    public void testcreateParentExecution_EmptyExecutionId() {
        IllegalArgumentException exception = Assert.assertThrows(IllegalArgumentException.class,
                () -> executionStateService.createParentExecution(null));
        Assert.assertEquals("executionId cannot be null or empty", exception.getMessage());
    }

    @Test
    public void testcreateParentExecution() {
        Long executionId = 123L;
        ExecutionState executionState = executionStateService.createParentExecution(executionId);
        assertThat(executionState.getExecutionId()).isEqualTo(executionId);
    }

    @Test
    public void testCreateExecutionState_NullExecutionId() {
        IllegalArgumentException exception = Assert.assertThrows(IllegalArgumentException.class,
                () -> executionStateService.createExecutionState(null, "Asdfsdf"));
        Assert.assertEquals("executionId cannot be null or empty", exception.getMessage());
    }

    @Test
    public void testCreateExecutionState_EmptyExecutionId() {
        IllegalArgumentException exception = Assert.assertThrows(IllegalArgumentException.class,
                () -> executionStateService.createExecutionState(null, "Asdfsdf"));
        Assert.assertEquals("executionId cannot be null or empty", exception.getMessage());
    }

    @Test
    public void testCreateExecutionState_NullBranchId() {
        IllegalArgumentException exception = Assert.assertThrows(IllegalArgumentException.class,
                () -> executionStateService.createExecutionState(123L, null));
        Assert.assertEquals("branchId cannot be null or empty", exception.getMessage());
    }

    @Test
    public void testCreateExecutionState_EmptyBranchId() {
        IllegalArgumentException exception = Assert.assertThrows(IllegalArgumentException.class,
                () -> executionStateService.createExecutionState(123L, "          "));
        Assert.assertEquals("branchId cannot be null or empty", exception.getMessage());
    }

    @Test
    public void testCreateExecutionState() {
        Long executionId = 123L;
        String branchId = UUID.randomUUID().toString();
        ExecutionState executionState = executionStateService.createExecutionState(executionId, branchId);
        assertThat(executionState.getExecutionId()).isEqualTo(executionId);
        assertThat(executionState.getBranchId()).isEqualTo(branchId);
    }

    @Test
    public void testReadExecutionObject_NullExecutionId() {
        IllegalArgumentException exception = Assert.assertThrows(IllegalArgumentException.class,
                () -> executionStateService.readExecutionObject(null, "Asdfsdf"));
        Assert.assertEquals("executionId cannot be null or empty", exception.getMessage());
    }

    @Test
    public void testReadExecutionObject_EmptyExecutionId() {
        IllegalArgumentException exception = Assert.assertThrows(IllegalArgumentException.class,
                () -> executionStateService.readExecutionObject(null, "Asdfsdf"));
        Assert.assertEquals("executionId cannot be null or empty", exception.getMessage());
    }

    @Test
    public void testReadExecutionObject_NullBranchId() {
        IllegalArgumentException exception = Assert.assertThrows(IllegalArgumentException.class,
                () -> executionStateService.readExecutionObject(123L, null));
        Assert.assertEquals("branchId cannot be null or empty", exception.getMessage());
    }

    @Test
    public void testReadExecutionObject_EmptyBranchId() {
        IllegalArgumentException exception = Assert.assertThrows(IllegalArgumentException.class,
                () -> executionStateService.readExecutionObject(123L, "          "));
        Assert.assertEquals("branchId cannot be null or empty", exception.getMessage());
    }

    @Test
    public void testReadExecutionObject() {
        Long executionId = 123L;
        String branchId = UUID.randomUUID().toString();

        Execution expectedExecution = new Execution();
        byte[] runObjectBytes = new byte[]{0, 0, 0};
        ExecutionState executionState = new ExecutionState();
        executionState.setExecutionObject(runObjectBytes);

        when(executionStateRepository.findByExecutionIdAndBranchId(executionId, branchId)).thenReturn(executionState);
        when(executionSerializationUtil.objFromBytes(runObjectBytes)).thenReturn(expectedExecution);

        Execution actualExecution = executionStateService.readExecutionObject(executionId, branchId);
        assertThat(actualExecution).isSameAs(expectedExecution);
    }

    @Test
    public void testReadNullExecutionObject() {
        Long executionId = 123L;
        String branchId = UUID.randomUUID().toString();

        ExecutionState executionState = new ExecutionState();
        executionState.setExecutionObject(null);

        when(executionStateRepository.findByExecutionIdAndBranchId(executionId, branchId)).thenReturn(executionState);

        Execution actualExecution = executionStateService.readExecutionObject(executionId, branchId);
        assertThat(actualExecution).isNull();
    }

    @Test
    public void testUpdateExecutionObject_NullExecutionId() {
        IllegalArgumentException exception = Assert.assertThrows(IllegalArgumentException.class,
                () -> executionStateService.updateExecutionObject(null, "Asdfsdf", null, new Date()));
        Assert.assertEquals("executionId cannot be null or empty", exception.getMessage());
    }

    @Test
    public void testUpdateExecutionObject_EmptyExecutionId() {
        IllegalArgumentException exception = Assert.assertThrows(IllegalArgumentException.class,
                () -> executionStateService.updateExecutionObject(null, "Asdfsdf", null, new Date()));
        Assert.assertEquals("executionId cannot be null or empty", exception.getMessage());
    }

    @Test
    public void testUpdateExecutionObject_NullBranchId() {
        IllegalArgumentException exception = Assert.assertThrows(IllegalArgumentException.class,
                () -> executionStateService.updateExecutionObject(123L, null, null, new Date()));
        Assert.assertEquals("branchId cannot be null or empty", exception.getMessage());
    }

    @Test
    public void testUpdateExecutionObject_EmptyBranchId() {
        IllegalArgumentException exception = Assert.assertThrows(IllegalArgumentException.class,
                () -> executionStateService.updateExecutionObject(123L, "          ", null, new Date()));
        Assert.assertEquals("branchId cannot be null or empty", exception.getMessage());
    }

    @Test
    public void testUpdateExecutionObject() {
        Long executionId = 123L;
        String branchId = UUID.randomUUID().toString();
        Execution execution = new Execution();
        Date date = new Date();

        byte[] runObjectBytes = new byte[]{0, 0, 0};
        ExecutionState executionState = Mockito.mock(ExecutionState.class);

        when(executionStateRepository.findByExecutionIdAndBranchId(executionId, branchId)).thenReturn(executionState);
        when(executionSerializationUtil.objToBytes(execution)).thenReturn(runObjectBytes);

        executionStateService.updateExecutionObject(executionId, branchId, execution, date);
        verify(executionState, times(1)).setExecutionObject(runObjectBytes);
    }

    @Test
    public void testUpdateExecutionStateStatus_NullExecutionId() {
        IllegalArgumentException exception = Assert.assertThrows(IllegalArgumentException.class,
                () -> executionStateService.updateExecutionStateStatus(null, "Asdfsdf", null));
        Assert.assertEquals("executionId cannot be null or empty", exception.getMessage());
    }

    @Test
    public void testUpdateExecutionStateStatus_EmptyExecutionId() {
        IllegalArgumentException exception = Assert.assertThrows(IllegalArgumentException.class,
                () -> executionStateService.updateExecutionStateStatus(null, "Asdfsdf", null));
        Assert.assertEquals("executionId cannot be null or empty", exception.getMessage());
    }

    @Test
    public void testUpdateExecutionStateStatus_NullBranchId() {
        IllegalArgumentException exception = Assert.assertThrows(IllegalArgumentException.class,
                () -> executionStateService.updateExecutionStateStatus(123L, null, null));
        Assert.assertEquals("branchId cannot be null or empty", exception.getMessage());
    }

    @Test
    public void testUpdateExecutionStateStatus_EmptyBranchId() {
        IllegalArgumentException exception = Assert.assertThrows(IllegalArgumentException.class,
                () -> executionStateService.updateExecutionStateStatus(123L, "          ", null));
        Assert.assertEquals("branchId cannot be null or empty", exception.getMessage());
    }

    @Test
    public void testUpdateExecutionStateStatus_NullStatus() {
        IllegalArgumentException exception = Assert.assertThrows(IllegalArgumentException.class,
                () -> executionStateService.updateExecutionStateStatus(123L, "asdasd", null));
        Assert.assertEquals("status cannot be null", exception.getMessage());
    }

    @Test
    public void testUpdateExecutionStateStatus() {
        Long executionId = 123L;
        String branchId = UUID.randomUUID().toString();
        ExecutionStatus status = ExecutionStatus.PAUSED;

        ExecutionState executionState = Mockito.mock(ExecutionState.class);

        when(executionStateRepository.findByExecutionIdAndBranchId(executionId, branchId)).thenReturn(executionState);

        executionStateService.updateExecutionStateStatus(executionId, branchId, status);
        verify(executionState, times(1)).setStatus(status);
    }

    @Test
    public void testDeleteExecutionState_NullExecutionId() {
        IllegalArgumentException exception = Assert.assertThrows(IllegalArgumentException.class,
                () -> executionStateService.deleteExecutionState(null, "Asdfsdf"));
        Assert.assertEquals("executionId cannot be null or empty", exception.getMessage());
    }

    @Test
    public void testDeleteExecutionState_EmptyExecutionId() {
        IllegalArgumentException exception = Assert.assertThrows(IllegalArgumentException.class,
                () -> executionStateService.deleteExecutionState(null, "Asdfsdf"));
        Assert.assertEquals("executionId cannot be null or empty", exception.getMessage());
    }

    @Test
    public void testDeleteExecutionState_NullBranchId() {
        IllegalArgumentException exception = Assert.assertThrows(IllegalArgumentException.class,
                () -> executionStateService.deleteExecutionState(123L, null));
        Assert.assertEquals("branchId cannot be null or empty", exception.getMessage());
    }

    @Test
    public void testDeleteExecutionState_EmptyBranchId() {
        IllegalArgumentException exception = Assert.assertThrows(IllegalArgumentException.class,
                () -> executionStateService.deleteExecutionState(123L, "          "));
        Assert.assertEquals("branchId cannot be null or empty", exception.getMessage());
    }

    @Test
    @DirtiesContext
    public void testDeleteExecutionState() {
        Mockito.reset(executionStateRepository);
        Long executionId = 123L;
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
