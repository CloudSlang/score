/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package org.openscore.engine.queue.services.cleaner;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User:
 * Date: 14/10/13
 *
 * A service that is responsible for cleaning the queue tables
 */
public interface QueueCleanerService {

    /**
     *
     * get a set of ids of finished executions
     *
     * @return Set of ids of finished executions
     */
    Set<Long> getFinishedExecStateIds();

    /**
     *
     * clean queues data for the given ids
     *
     * @param ids the ids to clean data for
     */
    void cleanFinishedSteps(Set<Long> ids);
}
