/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package org.openscore.worker.management;

/**
 * @author kravtsov
 * @author Avi Moradi
 * @since 03/06/2012
 * Used by Score for pause/cancel runs & stay in the worker
 */
public interface WorkerConfigurationService {

    /**
     * checks if the given execution is pending cancel
     *
     * @param executionId the execution id to check
     * @return true if the execution is pending cancel
     */
	public boolean isExecutionCancelled(Long executionId);

    /**
     * checks if the given execution is pending pause
     *
     * @param executionId the execution id to check
     * @return true if the execution is pending pause
     */
	public boolean isExecutionPaused(Long executionId, String branchId);

    /**
     *
     * checks if the current worker is part of the fiven group
     *
     * @param group the group to check
     * @return true if the worker is part of the group
     */
	public boolean isMemberOf(String group);

    /**
     * Sets the current worker enabled state
     * relevant for tasks not to work until the worker is enabled
     *
     * @param enabled the edibility state to set
     */
	public void setEnabled(boolean enabled);

}
