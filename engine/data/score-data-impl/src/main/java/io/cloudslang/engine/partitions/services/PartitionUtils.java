/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package io.cloudslang.engine.partitions.services;

/**
 * Date: 4/27/12
 *
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
