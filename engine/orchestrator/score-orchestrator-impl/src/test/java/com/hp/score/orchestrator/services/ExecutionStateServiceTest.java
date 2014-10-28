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


import com.hp.score.facade.execution.ExecutionStatus;
import com.hp.score.facade.entities.Execution;
import com.hp.score.orchestrator.entities.ExecutionState;
import com.hp.score.orchestrator.repositories.ExecutionStateRepository;
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
        executionStateService.readByExecutionId(null);
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
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("executionId cannot be null or empty");
        executionStateService.readByExecutionIdAndBranchId(null, "asd");
    }

    @Test
    public void testReadByExecutionIdAndBranchId_ExecutionIdEmpty() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("executionId cannot be null or empty");
        executionStateService.readByExecutionIdAndBranchId(null, "asd");
    }

    @Test
    public void testReadByExecutionIdAndBranchId_BranchIdNull() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("branchId cannot be null or empty");
        executionStateService.readByExecutionIdAndBranchId(123L, null);
    }

    @Test
    public void testReadByExecutionIdAndBranchId_BranchIdEmpty() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("branchId cannot be null or empty");
        executionStateService.readByExecutionIdAndBranchId(123L, "      ");
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
        when(executionStateRepository.findExecutionIdByStatuses(anyListOf(ExecutionStatus.class))).thenReturn(new ArrayList<Long>());
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
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("executionId cannot be null or empty");
        executionStateService.readCancelledExecution(null);
    }

    @Test
    public void testreadCancelledExecution_EmptyValue() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("executionId cannot be null or empty");
        executionStateService.readCancelledExecution(null);
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
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("executionId cannot be null or empty");
        executionStateService.createParentExecution(null);
    }

    @Test
    public void testcreateParentExecution_EmptyExecutionId() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("executionId cannot be null or empty");
        executionStateService.createParentExecution(null);
    }

    @Test
    public void testcreateParentExecution() {
        Long executionId = 123L;
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
        executionStateService.createExecutionState(null, "Asdfsdf");
    }

    @Test
    public void testCreateExecutionState_NullBranchId() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("branchId cannot be null or empty");
        executionStateService.createExecutionState(123L, null);
    }

    @Test
    public void testCreateExecutionState_EmptyBranchId() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("branchId cannot be null or empty");
        executionStateService.createExecutionState(123L, "          ");
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
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("executionId cannot be null or empty");
        executionStateService.readExecutionObject(null, "Asdfsdf");
    }

    @Test
    public void testReadExecutionObject_EmptyExecutionId() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("executionId cannot be null or empty");
        executionStateService.readExecutionObject(null, "Asdfsdf");
    }

    @Test
    public void testReadExecutionObject_NullBranchId() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("branchId cannot be null or empty");
        executionStateService.readExecutionObject(123L, null);
    }

    @Test
    public void testReadExecutionObject_EmptyBranchId() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("branchId cannot be null or empty");
        executionStateService.readExecutionObject(123L, "          ");
    }

    @Test
    public void testReadExecutionObject() {
        Long executionId = 123L;
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
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("executionId cannot be null or empty");
        executionStateService.updateExecutionObject(null, "Asdfsdf", null);
    }

    @Test
    public void testUpdateExecutionObject_EmptyExecutionId() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("executionId cannot be null or empty");
        executionStateService.updateExecutionObject(null, "Asdfsdf", null);
    }

    @Test
    public void testUpdateExecutionObject_NullBranchId() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("branchId cannot be null or empty");
        executionStateService.updateExecutionObject(123L, null, null);
    }

    @Test
    public void testUpdateExecutionObject_EmptyBranchId() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("branchId cannot be null or empty");
        executionStateService.updateExecutionObject(123L, "          ", null);
    }

    @Test
    public void testUpdateExecutionObject() {
        Long executionId = 123L;
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
        executionStateService.updateExecutionStateStatus(null, "Asdfsdf", null);
    }

    @Test
    public void testUpdateExecutionStateStatus_NullBranchId() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("branchId cannot be null or empty");
        executionStateService.updateExecutionStateStatus(123L, null, null);
    }

    @Test
    public void testUpdateExecutionStateStatus_EmptyBranchId() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("branchId cannot be null or empty");
        executionStateService.updateExecutionStateStatus(123L, "          ", null);
    }

    @Test
    public void testUpdateExecutionStateStatus_NullStatus() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("status cannot be null");
        executionStateService.updateExecutionStateStatus(123L, "asdasd", null);
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
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("executionId cannot be null or empty");
        executionStateService.deleteExecutionState(null, "Asdfsdf");
    }

    @Test
    public void testDeleteExecutionState_EmptyExecutionId() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("executionId cannot be null or empty");
        executionStateService.deleteExecutionState(null, "Asdfsdf");
    }

    @Test
    public void testDeleteExecutionState_NullBranchId() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("branchId cannot be null or empty");
        executionStateService.deleteExecutionState(123L, null);
    }

    @Test
    public void testDeleteExecutionState_EmptyBranchId() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("branchId cannot be null or empty");
        executionStateService.deleteExecutionState(123L, "          ");
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
