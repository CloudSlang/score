package com.hp.score.orchestrator.services;

import com.hp.score.facade.entities.RunningExecutionPlan;
import com.hp.score.facade.services.RunningExecutionPlanService;
import com.hp.score.worker.management.services.dbsupport.WorkerDbSupportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;

/**
 * User: stoneo
 * Date: 14/07/2014
 * Time: 13:38
 */
public class WorkerDbSupportServiceImpl implements WorkerDbSupportService {

    @Autowired
    private RunningExecutionPlanService runningExecutionPlanService;

    @Override
    @Cacheable("running_execution_plans")
    public RunningExecutionPlan readExecutionPlanById(Long runningExecutionPlanId) {
        return runningExecutionPlanService.readExecutionPlanById(runningExecutionPlanId);
    }
}
