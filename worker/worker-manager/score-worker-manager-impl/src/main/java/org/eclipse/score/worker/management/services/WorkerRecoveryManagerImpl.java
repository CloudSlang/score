/*
 * Licensed to Hewlett-Packard Development Company, L.P. under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
*/
package org.eclipse.score.worker.management.services;

import org.eclipse.score.engine.node.services.WorkerNodeService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Date: 6/11/13
 *
 * @author Dima Rassin
 */
public class WorkerRecoveryManagerImpl implements WorkerRecoveryManager {

	protected static final Logger logger = Logger.getLogger(WorkerRecoveryManagerImpl.class);

	@Autowired
	private List<WorkerRecoveryListener> listeners;
	@Autowired
	private WorkerNodeService workerNodeService;

	@Autowired
	private RetryTemplate retryTemplate;

    @Autowired
    private SynchronizationManager syncManager;

	private volatile boolean inRecovery; //must be volatile since it is read/written in several threads

    private volatile String wrv; //must be volatile since it is read/written in several threads

	public void doRecovery(){
		try{
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
                    String newWrv = workerNodeService.up(System.getProperty("worker.uuid"));
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
