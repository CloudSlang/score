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

import org.openscore.engine.queue.entities.ExecStatus;
import org.openscore.engine.queue.entities.ExecutionMessage;

import java.util.List;

/**
 * User: varelasa
 * Date: 22/07/14
 * Time: 13:20
 */

/**
 * Recovers queue messages.
 */
public interface MessageRecoveryService {

    boolean recoverMessagesBulk(String workerName, int defaultPoolSize);

    void logMessageRecovery(List<ExecutionMessage> messages);

    void enqueueMessages(List<ExecutionMessage> messages, ExecStatus messageStatus);
}
