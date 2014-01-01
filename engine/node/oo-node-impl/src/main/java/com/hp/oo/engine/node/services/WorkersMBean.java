package com.hp.oo.engine.node.services;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * User: Dima rassin
 * Date: 2/9/13
 */
@ManagedResource(description = "Worker Nodes Managing API")
@Component
public class WorkersMBean {
	@Autowired
	private WorkerNodeService workerNodeService;

	@Autowired
	ObjectMapper objectMapper;

	@ManagedAttribute(description = "Number of active workers")
	public int getActiveWorkers(){
		return workerNodeService.readWorkersByActivation(true).size();
	}

	@ManagedAttribute(description = "Number of all registered workers")
	public int getTotalWorkers(){
		return workerNodeService.readAllWorkers().size();
	}

	@ManagedOperation(description = "Returns a list of all registered workers")
	public String showWorkers() throws IOException {
		ObjectWriter objectWriter = objectMapper.writerWithDefaultPrettyPrinter();
		return objectWriter.writeValueAsString(workerNodeService.readAllWorkers());
	}

	@ManagedOperation(description = "Activates specified worker")
	@ManagedOperationParameters(
			@ManagedOperationParameter(name = "worker UUID", description = "Worker UUID to be activated")
	)
	public void activateWorker(String workerUuid){
		workerNodeService.activate(workerUuid);
	}

	@ManagedOperation(description = "Deactivates specified worker")
	@ManagedOperationParameters(
			@ManagedOperationParameter(name = "worker UUID", description = "Worker UUID to be deactivated")
	)
	public void deactivateWorker(String workerUuid){
		workerNodeService.deactivate(workerUuid);
	}
}