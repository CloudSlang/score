/*
 * Licensed to Hewlett-Packard Development Company, L.P. under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
*/
package com.hp.score.facade.services;

import com.hp.score.facade.entities.RunningExecutionPlan;
import com.hp.score.api.ExecutionPlan;

import java.util.List;

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
