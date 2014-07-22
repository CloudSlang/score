package com.hp.oo.worker.management;

import com.hp.oo.engine.node.services.WorkerNodeService;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

/**
 * User: stoneo
 * Date: 15/07/2014
 * Time: 15:39
 */
public class WorkerRegistration {

    private Logger logger = Logger.getLogger(getClass());

    @Autowired
    private WorkerNodeService workerNodeService;

    @Autowired
    private String workerUuid;


    @PostConstruct
    public void registerWorkerPostConstruct() throws IOException {

        try {
            registerWorker();
        } catch (Exception e) {
            logger.error("Failed to register worker due to: " + e.getMessage(), e);
            throw new RuntimeException("Failed to register worker", e);
        }
    }

    private void registerWorker() throws IOException {
        if (logger.isDebugEnabled()) logger.debug("Registering embedded worker...");

        String id = workerUuid;
        String password = UUID.randomUUID().toString();
        workerNodeService.create(id, password, getHostName(), FilenameUtils.separatorsToSystem("C:\\"));
        workerNodeService.activate(id);
    }

    private String getHostName() {
        try {
            return InetAddress.getLocalHost().getCanonicalHostName();
        } catch (UnknownHostException ex) {
            logger.fatal("Unable to register embedded worker due to failure in host name resolving", ex);
            throw new RuntimeException("Failed to resolve host name", ex);
        }
    }

}
