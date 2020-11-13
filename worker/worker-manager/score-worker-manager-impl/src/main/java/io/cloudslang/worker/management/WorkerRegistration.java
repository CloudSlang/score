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

package io.cloudslang.worker.management;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.cloudslang.engine.node.entities.WorkerNode;
import io.cloudslang.engine.node.services.WorkerNodeService;

/**
 * @author stoneo
 * @author Avi Moradi
 * @since 15/07/2014
 */
public class WorkerRegistration {

	private static final Logger log = LogManager.getLogger(WorkerRegistration.class);

	@Resource
	protected String workerUuid;
	@Resource
	protected WorkerNodeService workerNodeService;

	@PostConstruct
	public void registerWorkerPostConstruct() throws Exception {
		try {
			registerWorker();
		} catch(Exception ex) {
			log.error("Failed to register worker due to: " + ex.getMessage(), ex);
			throw ex;
		}
	}

	protected void registerWorker() throws Exception {
		try {
			WorkerNode workerNode = workerNodeService.readByUUID(workerUuid);
			if(workerNode != null) {
				log.info("Worker already registered: " + workerNode);
				return;
			}
		} catch(Exception ex) { /* Worker not found, register it */ }
		log.info("Registering worker " + workerUuid);
		String password = UUID.randomUUID().toString();
		createWorker(workerUuid, password, System.getProperty("user.dir"));
	}

	protected void createWorker(String uuid, String password, String installPath) throws UnknownHostException {
		log.info("Creating worker...");
		workerNodeService.create(uuid, password,getLocalHostName(), installPath);
		workerNodeService.activate(uuid);
		log.info("Worker [" + uuid + "] registered and activated");
	}

    protected String getLocalHostName(){
        String hostName = StringUtils.EMPTY;
        try {
            hostName = InetAddress.getLocalHost().getCanonicalHostName();
        } catch (UnknownHostException e) {
            // on Mac OS X with Java 7 calling 'getLocalHost()' can throw UnknownHostException
            // see http://bugs.java.com/bugdatabase/view_bug.do?bug_id=7180557
        }
        return hostName;
    }

}
