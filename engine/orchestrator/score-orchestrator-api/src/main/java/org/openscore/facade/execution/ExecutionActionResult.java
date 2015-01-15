package org.openscore.facade.execution;

import java.util.HashMap;
import java.util.Map;

/**
 * User: noym
 * Date: 24/11/2014
 * Time: 13:30
 */
public enum ExecutionActionResult {
    SUCCESS,
    FAILED_ALREADY_PAUSED,
    FAILED_ALREADY_COMPLETED,
    FAILED_ALREADY_CANCELED_OR_PENDING_CANCELLATION,
    FAILED_ALREADY_RUNNING,
    FAILED_NOT_FOUND,
    FAILED_PENDING_PAUSE,
    FAILED_SYSTEM_FAILURE;

    private static final Map<ExecutionStatus, ExecutionActionResult> EXECUTION_STATUS_TO_EXECUTION_ACTION_RESULT = new HashMap<ExecutionStatus, ExecutionActionResult>() {{
        put(ExecutionStatus.COMPLETED, ExecutionActionResult.FAILED_ALREADY_COMPLETED);
        put(ExecutionStatus.CANCELED, ExecutionActionResult.FAILED_ALREADY_CANCELED_OR_PENDING_CANCELLATION);
        put(ExecutionStatus.PENDING_CANCEL, ExecutionActionResult.FAILED_ALREADY_CANCELED_OR_PENDING_CANCELLATION);
        put(ExecutionStatus.PAUSED, ExecutionActionResult.FAILED_ALREADY_PAUSED);
        put(ExecutionStatus.PENDING_PAUSE, ExecutionActionResult.FAILED_PENDING_PAUSE);
        put(ExecutionStatus.RUNNING, ExecutionActionResult.FAILED_ALREADY_RUNNING);
        put(ExecutionStatus.SYSTEM_FAILURE, ExecutionActionResult.FAILED_SYSTEM_FAILURE);
    }};

    public static ExecutionActionResult getExecutionActionResult(ExecutionStatus executionStatus) {
        return EXECUTION_STATUS_TO_EXECUTION_ACTION_RESULT.get(executionStatus);
    }
}
