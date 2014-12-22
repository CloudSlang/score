/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package org.openscore.engine.partitions.services;

/**
 * User:
 * Date: 4/17/13
 *
 * A callback that is called when rolling a partition
 *
 */
public interface PartitionCallback {

    /**
     * the callback to call when rolling the partition
     *
     * @param previousTable the table the was used before the rolling
     * @param activeTable the current table, after the rolling
     */
	void doCallback(String previousTable, String activeTable);
}
