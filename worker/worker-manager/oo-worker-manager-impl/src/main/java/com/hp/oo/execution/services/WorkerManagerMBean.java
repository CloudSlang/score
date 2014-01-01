package com.hp.oo.execution.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

/**
 * User: Dima Rassin
 * Date: 1/14/13
 */
@ManagedResource(description = "Worker Manager Management API")
@Component
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

	@ManagedAttribute(description = "Worker UUID")
	public String getWorkerUuid(){
		return workerManager.getWorkerUuid();
	}

	@ManagedAttribute(description = "Running Tasks Count")
	public int getRunningTasksCount(){
		return workerManager.getRunningTasksCount();
	}
}
