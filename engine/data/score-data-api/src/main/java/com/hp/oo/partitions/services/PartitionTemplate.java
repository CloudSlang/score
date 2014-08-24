package com.hp.oo.partitions.services;

import java.util.List;

/**
 * Date: 4/27/12
 *
 * @author Dima Rassin
 */
public interface PartitionTemplate {
	String activeTable();
	String previousTable();
	List<String> reversedTables();
	void onRolling();
}
