/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package org.openscore.engine.queue.services.recovery;

/**
 * Created by IntelliJ IDEA.
 * User:
 * Date: 20/11/12
 */

/**
 * Recovery service that is responsible to do recovery to execution messages of non responsive workers.
 */
public interface ExecutionRecoveryService {
	public void doRecovery();
}
