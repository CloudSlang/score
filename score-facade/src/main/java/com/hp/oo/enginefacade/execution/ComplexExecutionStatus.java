package com.hp.oo.enginefacade.execution;

/**
 * User: neumane
 * Date: 04/03/14
 */
public class ComplexExecutionStatus {
    ExecutionEnums.ExecutionStatus executionStatus;
    String resultStatus;
    PauseReason pauseReason;

    public ComplexExecutionStatus(ExecutionEnums.ExecutionStatus executionStatus, String resultStatus, PauseReason pauseReason) {
        this.executionStatus = executionStatus;
        this.resultStatus = resultStatus;
        this.pauseReason = pauseReason;
    }

    public ExecutionEnums.ExecutionStatus getExecutionStatus() {
        return executionStatus;
    }

    public String getResultStatus() {
        return resultStatus;
    }

    public PauseReason getPauseReason() {
        return pauseReason;
    }
}

