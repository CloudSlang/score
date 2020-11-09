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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.cloudslang.engine.node.services.WorkerNodeService;
import io.cloudslang.orchestrator.entities.MergedConfigurationDataContainer;
import io.cloudslang.orchestrator.model.MergedConfigurationHolder;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor.DiscardPolicy;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.Long.getLong;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.concurrent.TimeUnit.MILLISECONDS;


public class MergedConfigurationServiceImpl implements MergedConfigurationService {

    private static final Logger log = Logger.getLogger(MergedConfigurationServiceImpl.class);
    private static final long MERGED_CONFIGURATION_PERIODIC_REFRESH_MILLIS = getLong("worker.mergedConfiguration.refreshDelayMillis", 2300L);
    private static final long MERGED_CONFIGURATION_INITIAL_DELAY_MILLIS = getLong("worker.mergedConfiguration.initialDelayMillis", 1000L);

    @Autowired
    private CancelExecutionService cancelExecutionService;

    @Autowired
    private PauseResumeService pauseResumeService;

    @Autowired
    private WorkerNodeService workerNodeService;

    // Intentionally not a Spring bean as this an internal executor dedicated to the MergedConfigurationService only
    private final ScheduledThreadPoolExecutor scheduledExecutor;

    // Used to store the latest state of the MergedConfigurationHolder object,
    // periodically reloaded every worker.mergedConfiguration.refreshIntervalInMillis millis
    private final AtomicReference<MergedConfigurationHolder> mergedConfigHolderReference;

    public MergedConfigurationServiceImpl() {
        this.scheduledExecutor = getScheduledExecutor();
        // Initially value points to an empty state
        this.mergedConfigHolderReference = new AtomicReference<>(new MergedConfigurationHolder());
    }

    @PostConstruct
    protected void schedulePeriodicRefreshOfMergedConfiguration() {
        Runnable refreshMergedConfigRunnable = new RefreshMergedConfigurationRunnable(cancelExecutionService,
                pauseResumeService, workerNodeService, mergedConfigHolderReference);
        final long initialDelay = MERGED_CONFIGURATION_INITIAL_DELAY_MILLIS;
        final long periodicDelay = MERGED_CONFIGURATION_PERIODIC_REFRESH_MILLIS;
        scheduledExecutor.scheduleWithFixedDelay(refreshMergedConfigRunnable, initialDelay, periodicDelay, MILLISECONDS);
    }

    /**
     * Read data from memory, this data is periodically refreshed from database by scheduledExecutor executor.
     */
    @Override
    public MergedConfigurationDataContainer fetchMergedConfiguration(String workerUuid) {
        final MergedConfigurationHolder holder = mergedConfigHolderReference.get();
        return new MergedConfigurationDataContainer(holder.getCancelledExecutions(),
                holder.getPausedExecutionBranchIdPairs(),
                holder.getWorkerGroupsForWorker(workerUuid));
    }

    @PreDestroy
    public void destroy() {
        try {
            scheduledExecutor.shutdown();
            scheduledExecutor.shutdownNow();
        } catch (Exception failedShutdownEx) {
            log.error("Could not shutdown merged configuration container executor: ", failedShutdownEx);
        }
    }

    private ScheduledThreadPoolExecutor getScheduledExecutor() {
        final ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNameFormat("merged-config-refresher-%d")
                .setDaemon(true)
                .build();

        // Intentionally 1 thread
        ScheduledThreadPoolExecutor scheduledExecutor = new ScheduledThreadPoolExecutor(1, threadFactory);
        scheduledExecutor.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
        scheduledExecutor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        scheduledExecutor.setRemoveOnCancelPolicy(true);
        scheduledExecutor.setRejectedExecutionHandler(new DiscardPolicy());
        return scheduledExecutor;
    }

    private static class RefreshMergedConfigurationRunnable implements Runnable {

        private final CancelExecutionService cancelExecutionService;
        private final PauseResumeService pauseResumeService;
        private final WorkerNodeService workerNodeService;
        private final AtomicReference<MergedConfigurationHolder> ref;

        public RefreshMergedConfigurationRunnable(CancelExecutionService cancelExecutionService,
                PauseResumeService pauseResumeService,
                WorkerNodeService workerNodeService,
                AtomicReference<MergedConfigurationHolder> ref) {

            this.cancelExecutionService = cancelExecutionService;
            this.pauseResumeService = pauseResumeService;
            this.workerNodeService = workerNodeService;
            this.ref = ref;
        }

        @Override
        public void run() {
            MergedConfigurationHolder mergedConfigHolderValue = null;
            try {
                Set<Long> cancelledExecutions;
                try {
                    cancelledExecutions = new HashSet<>(cancelExecutionService.readCanceledExecutionsIds());
                } catch (Exception readCancelledExc) {
                    cancelledExecutions = emptySet();
                    log.error("Failed to read cancelled executions information: ", readCancelledExc);
                }

                Set<String> pausedExecutionBranchIds;
                try {
                    pausedExecutionBranchIds = pauseResumeService.readAllPausedExecutionBranchIdsNoCache();
                } catch (Exception readPausedExecBranchPairsExc) {
                    pausedExecutionBranchIds = emptySet();
                    log.error("Failed to read paused executions information: ", readPausedExecBranchPairsExc);
                }

                Map<String, Set<String>> workerGroupsMap;
                try {
                    workerGroupsMap = workerNodeService.readWorkerGroupsMap();
                } catch (Exception readWorkerGroupsExc) {
                    workerGroupsMap = emptyMap();
                    log.error("Failed to read current worker group information: ", readWorkerGroupsExc);
                }

                // Construct the object that is queried
                mergedConfigHolderValue = new MergedConfigurationHolder(cancelledExecutions, pausedExecutionBranchIds, workerGroupsMap);
            } catch (Exception exception) {
                log.error("Exception during refresh worker merged configuration information: ", exception);
            } finally {
                ref.set((mergedConfigHolderValue != null) ? mergedConfigHolderValue : new MergedConfigurationHolder());
            }
        }
    }
}

