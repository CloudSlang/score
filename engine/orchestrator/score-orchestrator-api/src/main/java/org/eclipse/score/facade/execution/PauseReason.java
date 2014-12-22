/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package org.eclipse.score.facade.execution;

/**
 * Created with IntelliJ IDEA.
 * User: kravtsov
 * Date: 20/12/12
 * Time: 11:55
 */
public enum PauseReason {
    USER_PAUSED,
    INPUT_REQUIRED,
    INPUT_REQUIRED_MANUAL_OP,
	SELECT_TRANSITION,
    DISPLAY,
    GATED_TRANSITION,
    HAND_OFF,
    INTERRUPT,
    NO_WORKERS_IN_GROUP,
    BRANCH_PAUSED
}
