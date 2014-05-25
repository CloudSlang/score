package com.hp.score.services;

import com.hp.oo.enginefacade.execution.ExecutionEnums;
import com.hp.oo.enginefacade.execution.ExecutionSummary;
import com.hp.oo.internal.sdk.execution.Execution;
import com.hp.oo.orchestrator.services.ExecutionSerializationUtil;
import com.hp.score.entities.RunState;
import com.hp.score.repositories.RunStateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * User: maromg
 * Date: 12/05/2014
 */
//todo create tests for this class. Do we want to validate all arguments in each method?
public class RunStateServiceImpl implements RunStateService {

    @Autowired
    private RunStateRepository runStateRepository;

    @Autowired
    private ExecutionSerializationUtil serUtil;

    @Override
    @Transactional(readOnly = true)
    public RunState readByRunIdAndBranchId(String runId, String branchId) {
        return runStateRepository.findByRunIdAndBranchId(runId, branchId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RunState> readByRunId(String runId) {
        return runStateRepository.findByRunId(runId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> readRunIdAndBranchIdByStatuses(List<ExecutionEnums.ExecutionStatus> statuses) {
        return runStateRepository.findRunIdByStatuses(statuses);
    }

    @Override
    @Transactional(readOnly = true)
    public RunState readCancelledRun(String runId) {
        return runStateRepository.findByRunIdAndBranchIdAndStatusIn(runId, ExecutionSummary.EMPTY_BRANCH, getCancelStatuses());
    }

    @Override
    @Transactional
    public void createParentRun(String runId) {
        RunState runState = new RunState();
        runState.setRunId(runId);
        runState.setBranchId(ExecutionSummary.EMPTY_BRANCH);
        runState.setStatus(ExecutionEnums.ExecutionStatus.RUNNING);
        runStateRepository.save(runState);
    }

    @Override
    @Transactional
    public void createRunState(String runId, String branchId) {
        RunState runState = new RunState();
        runState.setRunId(runId);
        runState.setBranchId(branchId);
        runState.setStatus(ExecutionEnums.ExecutionStatus.PENDING_PAUSE);
        runStateRepository.save(runState);
    }

    @Override
    @Transactional(readOnly = true)
    public Execution readRunObject(String runId, String branchId) {
        RunState runState = findByRunIdAndBranchId(runId, branchId);
        return serUtil.objFromBytes(runState.getRunObject());
    }

    @Override
    @Transactional
    public void updateRunObject(String runId, String branchId, Execution execution) {
        RunState runState = findByRunIdAndBranchId(runId, branchId);
        runState.setRunObject(serUtil.objToBytes(execution));
    }

    @Override
    @Transactional
    public void updateRunStateStatus(String runId, String branchId, ExecutionEnums.ExecutionStatus status) {
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
        RunState runState = runStateRepository.findByRunIdAndBranchId(runId, branchId);
        if (runState != null) {
            runStateRepository.delete(runState);
        }
    }
}
