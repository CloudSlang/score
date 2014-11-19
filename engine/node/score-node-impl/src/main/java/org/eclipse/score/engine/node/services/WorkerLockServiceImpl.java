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
package org.eclipse.score.engine.node.services;

import org.eclipse.score.engine.node.entities.WorkerLock;
import org.eclipse.score.engine.node.repositories.WorkerLockRepository;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * User: varelasa
 * Date: 20/07/14
 * Time: 11:27
 */
//@Service
public final class WorkerLockServiceImpl implements  WorkerLockService{

    private final Logger logger = Logger.getLogger(getClass());

    @Autowired
    private WorkerLockRepository workerLockRepository;

    @Override
    @Transactional
    public void create(String uuid) {

        WorkerLock workerLock = new WorkerLock();
        workerLock.setUuid(uuid);
        workerLockRepository.save(workerLock);
    }

    @Override
    @Transactional
    public void delete(String uuid) {

        workerLockRepository.deleteByUuid(uuid);
    }

    @Override
    @Transactional
    public void lock(String uuid) {
        if (workerLockRepository.lock(uuid) == 1){
            if (logger.isDebugEnabled()) logger.debug("Worker [" + uuid + "] is locked");
        }
        else {
            throw new IllegalStateException("Unknown worker uuid [" + uuid + "]");
        }
    }
}
