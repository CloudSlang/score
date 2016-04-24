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

import io.cloudslang.engine.partitions.entities.PartitionGroup;
import io.cloudslang.engine.partitions.repositories.PartitionGroupRepository;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Date: 4/23/12
 *
 * @author
 */
public final class PartitionServiceImpl implements PartitionService {
	private final Logger logger = Logger.getLogger(getClass());

	public static final int MIN_GROUP_SIZE = 2;

	@Autowired
	private PartitionGroupRepository repository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

    @Autowired
    private PartitionUtils partitionUtils;

	@Override
	@Transactional
	public void createPartitionGroup(String groupName, int groupSize, long timeThreshold, long sizeThreshold){
		Validate.notEmpty(groupName, "Group name is empty or null");
		Validate.isTrue(groupSize >= MIN_GROUP_SIZE, formatMessage2Small("Group size", groupSize, MIN_GROUP_SIZE));
//		Validate.isTrue(timeThreshold==-1 || timeThreshold >= MIN_TIME_THRESHOLD, formatMessage2Small("Time threshold", timeThreshold, MIN_TIME_THRESHOLD));
//		Validate.isTrue(sizeThreshold==-1 || sizeThreshold >= MIN_SIZE_THRESHOLD, formatMessage2Small("Size threshold", sizeThreshold, MIN_SIZE_THRESHOLD));

		repository.save(new PartitionGroup(groupName, groupSize, timeThreshold, sizeThreshold));
		if (logger.isInfoEnabled()) logger.info("Partition group [" + groupName + "] was created, with group size "+groupSize+", and timeThreshold "+timeThreshold+", and sizeThreshold "+sizeThreshold);
	}

    @Override
    @Transactional
    public void updatePartitionGroup(String groupName, int groupSize, long timeThreshold, long sizeThreshold){
        Validate.notEmpty(groupName, "Group name is empty or null");
        Validate.isTrue(groupSize >= MIN_GROUP_SIZE, formatMessage2Small("Group size", groupSize, MIN_GROUP_SIZE));

        PartitionGroup partitionGroup = repository.findByName(groupName);

        if (partitionGroup != null) {
            partitionGroup.setName(groupName);
            partitionGroup.setGroupSize(groupSize);
            partitionGroup.setTimeThreshold(timeThreshold);
            partitionGroup.setSizeThreshold(sizeThreshold);
            if (logger.isInfoEnabled()) logger.info("Partition group [" + groupName + "] was updated, with group size "+groupSize+", and timeThreshold "+timeThreshold+", and sizeThreshold "+sizeThreshold);
        }
    }

	private String formatMessage2Small(String param, long value, long limit){
		return param + " is too small: " + value + " (min " + limit + ")";
	}

	@Override
	@Transactional(readOnly = true)
	public PartitionGroup readPartitionGroup(String groupName){
		Validate.notEmpty(groupName, "Group name is empty or null");
		return repository.findByName(groupName);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public boolean rollPartitions(String groupName) {
		Validate.notEmpty(groupName, "Group name is empty or null");

		Validate.isTrue(repository.lock(groupName)==1, "Unknown partition group [" + groupName + "]");

		// the partition group cannot be null since it is locked in the database
		PartitionGroup partitionGroup = repository.findByName(groupName);
		if (!shouldBeRolled(partitionGroup)){
			return false;
		}

		if (logger.isDebugEnabled()) logger.debug("Rolling partition group [" + groupName + "]");
		long t = System.currentTimeMillis();

		// increment the active partition
		partitionGroup
				.setActivePartition(partitionUtils.partitionAfter(partitionGroup.getActivePartition(), partitionGroup.getGroupSize()))
				.setLastRollTime(System.currentTimeMillis());

		// truncate next partition
		jdbcTemplate.execute(SQL("truncate table " + table(partitionGroup)));

		if (logger.isDebugEnabled()){
			logger.debug("Group [" + groupName + "]: active partition is " + partitionGroup.getActivePartition() + " (rolled in " + (System.currentTimeMillis()-t) + " ms)");
		}

		return true;
	}

	private boolean shouldBeRolled(PartitionGroup partitionGroup){
		if (partitionGroup.getTimeThreshold() != -1){
			long lastRoll = System.currentTimeMillis()-partitionGroup.getLastRollTime();
			if (logger.isDebugEnabled()) logger.debug("Partition group [" + partitionGroup.getName() + "] was rolled before " + lastRoll + " ms");

			if (lastRoll > partitionGroup.getTimeThreshold()){
				if (logger.isInfoEnabled()) logger.info("Partition group [" + partitionGroup.getName() + "] has reached a time threshold and will be rolled");
				return true;
			} else if (logger.isDebugEnabled()){
				logger.debug("Time threshold wasn't reached -> timeThreshold-lastRoll = " + (partitionGroup.getTimeThreshold()-lastRoll) + " ms");
			}
		}

		if (partitionGroup.getSizeThreshold() != -1){
			Number number = jdbcTemplate.queryForObject(SQL("select count(*) from " + table(partitionGroup)), Long.class);
			long partitionSize = number !=null ? number.intValue() : 0;
			if (logger.isDebugEnabled()) logger.debug("Partition group [" + partitionGroup.getName() + "]: active partition=" + partitionGroup.getActivePartition() + ", size=" + partitionSize);

			if (partitionSize >= partitionGroup.getSizeThreshold()){
				if (logger.isInfoEnabled()) logger.info("Partition group [" + partitionGroup.getName() + "] has reached a records limit and will be rolled");
				return true;
			}
		}

		return false;
	}

	private String table(PartitionGroup partitionGroup) {
		return partitionUtils.tableName(partitionGroup.getName(), partitionGroup.getActivePartition());
	}

	private String SQL(String sql){
		if (logger.isDebugEnabled()) logger.debug("SQL: " + sql);
		return sql;
	}
}
