/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package io.cloudslang.worker.management.services;

/**
 * Date: 6/13/13
 *
 * Manages the worker internal recovery.
 * Holds the current WRV (worker recovery version) as known to worker.
 */
public interface WorkerRecoveryManager {
	void doRecovery();
	boolean isInRecovery();
    String getWRV();
    void setWRV(String newWrv);
}
