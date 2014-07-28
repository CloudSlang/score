package org.score.samples.stubs;

import com.hp.oo.enginefacade.execution.ExecutionSummary;
import com.hp.oo.enginefacade.execution.PauseReason;
import com.hp.oo.internal.sdk.execution.Execution;
import com.hp.oo.orchestrator.services.PauseResumeService;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * User: stoneo
 * Date: 14/07/2014
 * Time: 11:13
 */
public class StubPauseResumeServiceImpl implements PauseResumeService {

    @Override
    public Long pauseExecution(Long executionId, String branchId, PauseReason reason) {
        return null;
    }

    @Override
    public void resumeExecution(Long executionId, String branchId, Map<String, Serializable> map) {

    }

    @Override
    public PauseReason writeExecutionObject(Long executionId, String branchId, Execution execution) {
        return null;
    }

    @Override
    public Set<String> readAllPausedExecutionBranchIds() {
        return null;
    }

    @Override
    public ExecutionSummary readPausedExecution(Long executionId, String branchId) {
        return null;
    }

    @Override
    public List<Long> readPauseIds(Long executionId) {
        return null;
    }
}
