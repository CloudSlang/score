/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package io.cloudslang.facade.execution;

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
