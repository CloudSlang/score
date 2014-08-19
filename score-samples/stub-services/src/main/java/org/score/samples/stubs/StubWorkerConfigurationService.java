package org.score.samples.stubs;

import com.google.common.collect.Lists;
import com.hp.oo.orchestrator.services.configuration.WorkerConfigurationService;

import java.util.ArrayList;
import java.util.List;

/**
 * User: stoneo
 * Date: 14/07/2014
 * Time: 13:30
 */
public class StubWorkerConfigurationService implements WorkerConfigurationService {
    @Override
    public List<Long> getCancelledExecutions() {
        return new ArrayList();
    }

    @Override
    public List<String> getWorkerGroups() {
		return Lists.newArrayList("RAS_Operator_Path");
	}

    @Override
    public boolean isExecutionPaused(Long executionId, String branchId) {
        return false;
    }

    @Override
    public void enabled(boolean enabled) {

    }
}
