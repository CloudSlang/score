package com.hp.score.engine.partitions.services;

import com.hp.score.engine.partitions.entities.PartitionGroup;

/**
 * Date: 4/22/12
 *
 * @author Dima Rassin
 */
public interface PartitionService {
    void createPartitionGroup(String groupName, int groupSize, long timeThreshold, long sizeThreshold);
    void updatePartitionGroup(String groupName, int groupSize, long timeThreshold, long sizeThreshold);
	PartitionGroup readPartitionGroup(String groupName);
	boolean rollPartitions(String groupName);
}
