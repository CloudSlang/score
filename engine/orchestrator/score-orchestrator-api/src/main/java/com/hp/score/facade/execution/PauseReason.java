package com.hp.score.facade.execution;

/**
 * Created with IntelliJ IDEA.
 * User: kravtsov
 * Date: 20/12/12
 * Time: 11:55
 */
public enum PauseReason {
    USER_PAUSED,
    INPUT_REQUIRED,
    SELECT_TRANSITION,
    DISPLAY,
    GATED_TRANSITION,
    HAND_OFF,
    INTERRUPT,
    NO_WORKERS_IN_GROUP,
    BRANCH_PAUSED
}
