package com.hp.oo.orchestrator.services;

import com.hp.oo.broker.entities.BranchContextHolder;
import com.hp.oo.broker.entities.RunningExecutionPlan;
import com.hp.oo.broker.services.ParallelPersistenceService;
import com.hp.oo.broker.services.RunningExecutionPlanService;
import com.hp.oo.execution.services.dbsupport.WorkerDbSupportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;

/**
 * User: stoneo
 * Date: 14/07/2014
 * Time: 13:38
 */
public class WorkerDbSupportServiceImpl implements WorkerDbSupportService {

    @Autowired
    private RunningExecutionPlanService runningExecutionPlanService;

    //todo: remove the old non-blocking interface
    @Autowired
    private ParallelPersistenceService parallelPersistenceService;

    @Override
    @Cacheable("running_execution_plans")
    public RunningExecutionPlan readExecutionPlanById(Long runningExecutionPlanId) {
        return runningExecutionPlanService.readExecutionPlanById(runningExecutionPlanId);
    }

    @Override
    public List<BranchContextHolder> readBranchContextById(String splitId) {
        return parallelPersistenceService.readBranchContextById(splitId);
    }

    @Override
    public Long createBranchContext(BranchContextHolder branchContextHolder) {
        return parallelPersistenceService.createBranchContext(branchContextHolder).getId();
    }

    @Override
    public void deleteParallelRecordsById(String splitId) {
        parallelPersistenceService.deleteParallelRecordsById(splitId);
    }

    @Override
    public int countBranchContextBySplitId(String splitId) {
        return parallelPersistenceService.countBranchContextBySplitId(splitId);
    }
}
