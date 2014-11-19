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
package org.eclipse.score.orchestrator.services;

import org.eclipse.score.facade.execution.ExecutionStatus;
import org.eclipse.score.facade.entities.Execution;
import org.eclipse.score.orchestrator.entities.ExecutionState;
import org.eclipse.score.orchestrator.repositories.ExecutionStateRepository;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * User:
 * Date: 12/05/2014
 */
public class ExecutionStateServiceImpl implements ExecutionStateService {

    @Autowired
    private ExecutionStateRepository executionStateRepository;

    @Autowired
    private ExecutionSerializationUtil executionSerializationUtil;

    @Override
    @Transactional(readOnly = true)
    public ExecutionState readByExecutionIdAndBranchId(Long executionId, String branchId) {
        validateExecutionId(executionId);
        validateBranchId(branchId);
        return executionStateRepository.findByExecutionIdAndBranchId(executionId, branchId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExecutionState> readByExecutionId(Long executionId) {
        validateExecutionId(executionId);
        return executionStateRepository.findByExecutionId(executionId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> readExecutionIdByStatuses(List<ExecutionStatus> statuses) {
        return executionStateRepository.findExecutionIdByStatuses(statuses);
    }

    @Override
    @Transactional(readOnly = true)
    public ExecutionState readCancelledExecution(Long executionId) {
        validateExecutionId(executionId);
        return executionStateRepository.findByExecutionIdAndBranchIdAndStatusIn(executionId, ExecutionState.EMPTY_BRANCH, getCancelStatuses());
    }

    @Override
    @Transactional
    public ExecutionState createParentExecution(Long executionId) {
        validateExecutionId(executionId);
        ExecutionState executionState = new ExecutionState();
        executionState.setExecutionId(executionId);
        executionState.setBranchId(ExecutionState.EMPTY_BRANCH);
        executionState.setStatus(ExecutionStatus.RUNNING);
        return executionStateRepository.save(executionState);
    }

    @Override
    @Transactional
    public ExecutionState createExecutionState(Long executionId, String branchId) {
        validateExecutionId(executionId);
        validateBranchId(branchId);
        ExecutionState executionState = new ExecutionState();
        executionState.setExecutionId(executionId);
        executionState.setBranchId(branchId);
        executionState.setStatus(ExecutionStatus.PENDING_PAUSE);
        return executionStateRepository.save(executionState);
    }

    @Override
    @Transactional(readOnly = true)
    public Execution readExecutionObject(Long executionId, String branchId) {
        validateExecutionId(executionId);
        validateBranchId(branchId);
        ExecutionState executionState = findByExecutionIdAndBranchId(executionId, branchId);
        if (executionState.getExecutionObject() != null) {
            return executionSerializationUtil.objFromBytes(executionState.getExecutionObject());
        } else {
            return null;
        }
    }

    @Override
    @Transactional
    public void updateExecutionObject(Long executionId, String branchId, Execution execution) {
        validateExecutionId(executionId);
        validateBranchId(branchId);
        ExecutionState executionState = findByExecutionIdAndBranchId(executionId, branchId);
        executionState.setExecutionObject(executionSerializationUtil.objToBytes(execution));
    }

    @Override
    @Transactional
    public void updateExecutionStateStatus(Long executionId, String branchId, ExecutionStatus status) {
        validateExecutionId(executionId);
        validateBranchId(branchId);
        Validate.notNull(status, "status cannot be null");
        ExecutionState executionState = findByExecutionIdAndBranchId(executionId, branchId);
        executionState.setStatus(status);
    }

    private ExecutionState findByExecutionIdAndBranchId(Long executionId, String branchId) {
        ExecutionState executionState = executionStateRepository.findByExecutionIdAndBranchId(executionId, branchId);
        if (executionState == null) {
            throw new RuntimeException("Could not find execution state. executionId:  " + executionId + ", branchId: " + branchId);
        }
        return executionState;
    }

    private List<ExecutionStatus> getCancelStatuses() {
        return Arrays.asList(ExecutionStatus.CANCELED, ExecutionStatus.PENDING_CANCEL);
    }

    @Override
    public void deleteExecutionState(Long executionId, String branchId) {
        validateExecutionId(executionId);
        validateBranchId(branchId);
        ExecutionState executionState = executionStateRepository.findByExecutionIdAndBranchId(executionId, branchId);
        if (executionState != null) {
            executionStateRepository.delete(executionState);
        }
    }

    private void validateBranchId(String branchId) {
        Validate.notEmpty(StringUtils.trim(branchId), "branchId cannot be null or empty");
    }

    private void validateExecutionId(Long executionId) {
        Validate.notNull(executionId, "executionId cannot be null or empty");
    }
}
