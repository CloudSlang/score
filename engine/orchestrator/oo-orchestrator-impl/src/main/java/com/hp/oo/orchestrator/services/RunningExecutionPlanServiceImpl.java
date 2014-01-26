package com.hp.oo.orchestrator.services;

import com.hp.oo.broker.entities.RunningExecutionPlan;
import com.hp.oo.orchestrator.repositories.RunningExecutionPlanRepository;
import com.hp.oo.broker.services.RunningExecutionPlanService;import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.IllegalArgumentException;
import java.lang.Long;
import java.lang.Override;
import java.lang.String;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: lernery
 * Date: 11/24/11
 * Time: 10:54 AM
 */
@Service("runningEP")
public final class RunningExecutionPlanServiceImpl implements RunningExecutionPlanService {

    @Autowired
    private RunningExecutionPlanRepository runningExecutionPlanRepository;

	@Override
	@Transactional
	public RunningExecutionPlan createRunningExecutionPlan(RunningExecutionPlan runningExecutionPlan) {
		if (runningExecutionPlan == null) throw new IllegalArgumentException("Running execution plan is null");
		return runningExecutionPlanRepository.save(runningExecutionPlan);
	}

	@Override
    @Transactional(readOnly = true)
    public RunningExecutionPlan readExecutionPlanById(Long id) {
        RunningExecutionPlan runningExecutionPlan= runningExecutionPlanRepository.findOne(id);
        return runningExecutionPlan;
    }

    @Override
    @Transactional
    public Long saveExecutionPlan(RunningExecutionPlan runningExecutionPlan){
        runningExecutionPlanRepository.save(runningExecutionPlan);
        return runningExecutionPlan.getId();
    }

    @Override
    @Transactional
    public byte[] getZippedExecutionPlan(Long id) {
        byte[] bytes = runningExecutionPlanRepository.getZippedExecutionPlan(id);
        return bytes;
    }

	@Override
	@Transactional
	public List<RunningExecutionPlan> readByFlowId(String flowUuid) {
		if (StringUtils.isEmpty(flowUuid)) throw new IllegalArgumentException("Flow UUID is null or empty");
		return runningExecutionPlanRepository.findByUuidCached(flowUuid);
	}
}
