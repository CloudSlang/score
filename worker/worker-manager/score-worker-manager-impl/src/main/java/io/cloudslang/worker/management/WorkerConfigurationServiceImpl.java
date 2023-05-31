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

import io.cloudslang.orchestrator.entities.MergedConfigurationDataContainer;
import io.cloudslang.orchestrator.services.MergedConfigurationService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

/**
 * @author kravtsov
 * @author Avi Moradi
 * @version $Id$
 * @since 07/06/2012
 */
public class WorkerConfigurationServiceImpl implements WorkerConfigurationService {

    private static final Logger log = LogManager.getLogger(WorkerConfigurationServiceImpl.class);

    private volatile Set<Long> cancelledExecutions;
    private volatile Set<String> pausedExecutions;
    private volatile Set<String> workerGroups;
    private volatile boolean enabled;

    @Autowired
    private MergedConfigurationService mergedConfigurationService;

    @Override
    public boolean isExecutionCancelled(Long executionId) {
        return (cancelledExecutions != null) && cancelledExecutions.contains(executionId);
    }

    @Override
    public boolean isExecutionPaused(Long executionId, String branchId) {
        return (pausedExecutions != null) && pausedExecutions.contains(executionId + ":" + String.valueOf(branchId));
    }

    @Override
    public boolean isMemberOf(String group) {
        return (workerGroups != null) && workerGroups.contains(group);
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void refresh() {
        if (!enabled) {
            return;
        }
        MergedConfigurationDataContainer mergedConfigurationDataContainer = mergedConfigurationService.fetchMergedConfiguration(getWorkerUuid());
        setCanceledExecutions(mergedConfigurationDataContainer);
        setPausedExecutions(mergedConfigurationDataContainer);
        setWorkerGroups(mergedConfigurationDataContainer);
    }

    protected void setCanceledExecutions(MergedConfigurationDataContainer mergedConfigurationDataContainer) {
        try {
            cancelledExecutions = mergedConfigurationDataContainer.getCancelledExecutions();
        } catch (Exception ex) {
            log.error("Failed to fetch cancelled information: ", ex);
        }
    }

    protected void setPausedExecutions(MergedConfigurationDataContainer mergedConfigurationDataContainer) {
        try {
            pausedExecutions = mergedConfigurationDataContainer.getPausedExecutions();
        } catch (Exception ex) {
            log.error("Failed to fetch paused information: ", ex);
        }
    }

    protected void setWorkerGroups(MergedConfigurationDataContainer mergedConfigurationDataContainer) {
        try {
            workerGroups = mergedConfigurationDataContainer.getWorkerGroups();
        } catch (Exception ex) {
            log.error("Failed to fetch worker group information: ", ex);
        }
    }

    protected static String getWorkerUuid() {
        return System.getProperty("worker.uuid");
    }

}
