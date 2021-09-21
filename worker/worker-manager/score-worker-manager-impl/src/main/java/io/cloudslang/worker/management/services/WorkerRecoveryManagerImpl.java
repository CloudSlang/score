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

package io.cloudslang.worker.management.services;

import io.cloudslang.engine.node.services.WorkerNodeService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Date: 6/11/13
 *
 */
public class WorkerRecoveryManagerImpl implements WorkerRecoveryManager {

    protected static final Logger logger = LogManager.getLogger(WorkerRecoveryManagerImpl.class);
    private static final int EXIT_STATUS = 75;

    @Autowired
	private List<WorkerRecoveryListener> listeners;
	@Autowired
	private WorkerNodeService workerNodeService;

	@Autowired
	private RetryTemplate retryTemplate;

    @Autowired
    private SynchronizationManager syncManager;

    @Autowired
   	protected WorkerVersionService workerVersionService;

	private volatile boolean inRecovery; //must be volatile since it is read/written in several threads

    private volatile String wrv; //must be volatile since it is read/written in several threads

	public void doRecovery(){
        try {
            boolean toRestart = Boolean.getBoolean("worker.restartOnRecovery");
            //If we are configured to restart on recovery - do shutdown
            if(toRestart){
                logger.warn("Worker is configured to restart on recovery and since internal recovery is needed the process is exiting...");
                System.exit(EXIT_STATUS);
            }

            synchronized (this){
                //If already in recovery - then return and do nothing
                if(inRecovery){
                    return;
                }
                inRecovery = true;

            }
            syncManager.startRecovery();

            logger.warn("Worker internal recovery started");

            for (WorkerRecoveryListener listener : listeners){
                try {
                    listener.doRecovery();
                } catch (Exception ex) {
                    logger.error("Failed on worker internal recovery", ex);
                }
            }
            if (logger.isDebugEnabled()) logger.debug("Listeners recovery is done");

            retryTemplate.retry(RetryTemplate.INFINITELY, 30*1000L, new RetryTemplate.RetryCallback() {
                @Override
                public void tryOnce() {
					if(logger.isDebugEnabled()) logger.debug("sending worker UP");
                    String newWrv = workerNodeService.up(System.getProperty("worker.uuid"), workerVersionService.getWorkerVersion(), workerVersionService.getWorkerVersionId());
                    setWRV(newWrv);
                    if(logger.isDebugEnabled()) logger.debug("the worker is UP");
                }
            });
            inRecovery = false;
            logger.warn("Worker recovery is done");
        } finally {
            syncManager.finishRecovery();
		}
	}

	public boolean isInRecovery() {
		return inRecovery;
	}

    public String getWRV() {
        return wrv;
    }

    public void setWRV(String newWrv) {
        wrv = newWrv;
    }
}
