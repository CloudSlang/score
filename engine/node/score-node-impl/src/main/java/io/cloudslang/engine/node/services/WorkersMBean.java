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

package io.cloudslang.engine.node.services;


import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import java.io.IOException;

/**
 * User:
 * Date: 2/9/13
 */
@ManagedResource(description = "Worker Nodes Managing API")
public class WorkersMBean {
	@Autowired

	private WorkerNodeService workerNodeService;

    private ObjectMapper mapper;

    private ObjectMapper objectMapper(){
        if (mapper == null) {
            mapper = new ObjectMapper();

            mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        }
        return mapper;
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
		ObjectWriter objectWriter = objectMapper().writerWithDefaultPrettyPrinter();
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