package io.cloudslang.worker.execution.services;

import io.cloudslang.score.api.execution.precondition.ExecutionPostconditionService;

import java.io.Serializable;
import java.util.Map;

public class StubExecutionPostconditionService implements ExecutionPostconditionService {
    @Override
    public void postExecutionWork(Map<String, Serializable> context, Map<String, Serializable> runtimeValues) {
        /* If no implementation for the interface exists then there is no work to be done after the execution. */
    }
}
