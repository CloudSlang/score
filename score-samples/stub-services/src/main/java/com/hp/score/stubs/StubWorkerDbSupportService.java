package com.hp.score.stubs;

import com.hp.oo.broker.entities.BranchContextHolder;
import com.hp.oo.broker.entities.RunningExecutionPlan;
import com.hp.oo.execution.services.dbsupport.WorkerDbSupportService;

import java.util.List;

/**
 * User: stoneo
 * Date: 14/07/2014
 * Time: 13:38
 */
public class StubWorkerDbSupportService implements WorkerDbSupportService {
    @Override
    public RunningExecutionPlan readExecutionPlanById(Long id) {
        return null;
    }

    @Override
    public List<BranchContextHolder> readBranchContextById(String splitId) {
        return null;
    }

    @Override
    public Long createBranchContext(BranchContextHolder branchContextHolder) {
        return null;
    }

    @Override
    public void deleteParallelRecordsById(String splitId) {

    }

    @Override
    public int countBranchContextBySplitId(String splitId) {
        return 0;
    }
}
