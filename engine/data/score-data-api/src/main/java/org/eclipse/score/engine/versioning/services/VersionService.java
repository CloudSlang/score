/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package org.eclipse.score.engine.versioning.services;

/**
 * Created with IntelliJ IDEA.
 * User: wahnonm
 * Date: 11/3/13
 * Time: 9:23 AM
 */
public interface VersionService {

    /**
     * The recovery key
     *
     */
    public static final String MSG_RECOVERY_VERSION_COUNTER_NAME = "MSG_RECOVERY_VERSION";

    /**
     * Given the counter name (key) returns the current version of it.
     * @param counterName : the counter name (key)
     * @return current count
     */
    public long getCurrentVersion(String counterName);

    /**
     * Increments the conuter relevent to the given counter name by 1.
     * @param counterName : the counter name (key) to increment.
     */
    public void incrementVersion(String counterName);
}
