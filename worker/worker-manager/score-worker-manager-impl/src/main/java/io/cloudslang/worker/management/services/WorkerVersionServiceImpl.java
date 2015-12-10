package io.cloudslang.worker.management.services;

import org.springframework.stereotype.Service;

/**
 * Created by kravtsov on 07/12/2015
 */

@Service
public class WorkerVersionServiceImpl implements WorkerVersionService {
    @Override
    public String getWorkerVersion() {
        return "";
    }

    @Override
    public String getWorkerComparableVersion() {
        return "";
    }
}
