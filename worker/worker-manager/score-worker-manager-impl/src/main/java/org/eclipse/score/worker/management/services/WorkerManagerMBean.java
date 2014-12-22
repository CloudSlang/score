/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package org.eclipse.score.worker.management.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

/**
 * User:
 * Date: 1/14/13
 */
@ManagedResource(description = "Worker Manager Management API")
public class WorkerManagerMBean {

	@Autowired
	private WorkerManager workerManager;

	@Autowired
	private OutboundBuffer outBuffer;

	@ManagedAttribute(description = "Current In-Buffer Size")
	public int getInBufferSize(){
		return workerManager.getInBufferSize();
	}

	@ManagedAttribute(description = "Current Out-Buffer Size")
	public int getOutBufferSize(){
		return outBuffer.getSize();
	}

    @ManagedAttribute(description = "Out-Buffer Capacity")
    public int getOutBufferCapacity(){
        return outBuffer.getCapacity();
    }

	@ManagedAttribute(description = "Worker UUID")
	public String getWorkerUuid(){
		return workerManager.getWorkerUuid();
	}

	@ManagedAttribute(description = "Running Tasks Count")
	public int getRunningTasksCount(){
		return workerManager.getRunningTasksCount();
	}
}
