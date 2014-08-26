package com.hp.score.engine.partitions.services;

/**
 * User: Dima rassin
 * Date: 4/17/13
 */
public interface PartitionCallback {
	void doCallback(String previousTable, String activeTable);
}
