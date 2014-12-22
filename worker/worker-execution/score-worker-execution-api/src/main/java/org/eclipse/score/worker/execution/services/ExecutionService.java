/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package org.eclipse.score.worker.execution.services;

import org.openscore.facade.entities.Execution;

import java.util.List;

/**
 * Date: 8/1/11
 *
 * @author
 *
 * Responsible for handling the execution
 *
 */
public interface ExecutionService {

    /**
     *
     * Execute the given execution
     *
     * @param execution the {@link org.openscore.facade.entities.Execution} to execute
     * @return the {@link org.openscore.facade.entities.Execution} after executing
     * @throws InterruptedException
     */
	Execution execute(Execution execution) throws InterruptedException;

    /**
     *
     * Handles execution of split step
     *
     * @param execution the split {@link org.openscore.facade.entities.Execution} to execute
     * @return the List of {@link org.openscore.facade.entities.Execution} that the split returns
     * returns null in case this execution is paused or cancelled and the split was not done
     * @throws InterruptedException
     */
    List<Execution> executeSplit(Execution execution) throws InterruptedException;
    boolean isSplitStep(Execution execution);
}
