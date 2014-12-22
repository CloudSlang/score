/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package org.eclipse.score.worker.management;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.log4j.Logger;

import org.openscore.engine.node.entities.WorkerNode;
import org.openscore.engine.node.services.WorkerNodeService;

/**
 * @author stoneo
 * @author Avi Moradi
 * @since 15/07/2014
 */
public class WorkerRegistration {

	private static final Logger log = Logger.getLogger(WorkerRegistration.class);

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
		workerNodeService.create(uuid, password, InetAddress.getLocalHost().getCanonicalHostName(), installPath);
		workerNodeService.activate(uuid);
		log.info("Worker [" + uuid + "] registered and activated");
	}

}
