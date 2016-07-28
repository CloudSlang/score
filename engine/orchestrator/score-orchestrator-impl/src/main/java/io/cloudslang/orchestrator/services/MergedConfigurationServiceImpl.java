/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/


package io.cloudslang.orchestrator.services;

import io.cloudslang.engine.node.services.WorkerNodeService;
import io.cloudslang.orchestrator.entities.MergedConfigurationDataContainer;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;


public class MergedConfigurationServiceImpl implements MergedConfigurationService {

    private final Logger log = Logger.getLogger(getClass());

    @Autowired
    private CancelExecutionService cancelExecutionService;
    @Autowired
    private PauseResumeService pauseResumeService;
    @Autowired
    private WorkerNodeService workerNodeService;

    @Override
    public MergedConfigurationDataContainer fetchMergedConfiguration(String workerUuid) {
        MergedConfigurationDataContainer mergedConfigurationDataContainer = new MergedConfigurationDataContainer();
        try {
            mergedConfigurationDataContainer.setCancelledExecutions(cancelExecutionService.readCanceledExecutionsIds());
        } catch(Exception ex) {
            log.error("Failed to fetch cancelled information: ", ex);
        }

        try {
            mergedConfigurationDataContainer.setPausedExecutions(pauseResumeService.readAllPausedExecutionBranchIds());
        } catch(Exception ex) {
            log.error("Failed to read paused flows information: ", ex);
        }

        try {
            mergedConfigurationDataContainer.setWorkerGroups(workerNodeService.readWorkerGroups(workerUuid));
        } catch(Exception ex) {
            log.error("Failed to fetch worker group information: ", ex);
        }

        return mergedConfigurationDataContainer;
    }
}

