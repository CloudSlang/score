package com.hp.score.worker.management;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.hp.score.engine.node.services.WorkerNodeService;

/**
 * User: stoneo
 * Date: 15/07/2014
 * Time: 15:39
 */
public class WorkerRegistration {

	private static final Logger logger = Logger.getLogger(WorkerRegistration.class);

	@Autowired
	private WorkerNodeService workerNodeService;
	@Autowired
	private String workerUuid;

	@PostConstruct
	public void registerWorkerPostConstruct() {
		try {
			registerWorker();
		} catch(Exception e) {
			logger.error("Failed to register worker due to: " + e.getMessage(), e);
			throw new RuntimeException("Failed to register worker", e);
		}
	}

	private void registerWorker() {
		if(logger.isDebugEnabled()) logger.debug("Registering embedded worker...");
		String password = UUID.randomUUID().toString();
		workerNodeService.create(workerUuid, password, getHostName(), FilenameUtils.separatorsToSystem("C:\\"));
		workerNodeService.activate(workerUuid);
	}

	private static String getHostName() {
		try {
			return InetAddress.getLocalHost().getCanonicalHostName();
		} catch(UnknownHostException ex) {
			logger.fatal("Unable to register embedded worker due to failure in host name resolving", ex);
			throw new RuntimeException("Failed to resolve host name", ex);
		}
	}

}
