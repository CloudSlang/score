package com.hp.score.facade.execution;

/**
 * Created by peerme on 17/08/2014.
 */
public enum ExecutionStatus {
    RUNNING,
    COMPLETED,
    SYSTEM_FAILURE,
    PAUSED,
    PENDING_PAUSE,
    CANCELED,
    PENDING_CANCEL
}
