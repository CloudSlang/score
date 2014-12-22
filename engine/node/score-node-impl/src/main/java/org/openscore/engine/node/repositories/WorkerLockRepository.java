/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package org.openscore.engine.node.repositories;

import org.openscore.engine.node.entities.WorkerLock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

/**
 * User: varelasa
 * Date: 20/07/14
 * Time: 11:29
 */
public interface WorkerLockRepository  extends JpaRepository<WorkerLock,Long> {

    @Modifying
    @Query("update WorkerLock w set w.uuid = w.uuid where w.uuid = ?1")
    int lock(String uuid);
    @Modifying
    @Query("delete from WorkerLock w where w.uuid = ?1")
    void deleteByUuid(String uuid);

}
