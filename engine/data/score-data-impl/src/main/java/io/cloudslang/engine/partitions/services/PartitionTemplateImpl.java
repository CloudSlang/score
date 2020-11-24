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
import org.apache.commons.lang.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Date: 4/27/12
 *
 */
@SuppressWarnings("unused")
public class PartitionTemplateImpl implements PartitionTemplate, BeanNameAware {
	private final Logger logger = LogManager.getLogger(getClass());

	@Autowired
	private PartitionUtils partitionUtils;

	@Autowired
	private PartitionService service;

	@Autowired
	private ApplicationContext applicationContext;

	private int groupSize = 4; // by default
	private long timeThreshold = 4 * 60 * 60 * 1000L; // by default: 4 hours
	private long sizeThreshold = 1000000L; // by default: a million

	private String groupName;

	private PartitionCallback[] callbacks = new PartitionCallback[0];

	private ThreadLocal<String> origThreadName = new ThreadLocal<>();

	@PostConstruct
	void registerPartitionGroup() {
        PartitionGroup partitionGroup = service.readPartitionGroup(groupName);
 		if (partitionGroup == null) {
			service.createPartitionGroup(groupName, groupSize, timeThreshold, sizeThreshold);
		} else {
            // check if need to update the partition group
            if (partitionGroup.getGroupSize() != groupSize ||
                    partitionGroup.getSizeThreshold() != sizeThreshold ||
                    partitionGroup.getTimeThreshold() != timeThreshold) {
                // update the partition group with the new values
                service.updatePartitionGroup(groupName, groupSize, timeThreshold, sizeThreshold);
            }
        }
	}

	@Override
	public String activeTable() {
		PartitionGroup group = service.readPartitionGroup(groupName);
		return group == null ? null :
				partitionUtils.tableName(groupName, group.getActivePartition());
	}

	@Override
	public String previousTable() {
		PartitionGroup group = service.readPartitionGroup(groupName);
		return group == null ? null :
				partitionUtils.tableName(groupName,
						partitionUtils.partitionBefore(group.getActivePartition(), group.getGroupSize()));
	}

	@Override
	public List<String> reversedTables() {
		final PartitionGroup group = service.readPartitionGroup(groupName);
		if (group == null) return null;
		List<String> result = new ArrayList<>();

		int partition = group.getActivePartition();
		for (int i = 0; i < group.getGroupSize(); i++) {
			result.add(partitionUtils.tableName(groupName, partition));
			partition = partitionUtils.partitionBefore(partition, group.getGroupSize());
		}
		return result;
	}


	// this method is being called by scheduler
	@Override
	public void onRolling() {
        // For case that during last rolling a transaction wrote to previous table,
        // copy left over records.
        runCallbackOnRollingPartitions();

        // change active table pointer
		boolean wasRolled = service.rollPartitions(groupName);

        // Copy record from previous table to new table.
		if ( wasRolled ){
            runCallbackOnRollingPartitions();
		}
	}

    private void runCallbackOnRollingPartitions() {
        if (!ArrayUtils.isEmpty(callbacks)) {
            if (logger.isDebugEnabled()) logger.debug("Run callbacks on roll partition group [" + groupName + "]");
            for (PartitionCallback callback : callbacks)
                try {
                    callback.doCallback(previousTable(), activeTable());
                } catch (RuntimeException ex) {
                    logger.error("Partition group [" + groupName + "]: callback [" + callback.getClass().getSimpleName() + "] failed on rolling partitions", ex);
                }
        }
    }

    @Override
	public void setBeanName(String name) {
		this.groupName = name;
	}

	@Required
	public void setGroupSize(int groupSize) {
		this.groupSize = groupSize;
	}

	@Required
	public void setTimeThreshold(long timeThreshold) {
		this.timeThreshold = timeThreshold;
	}

	@Required
	public void setSizeThreshold(long sizeThreshold) {
		this.sizeThreshold = sizeThreshold;
	}

	public void setCallbacks(PartitionCallback... callbacks) {
		this.callbacks = (PartitionCallback[])ArrayUtils.addAll(this.callbacks, callbacks);
	}

	public void setCallbackClass(Class<? extends PartitionCallback> callbackClass){
		logger.info("Registering callback class " + callbackClass.getSimpleName() + " for partition group [" + groupName +"]");
		Map<String,? extends PartitionCallback> callbacksMap = applicationContext.getBeansOfType(callbackClass);

		if (!callbacksMap.isEmpty()){
			if (logger.isDebugEnabled()) logger.debug("Partition group [" + groupName + "] callbacks found: " + callbacksMap.keySet());
			PartitionCallback[] callbacksByClass = callbacksMap.values().toArray(new PartitionCallback[callbacksMap.size()]);
			callbacks = (PartitionCallback[])ArrayUtils.addAll(callbacks, callbacksByClass);
		} else {
			logger.warn("Partition group [" + groupName + "]: no callbacks found by class " + callbackClass);
		}
	}

	@Override
	public String toString() {
		return "Partition-" + groupName;
	}
}
