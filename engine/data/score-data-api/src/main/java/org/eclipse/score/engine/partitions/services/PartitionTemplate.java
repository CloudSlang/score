/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package org.eclipse.score.engine.partitions.services;

import java.util.List;

/**
 * Date: 4/27/12
 *
 * @author
 *
 * Trmplate class for handling partiotiond tables
 *
 */
public interface PartitionTemplate {

    /**
     *
     * return the currently active table
     *
     * @return a String of the table name
     */
	String activeTable();

    /**
     *
     * return the previous active table
     *
     * @return s String of the previous active table name
     */
	String previousTable();

    /**
     *
     * returns a List of the reserved tables for the group
     *
     * @return a List of the reserved tables for the group
     */
	List<String> reversedTables();

    /**
     * 
     * rolls to the next partition
     *
     */
	void onRolling();
}
