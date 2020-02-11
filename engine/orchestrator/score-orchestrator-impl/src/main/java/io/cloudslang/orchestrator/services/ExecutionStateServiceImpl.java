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

import static io.cloudslang.score.facade.execution.ExecutionStatus.CANCELED;
import static io.cloudslang.score.facade.execution.ExecutionStatus.PENDING_CANCEL;
import static java.util.stream.Collectors.toSet;
import static org.springframework.util.CollectionUtils.isEmpty;

import io.cloudslang.score.facade.execution.ExecutionActionException;
import io.cloudslang.score.facade.execution.ExecutionActionResult;
import io.cloudslang.score.facade.execution.ExecutionStatus;
import io.cloudslang.score.facade.entities.Execution;
import io.cloudslang.orchestrator.entities.ExecutionState;
import io.cloudslang.orchestrator.repositories.ExecutionStateRepository;
import java.util.Collection;
import java.util.Set;
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
            throw new ExecutionActionException("Could not find execution state. executionId:  " + executionId + ", branchId: " + branchId, ExecutionActionResult.FAILED_NOT_FOUND);
        }
        return executionState;
    }

    private List<ExecutionStatus> getCancelStatuses() {
        return Arrays.asList(CANCELED, PENDING_CANCEL);
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

    @Override
    public void deleteExecutionStateByIds(Collection<String> executionId, String branchId) {
        Set<Long> executionIds = executionId.stream().map(Long::valueOf).collect(toSet());
        for (Long id : executionIds) {
            validateExecutionId(id);
        }
        validateBranchId(branchId);
        List<ExecutionState> executionStates = executionStateRepository.findByBranchIdAndExecutionIdInAndStatus(branchId, executionIds, PENDING_CANCEL);
        if (!isEmpty(executionStates)) {
            executionStateRepository.deleteByIds(executionIds);
        }
    }

    private void validateBranchId(String branchId) {
        Validate.notEmpty(StringUtils.trim(branchId), "branchId cannot be null or empty");
    }

    private void validateExecutionId(Long executionId) {
        Validate.notNull(executionId, "executionId cannot be null or empty");
    }
}
