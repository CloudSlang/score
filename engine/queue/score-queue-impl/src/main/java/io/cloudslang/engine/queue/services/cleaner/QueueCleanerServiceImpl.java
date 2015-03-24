/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package io.cloudslang.engine.queue.services.cleaner;

import io.cloudslang.engine.queue.repositories.ExecutionQueueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User:
 * Date: 14/10/13
 */
final public class QueueCleanerServiceImpl  implements QueueCleanerService {

    final private int BULK_SIZE = 500;

    @Autowired
   	private ExecutionQueueRepository executionQueueRepository;

    @Override
    @Transactional
    public Set<Long> getFinishedExecStateIds() {
        return executionQueueRepository.getFinishedExecStateIds();
    }

    @Override
    @Transactional
    public void cleanFinishedSteps(Set<Long> ids) {
        executionQueueRepository.deleteFinishedSteps(ids);
    }

}
