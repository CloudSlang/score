/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package org.openscore.engine.partitions.entities;

import org.openscore.engine.data.AbstractIdentifiable;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Date: 4/23/12
 *
 * @author
 */
@Entity
@Table(name = "OO_PARTITION_GROUPS")
public class PartitionGroup extends AbstractIdentifiable {
	@Column(name = "NAME", length = 27, nullable = false, unique = true)
	private String name;

	@Column(name = "GROUP_SIZE", nullable = false)
	private int groupSize;

	@Column(name = "TIME_THRESHOLD", nullable = false)
	private long timeThreshold;

	@Column(name = "SIZE_THRESHOLD", nullable = false)
	private long sizeThreshold;

	@Column(name = "ACTIVE_PARTITION", nullable = false)
	private int activePartition;

	@Column(name = "LAST_ROLL_TIME", nullable = false)
	private long lastRollTime = System.currentTimeMillis();

	@SuppressWarnings("UnusedDeclaration")
	private PartitionGroup() {} // used by JPA

	public PartitionGroup(String name, int size, long timeThreshold, long sizeThreshold) {
		this.name = name;
		this.groupSize = size;
		this.timeThreshold = timeThreshold;
		this.sizeThreshold = sizeThreshold;
		activePartition = 1;
		lastRollTime = System.currentTimeMillis();
	}

	public String getName() {
		return name;
	}

	public int getGroupSize() {
		return groupSize;
	}

	public long getTimeThreshold() {
		return timeThreshold;
	}

	public long getSizeThreshold() {
		return sizeThreshold;
	}

	public int getActivePartition() {
		return activePartition;
	}

	public PartitionGroup setActivePartition(int activePartition) {
		this.activePartition = activePartition;
		return this;
	}

	public long getLastRollTime() {
		return lastRollTime;
	}

	public PartitionGroup setLastRollTime(long lastRollTime) {
		this.lastRollTime = lastRollTime;
		return this;
	}

    public void setName(String name) {
        this.name = name;
    }

    public void setGroupSize(int groupSize) {
        this.groupSize = groupSize;
    }

    public void setTimeThreshold(long timeThreshold) {
        this.timeThreshold = timeThreshold;
    }

    public void setSizeThreshold(long sizeThreshold) {
        this.sizeThreshold = sizeThreshold;
    }

    @Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof PartitionGroup)) return false;

		PartitionGroup that = (PartitionGroup) o;

		return new EqualsBuilder().append(this.name, that.name).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(name).toHashCode();
	}
}
