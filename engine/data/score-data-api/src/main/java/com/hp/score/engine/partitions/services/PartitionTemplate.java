package com.hp.score.engine.partitions.services;

import java.util.List;

/**
 * Date: 4/27/12
 *
 * @author Dima Rassin
 */
//TODO: Add Javadoc
public interface PartitionTemplate {
	String activeTable();
	String previousTable();
	List<String> reversedTables();
	void onRolling();
}
