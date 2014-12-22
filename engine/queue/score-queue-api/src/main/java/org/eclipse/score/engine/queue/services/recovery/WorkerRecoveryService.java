/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package org.eclipse.score.engine.queue.services.recovery;

/**
 * Created with IntelliJ IDEA.
 * User: kravtsov
 * Date: 8/6/14
 * Time: 9:12 AM
 */
public interface WorkerRecoveryService {

    /**
     * Used in order to recover non responsive worker
     * Also used during worker startup - in order to recover all data that was in worker before restart
     * @param workerUuid - the uuid of worker
     */
    void doWorkerRecovery(String workerUuid);

    /**
     * Used by the recovery job
     * Recovery will be done if the worker is non responsive or has not acknowledged messages
     * @param workerUuid - the uuid of worker
     */
    void doWorkerAndMessageRecovery(String workerUuid);
}
