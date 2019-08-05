package io.cloudslang.worker.execution.services;

import io.cloudslang.score.api.execution.precondition.ExecutionPreconditionsService;

public class StubExecutionPreconditionsService implements ExecutionPreconditionsService {

    @Override
    public boolean canExecute() {
        /*
         * If there is no actual implementation for the interface and we end up using this stub,
         * it means the execution has no preconditions and can be executed.
         */
        return true;
    }
}
