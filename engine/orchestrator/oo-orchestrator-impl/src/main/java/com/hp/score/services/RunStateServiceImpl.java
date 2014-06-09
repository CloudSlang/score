package com.hp.score.services;

import com.hp.oo.enginefacade.execution.ExecutionEnums;
import com.hp.oo.internal.sdk.execution.Execution;
import com.hp.oo.orchestrator.services.ExecutionSerializationUtil;
import com.hp.score.entities.RunState;
import com.hp.score.repositories.RunStateRepository;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * User: maromg
 * Date: 12/05/2014
 */
public class RunStateServiceImpl implements RunStateService {

    @Autowired
    private RunStateRepository runStateRepository;

    @Autowired
    private ExecutionSerializationUtil executionSerializationUtil;

    @Override
    @Transactional(readOnly = true)
    public RunState readByRunIdAndBranchId(String runId, String branchId) {
        validateRunId(runId);
        validateBranchId(branchId);
        return runStateRepository.findByRunIdAndBranchId(runId, branchId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RunState> readByRunId(String runId) {
        validateRunId(runId);
        return runStateRepository.findByRunId(runId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> readRunIdByStatuses(List<ExecutionEnums.ExecutionStatus> statuses) {
        return runStateRepository.findRunIdByStatuses(statuses);
    }

    @Override
    @Transactional(readOnly = true)
    public RunState readCancelledRun(String runId) {
        validateRunId(runId);
        return runStateRepository.findByRunIdAndBranchIdAndStatusIn(runId, RunState.EMPTY_BRANCH, getCancelStatuses());
    }

    @Override
    @Transactional
    public RunState createParentRun(String runId) {
        validateRunId(runId);
        RunState runState = new RunState();
        runState.setRunId(runId);
        runState.setBranchId(RunState.EMPTY_BRANCH);
        runState.setStatus(ExecutionEnums.ExecutionStatus.RUNNING);
        return runStateRepository.save(runState);
    }

    @Override
    @Transactional
    public RunState createRunState(String runId, String branchId) {
        validateRunId(runId);
        validateBranchId(branchId);
        RunState runState = new RunState();
        runState.setRunId(runId);
        runState.setBranchId(branchId);
        runState.setStatus(ExecutionEnums.ExecutionStatus.PENDING_PAUSE);
        return runStateRepository.save(runState);
    }

    @Override
    @Transactional(readOnly = true)
    public Execution readRunObject(String runId, String branchId) {
        validateRunId(runId);
        validateBranchId(branchId);
        RunState runState = findByRunIdAndBranchId(runId, branchId);
        return executionSerializationUtil.objFromBytes(runState.getRunObject());
    }

    @Override
    @Transactional
    public void updateRunObject(String runId, String branchId, Execution execution) {
        validateRunId(runId);
        validateBranchId(branchId);
        RunState runState = findByRunIdAndBranchId(runId, branchId);
        runState.setRunObject(executionSerializationUtil.objToBytes(execution));
    }

    @Override
    @Transactional
    public void updateRunStateStatus(String runId, String branchId, ExecutionEnums.ExecutionStatus status) {
        validateRunId(runId);
        validateBranchId(branchId);
        Validate.notNull(status, "status cannot be null");
        RunState runState = findByRunIdAndBranchId(runId, branchId);
        runState.setStatus(status);
    }

    private RunState findByRunIdAndBranchId(String runId, String branchId) {
        RunState runState = runStateRepository.findByRunIdAndBranchId(runId, branchId);
        if (runState == null) {
            throw new RuntimeException("Could not find run state. runId:  " + runId + ", branchId: " + branchId);
        }
        return runState;
    }

    private List<ExecutionEnums.ExecutionStatus> getCancelStatuses() {
        return Arrays.asList(ExecutionEnums.ExecutionStatus.CANCELED, ExecutionEnums.ExecutionStatus.PENDING_CANCEL);
    }

    @Override
    public void deleteRunState(String runId, String branchId) {
        validateRunId(runId);
        validateBranchId(branchId);
        RunState runState = runStateRepository.findByRunIdAndBranchId(runId, branchId);
        if (runState != null) {
            runStateRepository.delete(runState);
        }
    }

    private void validateBranchId(String branchId) {
        Validate.notEmpty(StringUtils.trim(branchId), "branchId cannot be null or empty");
    }

    private void validateRunId(String runId) {
        Validate.notEmpty(StringUtils.trim(runId), "runId cannot be null or empty");
    }
}
