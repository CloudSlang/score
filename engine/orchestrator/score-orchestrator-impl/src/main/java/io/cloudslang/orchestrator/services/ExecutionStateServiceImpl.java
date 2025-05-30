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
import io.cloudslang.score.facade.execution.ExecutionActionException;
import io.cloudslang.score.facade.execution.ExecutionActionResult;
import io.cloudslang.score.facade.execution.ExecutionStatus;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static io.cloudslang.orchestrator.entities.ExecutionState.EMPTY_BRANCH;
import static io.cloudslang.score.facade.execution.ExecutionStatus.CANCELED;
import static io.cloudslang.score.facade.execution.ExecutionStatus.PENDING_CANCEL;
import static org.springframework.util.CollectionUtils.isEmpty;

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
        return executionStateRepository.findByExecutionIdAndBranchIdAndStatusIn(executionId, EMPTY_BRANCH, getCancelStatuses());
    }

    @Override
    @Transactional
    public ExecutionState createParentExecution(Long executionId) {
        validateExecutionId(executionId);
        ExecutionState executionState = new ExecutionState();
        executionState.setExecutionId(executionId);
        executionState.setBranchId(EMPTY_BRANCH);
        executionState.setStatus(ExecutionStatus.RUNNING);
        executionState.setUpdateTime(new Date().getTime());
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
        executionState.setUpdateTime(new Date().getTime());
        return executionStateRepository.save(executionState);
    }

    @Override
    @Transactional
    public void updateExecutionStateStatus(Long executionId, String branchId, ExecutionStatus status,
                                           Date updateDate) {
        validateExecutionId(executionId);
        Validate.notNull(status, "status cannot be null");
        validateBranchId(branchId);
        Optional<ExecutionState> executionState = findByExecutionIdAndBranchIdNoException(executionId, branchId);
        if (executionState.isPresent()) {
            ExecutionState executionState1 = executionState.get();
            executionState1.setStatus(status);
            executionState1.setUpdateTime(updateDate.getTime());
        }
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
    public void updateExecutionObject(Long executionId, String branchId, Execution execution, Date updateDate) {
        validateExecutionId(executionId);
        validateBranchId(branchId);
        ExecutionState executionState = findByExecutionIdAndBranchId(executionId, branchId);
        executionState.setExecutionObject(executionSerializationUtil.objToBytes(execution));
        executionState.setUpdateTime(updateDate.getTime());
    }

    private ExecutionState findByExecutionIdAndBranchId(Long executionId, String branchId) {
        ExecutionState executionState = executionStateRepository.findByExecutionIdAndBranchId(executionId, branchId);
        if (executionState == null) {
            throw new ExecutionActionException("Could not find execution state. executionId:  " + executionId + ", branchId: " + branchId, ExecutionActionResult.FAILED_NOT_FOUND);
        }
        return executionState;
    }

    private Optional<ExecutionState> findByExecutionIdAndBranchIdNoException(Long executionId, String branchId) {
        ExecutionState executionState = executionStateRepository.findByExecutionIdAndBranchId(executionId, branchId);
        if (executionState == null) {
            return Optional.empty();
        }
        return Optional.of(executionState);
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
    public void deleteCanceledExecutionStates() {
        List<Long> executionStates = executionStateRepository.findByBranchIdAndStatusIn(EMPTY_BRANCH, PENDING_CANCEL);
        if (!isEmpty(executionStates)) {
            executionStateRepository.deleteByIds(executionStates);
        }
    }

    @Override
    public Execution getExecutionObjectForNullBranch(Long executionId) {
        validateExecutionId(executionId);
        ExecutionState executionState = readByExecutionId(executionId).get(0);
        if (executionState.getExecutionObject() != null) {
            return executionSerializationUtil.objFromBytes(executionState.getExecutionObject());
        } else {
            return null;
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<Long> findExecutionStateByStatusInAndUpdateTimeLessThanEqual(List<ExecutionStatus> statuses, long cutOffTime, PageRequest pageRequest) {
        return executionStateRepository.findByStatusInAndUpdateTimeLessThanEqual(statuses, cutOffTime, pageRequest);
    }

    @Transactional
    @Override
    public void deleteExecutionStateByIds(List<Long> toDeleteIds) {
        executionStateRepository.deleteByIds(toDeleteIds);
    }

    private void validateBranchId(String branchId) {
        Validate.notEmpty(StringUtils.trim(branchId), "branchId cannot be null or empty");
    }

    private void validateExecutionId(Long executionId) {
        Validate.notNull(executionId, "executionId cannot be null or empty");
    }
}
