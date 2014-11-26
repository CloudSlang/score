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
package org.eclipse.score.worker.execution.services;

import org.eclipse.score.facade.entities.Execution;

import java.util.List;

/**
 * Date: 8/1/11
 *
 * @author
 */
//TODO: Add Javadoc
public interface ExecutionService {
	Execution execute(Execution execution) throws InterruptedException;
    List<Execution> executeSplit(Execution execution) throws InterruptedException; //returns null in case this execution is paused or cancelled and the split was not done
    boolean isSplitStep(Execution execution);
}
