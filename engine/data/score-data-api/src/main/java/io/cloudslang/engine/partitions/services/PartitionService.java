/*
 * Copyright © 2014-2017 EntIT Software LLC, a Micro Focus company (L.P.)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.cloudslang.engine.partitions.services;

import io.cloudslang.engine.partitions.entities.PartitionGroup;

/**
 * Date: 4/22/12
 *
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
     * @return a {@link io.cloudslang.engine.partitions.entities.PartitionGroup} of the given name
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
