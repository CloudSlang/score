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

import io.cloudslang.score.facade.execution.ExecutionStatus;
import io.cloudslang.orchestrator.entities.ExecutionState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * User:
 * Date: 12/05/2014
 */
public interface ExecutionStateRepository extends JpaRepository<ExecutionState, String> {

    public ExecutionState findByExecutionIdAndBranchId(Long executionId, String branchId);

    public List<ExecutionState> findByExecutionId(Long executionId);

    public ExecutionState findByExecutionIdAndBranchIdAndStatusIn(Long executionId, String branchId, List<ExecutionStatus> statuses);

    @Query("select executionState.executionId from ExecutionState executionState where executionState.status in :statuses")
    public List<Long> findExecutionIdByStatuses(@Param("statuses") List<ExecutionStatus> statuses);
}
