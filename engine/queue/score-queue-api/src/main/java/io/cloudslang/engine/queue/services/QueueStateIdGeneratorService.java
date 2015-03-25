/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package io.cloudslang.engine.queue.services;

/**
 * Created by IntelliJ IDEA.
 * User:
 * Date: 07/08/13
 *
 * Service that generates DB unique ids for the queue
 *
 */
public interface QueueStateIdGeneratorService {

    /**
     *
     * generates a unique DB id for the queue consumers
     *
     * @return Long of a unique DB id
     */
    public Long generateStateId();
}
