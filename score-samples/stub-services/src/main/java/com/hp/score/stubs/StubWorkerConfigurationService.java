package com.hp.score.stubs;

import com.hp.oo.orchestrator.services.configuration.WorkerConfigurationService;

import java.util.List;

/**
 * User: stoneo
 * Date: 14/07/2014
 * Time: 13:30
 */
public class StubWorkerConfigurationService implements WorkerConfigurationService {
    @Override
    public List<Long> getCancelledExecutions() {
        return null;
    }

    @Override
    public List<String> getWorkerGroups() {
        return null;
    }

    @Override
    public boolean isExecutionPaused(Long executionId, String branchId) {
        return false;
    }

    @Override
    public void enabled(boolean enabled) {

    }
}
