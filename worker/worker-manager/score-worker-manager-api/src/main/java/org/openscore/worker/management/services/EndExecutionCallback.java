/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package org.openscore.worker.management.services;

/**
 * Created with IntelliJ IDEA.
 * User: kravtsov
 * Date: 21/11/12
 * Time: 14:14
 *
 * A callback that is called when an execution ends running in the worker
 *
 */
public interface EndExecutionCallback {

    /**
     * The callback to call when ending the execution
     *
     * @param executionId the executionId of the execution.
     */
    void endExecution(Long executionId);
}
