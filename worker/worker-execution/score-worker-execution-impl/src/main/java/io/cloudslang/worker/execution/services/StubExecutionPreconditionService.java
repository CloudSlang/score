package io.cloudslang.worker.execution.services;

import io.cloudslang.score.api.execution.precondition.ExecutionPreconditionService;

public class StubExecutionPreconditionService implements ExecutionPreconditionService {

    @Override
    public boolean canExecute() {
        /*
         * If there is no actual implementation for the interface and we end up using this stub,
         * it means the execution has no preconditions and can be executed.
         */
        return true;
    }
}
