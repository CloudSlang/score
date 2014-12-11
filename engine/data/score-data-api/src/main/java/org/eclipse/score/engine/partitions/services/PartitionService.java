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
package org.eclipse.score.engine.partitions.services;

import org.eclipse.score.engine.partitions.entities.PartitionGroup;

/**
 * Date: 4/22/12
 *
 * @author
 *
 * This service is responsible for handling tables partiotions.
 * Partition tables are rolled to the next partition when passing a certian treshold
 *
 */
public interface PartitionService {

    /**
     * creates a partition group and store it to the DB
     *
     * @param groupName the desired name of the group
     * @param groupSize the size of partitions
     * @param timeThreshold a time threshold to roll to the next partition
     * @param sizeThreshold a size threshold to roll to the next partition
     */
    void createPartitionGroup(String groupName, int groupSize, long timeThreshold, long sizeThreshold);

    /**
     * updates a partition group and store it to the DB
     *
     * @param groupName the desired name of the group
     * @param groupSize the size of partitions
     * @param timeThreshold a time threshold to roll to the next partition
     * @param sizeThreshold a size threshold to roll to the next partition
     */
    void updatePartitionGroup(String groupName, int groupSize, long timeThreshold, long sizeThreshold);

    /**
     *
     * Returns a group of the given name.
     *
     * @param groupName the group name to read
     * @return a {@link org.eclipse.score.engine.partitions.entities.PartitionGroup} of the given name
     */
	PartitionGroup readPartitionGroup(String groupName);

    /**
     *
     * Rolls a partition 
     *
     * @param groupName the group name to roll
     * @return true if roll was successful
     */
	boolean rollPartitions(String groupName);
}
