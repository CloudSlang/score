/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package org.eclipse.score.orchestrator.repositories;

import org.eclipse.score.orchestrator.entities.SuspendedExecution;
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

    @Query("from SuspendedExecution se where se.numberOfBranches=size(se.finishedBranches)")
    public List<SuspendedExecution> findFinishedSuspendedExecutions(Pageable pageRequest);
}
