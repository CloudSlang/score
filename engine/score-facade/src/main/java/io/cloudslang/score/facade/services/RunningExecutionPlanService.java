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
package io.cloudslang.score.facade.services;

import io.cloudslang.score.facade.entities.RunningExecutionPlan;
import io.cloudslang.score.api.ExecutionPlan;

import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: lernery
 * Date: 11/24/11
 * Time: 10:54 AM
 */
public interface RunningExecutionPlanService {

    /**
     * create a running execution plan
     *
     * @param runningExecutionPlan - the plan
     * @return the created plan
     */
    RunningExecutionPlan createRunningExecutionPlan(RunningExecutionPlan runningExecutionPlan);

    /**
     * check if exist such RunningExecutionPlan if not create it
     *
     * @param executionPlan - the RunningExecutionPlan
     * @param executionId   - the flow run id
     * @return the id of the exist \ created one
     */
    Long createRunningExecutionPlan(ExecutionPlan executionPlan, String executionId);

    /**
     * get Running ExecutionPlan
     *
     * @param id - the id ofthe required running execution plan
     * @return the required plan
     */
    RunningExecutionPlan readExecutionPlanById(Long id);


    /**
     * getter of the flow Uuid
     *
     * @param runningExecutionPlanId - id of the RunningExecutionPlan
     * @return the flow uuid of the runningExecutionPlanId
     */
    String getFlowUuidByRunningExecutionPlanId(Long runningExecutionPlanId);


    int deleteRunningExecutionPlans(Collection<String> executionIds);
}
