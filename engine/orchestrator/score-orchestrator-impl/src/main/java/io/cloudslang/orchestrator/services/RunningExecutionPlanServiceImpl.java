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

import io.cloudslang.score.facade.entities.RunningExecutionPlan;
import io.cloudslang.score.api.ExecutionPlan;
import io.cloudslang.orchestrator.repositories.RunningExecutionPlanRepository;
import io.cloudslang.score.facade.services.RunningExecutionPlanService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.lang.IllegalArgumentException;
import java.lang.Long;
import java.lang.Override;
import java.lang.String;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

/**
 * Created by IntelliJ IDEA.
 * User: lernery
 * Date: 11/24/11
 * Time: 10:54 AM
 */
public final class RunningExecutionPlanServiceImpl implements RunningExecutionPlanService {

    @Autowired
    private RunningExecutionPlanRepository runningExecutionPlanRepository;

    @Autowired
    private org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor updateRunningExecutionPlansExecutor;

    @Override
    @Transactional
    public RunningExecutionPlan createRunningExecutionPlan(RunningExecutionPlan runningExecutionPlan) {
        if (runningExecutionPlan == null)
            throw new IllegalArgumentException("Running execution plan is null");
        return runningExecutionPlanRepository.save(runningExecutionPlan);
    }

    @Override
    @Transactional(readOnly = true)
    public RunningExecutionPlan readExecutionPlanById(Long id) {
        return runningExecutionPlanRepository.findOne(id);
    }



    @Override
    @Transactional
    public Long getOrCreateRunningExecutionPlan(final ExecutionPlan executionPlan) {
        List<RunningExecutionPlan> existingRunningPlans = readByFlowId(executionPlan.getFlowUuid());

        //If no running execution plan existsByUuid for this UUID - create new
        if (CollectionUtils.isEmpty(existingRunningPlans)) {
            return createNewRunningExecutionPlan(executionPlan);
        }
        //If existsByUuid - check if the plans are equal
        else {
            for (RunningExecutionPlan existingRunningPlan : existingRunningPlans) {
                try {
                    if (existingRunningPlan.getExecutionPlan().getExecutionPlanUuid().equals(executionPlan.getExecutionPlanUuid())) {
                        //runningExecutionPlanRepository.incrementUseOfExecutionPlan(existingRunningPlan.getId());
                        //UseExcutionPlanIncrement tr = new UseExcutionPlanIncrement(existingRunningPlan.getId());
                        //Thread t = new Thread(tr, "UseExecutionPlan-" + existingRunningPlan.getId());
                        final Long uuid = existingRunningPlan.getId();
                        try {
                            updateRunningExecutionPlansExecutor.submit(new Runnable() {
                                @Override
                                public void run() {
                                    runningExecutionPlanRepository.incrementUseOfExecutionPlan(uuid);
                                }
                            });

                        } catch (RejectedExecutionException submitException) {

                            throw new RuntimeException("runningExecutionPlan could not update");
                        }
                        return existingRunningPlan.getId();
                    }
                } catch (Exception e)
                    {continue;}
            }
            return createNewRunningExecutionPlan(executionPlan);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public String getFlowUuidByRunningExecutionPlanId(Long runningExecutionPlanId) {
        RunningExecutionPlan runningExecutionPlan = readExecutionPlanById(runningExecutionPlanId);
        if (runningExecutionPlan == null)
            throw new RuntimeException("runningExecutionPlan is null");

        return runningExecutionPlan.getFlowUUID();
    }

    private List<RunningExecutionPlan> readByFlowId(String flowUuid) {
        if (StringUtils.isEmpty(flowUuid))
            throw new IllegalArgumentException("Flow UUID is null or empty");
        return runningExecutionPlanRepository.findByUuidCached(flowUuid);
    }

    private Long createNewRunningExecutionPlan(ExecutionPlan executionPlan) {
        //Create new and save in DB
        RunningExecutionPlan runningExecutionPlan = new RunningExecutionPlan();
        runningExecutionPlan.setFlowUUID(executionPlan.getFlowUuid());
        runningExecutionPlan.setExecutionPlan(executionPlan);
        runningExecutionPlan.setInUseCount(1L);
        runningExecutionPlan = createRunningExecutionPlan(runningExecutionPlan);

        return runningExecutionPlan.getId();
    }

    @Override
    @Transactional
    public void deleteRunningExecutionPlan(List<Long> runningPlansId)
    {
        runningExecutionPlanRepository.decrementUseOfExecutionPlan(runningPlansId);
        runningExecutionPlanRepository.deleteFinishedExecPlans(runningPlansId);
    }


    class UseExcutionPlanIncrement implements Runnable {

        private Long execPlanId;

        UseExcutionPlanIncrement(Long execPlanId) {
            this.execPlanId=execPlanId;
        }

        public void run() {
            runningExecutionPlanRepository.incrementUseOfExecutionPlan(execPlanId);

        }
    }
}
