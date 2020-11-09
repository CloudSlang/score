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

package io.cloudslang.orchestrator.services;

import io.cloudslang.engine.node.services.WorkerNodeService;
import io.cloudslang.orchestrator.entities.MergedConfigurationDataContainer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;


public class MergedConfigurationServiceImpl implements MergedConfigurationService {

    private static final Logger log = LogManager.getLogger(MergedConfigurationServiceImpl.class);

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
            mergedConfigurationDataContainer.setCancelledExecutions(new HashSet<>(cancelExecutionService.readCanceledExecutionsIds()));
        } catch (Exception ex) {
            log.error("Failed to fetch cancelled information: ", ex);
        }

        try {
            mergedConfigurationDataContainer.setPausedExecutions(pauseResumeService.readAllPausedExecutionBranchIds());
        } catch (Exception ex) {
            log.error("Failed to read paused flows information: ", ex);
        }

        try {
            mergedConfigurationDataContainer.setWorkerGroups(new HashSet<>(workerNodeService.readWorkerGroups(workerUuid)));
        } catch (Exception ex) {
            log.error("Failed to fetch worker group information: ", ex);
        }

        return mergedConfigurationDataContainer;
    }
}

