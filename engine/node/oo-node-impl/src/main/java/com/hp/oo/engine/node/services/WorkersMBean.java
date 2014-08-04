package com.hp.oo.engine.node.services;


import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import javax.annotation.PostConstruct;
import java.io.IOException;

/**
 * User: Dima rassin
 * Date: 2/9/13
 */
@ManagedResource(description = "Worker Nodes Managing API")
public class WorkersMBean {
	@Autowired

	private WorkerNodeService workerNodeService;

    private ObjectMapper mapper;

    @PostConstruct
    private void initObjectMapper(){
        mapper = new ObjectMapper();

        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

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
		ObjectWriter objectWriter = mapper.writerWithDefaultPrettyPrinter();
		return objectWriter.writeValueAsString(workerNodeService.readAllWorkers());
	}

    /*I remove this due to security issue this present*/
/*	@ManagedOperation(description = "Activates specified worker")
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
	}*/
}