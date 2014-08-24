package com.hp.oo.orchestrator.services;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: hajyhia
 * Date: 3/29/13
 * Time: 10:48 AM
 */
public interface CancelExecutionService {

    /**
     * Trigger execution cancellation - sets the given execution with status PENDING_CANCEL
     * @param executionId - the execution to cancel
     *
     */
    boolean requestCancelExecution(Long executionId);

    /**
     * Returns list of executions that are were cancelled (the status is either CANCELED or PENDING_CANCEL)
     * We need it for the cache of cancelled executions in the worker.
     */
    List<Long> readCanceledExecutionsIds();

    /**
     * Returns true if the given execution was cancelled.
     * @param executionId - the execution to check.
     * @return true if the given execution's status is CANCELED or PENDING_CANCEL
     */
    boolean isCanceledExecution(Long executionId);
}
