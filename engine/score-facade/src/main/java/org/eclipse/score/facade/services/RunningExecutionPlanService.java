/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package org.eclipse.score.facade.services;

import org.eclipse.score.facade.entities.RunningExecutionPlan;
import org.eclipse.score.api.ExecutionPlan;

/**
 * Created by IntelliJ IDEA.
 * User: lernery
 * Date: 11/24/11
 * Time: 10:54 AM
 */
public interface RunningExecutionPlanService {

    /**
     * create a running execution plan
     * @param runningExecutionPlan  - the plan
     * @return  the created plan
     */
    RunningExecutionPlan createRunningExecutionPlan(RunningExecutionPlan runningExecutionPlan);

    /**
     *  get Running ExecutionPlan
     * @param id - the id ofthe required running execution plan
     * @return  the required plan
     */
    RunningExecutionPlan readExecutionPlanById(Long id);

    /**
     * check if exist such RunningExecutionPlan if not create it
     * @param executionPlan - the RunningExecutionPlan
     * @return  the id of the exist \ created one
     */
    Long getOrCreateRunningExecutionPlan(ExecutionPlan executionPlan);

    /**
     *  getter of the flow Uuid
     * @param runningExecutionPlanId  - id of the RunningExecutionPlan
     * @return  the flow uuid of the runningExecutionPlanId
     */
    String getFlowUuidByRunningExecutionPlanId(Long runningExecutionPlanId);
}
