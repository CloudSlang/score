package com.hp.oo.orchestrator.services.configuration;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: kravtsov
 * Date: 03/06/12
 * Time: 14:35
 * Used by Score for pause/cancel runs & stay in the worker
 */
public interface WorkerConfigurationService {

    //todo: orit - refactor method to return boolean
    List<Long> getCancelledExecutions();

    List<String> getWorkerGroups();

    boolean isExecutionPaused(Long executionId, String branchId);

    void enabled(boolean enabled);
}
