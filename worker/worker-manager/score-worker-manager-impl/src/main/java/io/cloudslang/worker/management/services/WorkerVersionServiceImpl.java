package io.cloudslang.worker.management.services;

import io.cloudslang.orchestrator.services.EngineVersionService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by kravtsov on 07/12/2015
 */

public class WorkerVersionServiceImpl implements WorkerVersionService {

    @Autowired
    private EngineVersionService engineVersionService;

    @Override
    public String getWorkerVersion() {
        return "";
    }

    @Override
    public String getWorkerVersionId() {
        return engineVersionService.getEngineVersionId();
    }
}
