/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package io.cloudslang.orchestrator.services;

import java.io.Serializable;
import java.util.List;

/**
 * Date: 12/1/13
 *
 * Responsible for handling dispatching of messages from the queue consumer
 *
 */
public interface QueueDispatcherHelperService {

    /**
     *
     * Dispatch message to the queue from the consumer
     *
     * @param message the messages to dispatch
     */
	void dispatch(Serializable message);
	void dispatchBulk(List<? extends Serializable> messages, String bulkNumber, String workerUuid);

    boolean isDbConnectionOk();
}
