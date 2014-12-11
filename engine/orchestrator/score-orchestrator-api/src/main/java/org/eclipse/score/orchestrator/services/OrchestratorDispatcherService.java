/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package org.eclipse.score.orchestrator.services;

import java.io.Serializable;
import java.util.List;

/**
 * Date: 12/1/13
 *
 * Responsible for handling dispatching of messages from the queue consumer
 *
 */
public interface OrchestratorDispatcherService {

    /**
     *
     * Dispatch messges to the queue from the consumer
     *
     * @param messages the messages to dispatch
     * @param bulkNumber an identifier of the dispatch bulk, needed for recovery
     * @param wrv the worker recovery version, needed for recovery
     * @param workerUuid the id of the dispatching worker
     */
	void dispatch(List<? extends Serializable> messages, String bulkNumber, String wrv, String workerUuid);
}
