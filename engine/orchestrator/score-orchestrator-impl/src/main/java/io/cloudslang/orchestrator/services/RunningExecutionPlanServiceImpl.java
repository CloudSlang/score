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

package io.cloudslang.orchestrator.services;

import io.cloudslang.orchestrator.repositories.RunningExecutionPlanRepository;
import io.cloudslang.score.api.ExecutionPlan;
import io.cloudslang.score.facade.entities.RunningExecutionPlan;
import io.cloudslang.score.facade.services.RunningExecutionPlanService;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;


/**
 * Created by IntelliJ IDEA.
 * User: lernery
 * Date: 11/24/11
 * Time: 10:54 AM
 */
public final class RunningExecutionPlanServiceImpl implements RunningExecutionPlanService {

    private static final int IN_CLAUSE_LIMIT = 250;

    @Autowired
    private RunningExecutionPlanRepository runningExecutionPlanRepository;

    @Override
    @Transactional
    public RunningExecutionPlan createRunningExecutionPlan(RunningExecutionPlan runningExecutionPlan) {
        if (runningExecutionPlan == null)
            throw new IllegalArgumentException("Running execution plan is null");
        return runningExecutionPlanRepository.save(runningExecutionPlan);
    }

    @Override
    @Transactional
    public Long createRunningExecutionPlan(ExecutionPlan executionPlan, String executionId) {
        return createNewRunningExecutionPlan(executionPlan, executionId);
    }

    @Override
    @Transactional(readOnly = true)
    public RunningExecutionPlan readExecutionPlanById(Long id) {
        return runningExecutionPlanRepository.findOne(id);
    }


    @Override
    @Transactional(readOnly = true)
    public String getFlowUuidByRunningExecutionPlanId(Long runningExecutionPlanId) {
        RunningExecutionPlan runningExecutionPlan = readExecutionPlanById(runningExecutionPlanId);
        if (runningExecutionPlan == null)
            throw new RuntimeException("runningExecutionPlan is null");

        return runningExecutionPlan.getFlowUUID();
    }

    @Override
    @Transactional
    public void deleteRunningExecutionPlans(Collection<String> executionIds) {

        List<List<String>> executionIdsPartitioned = Lists.partition(new ArrayList<>(executionIds), IN_CLAUSE_LIMIT);
        for (List<String> list : executionIdsPartitioned) {
            runningExecutionPlanRepository.deleteByExecutionIds(list);
            runningExecutionPlanRepository.flush();
        }
    }

    private List<RunningExecutionPlan> readByFlowId(String flowUuid) {
        if (StringUtils.isEmpty(flowUuid))
            throw new IllegalArgumentException("Flow UUID is null or empty");
        return runningExecutionPlanRepository.findByUuidCached(flowUuid);
    }

    private Long createNewRunningExecutionPlan(ExecutionPlan executionPlan, String executionId) {
        //Create new and save in DB
        RunningExecutionPlan runningExecutionPlan = new RunningExecutionPlan();
        runningExecutionPlan.setFlowUUID(executionPlan.getFlowUuid());
        runningExecutionPlan.setExecutionPlan(executionPlan);
        runningExecutionPlan.setExecutionId(executionId);

        runningExecutionPlan = createRunningExecutionPlan(runningExecutionPlan);

        return runningExecutionPlan.getId();
    }
}
