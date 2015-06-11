/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package io.cloudslang.orchestrator.repositories;

import io.cloudslang.orchestrator.entities.SuspendedExecution;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kravtsov
 * Date: 10/09/13
 * Time: 10:01
 */
public interface SuspendedExecutionsRepository extends JpaRepository<SuspendedExecution, Long> {
    public List<SuspendedExecution> findBySplitIdIn(List<String> splitIds);

    public List<SuspendedExecution> findByExecutionId(String executionId);

    @Query("from SuspendedExecution se where se.numberOfBranches=size(se.finishedBranches)")
    public List<SuspendedExecution> findFinishedSuspendedExecutions(Pageable pageRequest);
}
