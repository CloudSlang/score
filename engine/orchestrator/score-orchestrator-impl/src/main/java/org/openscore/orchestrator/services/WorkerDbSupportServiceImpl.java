/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package org.openscore.orchestrator.services;

import org.eclipse.score.facade.entities.RunningExecutionPlan;
import org.eclipse.score.facade.services.RunningExecutionPlanService;
import org.eclipse.score.worker.management.services.dbsupport.WorkerDbSupportService;
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
