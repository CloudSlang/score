package com.hp.oo.partitions.services;

/**
 * Date: 4/27/12
 *
 * @author Dima Rassin
 */
public class PartitionUtils {
	public String tableName(String groupName, int partition){
		return groupName + "_" + partition;
	}

	public int partitionBefore(int partition, int groupSize) {
		return partition == 1? groupSize: partition-1;
	}

	public int partitionAfter(int partition, int groupSize) {
		return partition == groupSize? 1: partition+1;
	}
}
