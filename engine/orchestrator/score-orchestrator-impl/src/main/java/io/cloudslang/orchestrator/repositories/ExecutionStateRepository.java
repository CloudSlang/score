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

package io.cloudslang.orchestrator.repositories;

import io.cloudslang.score.facade.execution.ExecutionStatus;
import io.cloudslang.orchestrator.entities.ExecutionState;
import java.util.Collection;
import java.util.Set;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * User:
 * Date: 12/05/2014
 */
public interface ExecutionStateRepository extends JpaRepository<ExecutionState, String> {

    public ExecutionState findByExecutionIdAndBranchId(long executionId, String branchId);

    public List<ExecutionState> findByExecutionId(Long executionId);

    public ExecutionState findByExecutionIdAndBranchIdAndStatusIn(Long executionId, String branchId, List<ExecutionStatus> statuses);

    @Query("select executionState.executionId from ExecutionState executionState where executionState.status in :statuses")
    public List<Long> findExecutionIdByStatuses(@Param("statuses") List<ExecutionStatus> statuses);

    @Query("select executionState.executionId from ExecutionState executionState where executionState.status = :status and branchId = :branchId")
    public List<Long> findByBranchIdAndStatusIn(@Param("branchId") String branchId, @Param("status") ExecutionStatus status);

    @Query("delete from ExecutionState se where se.executionId in :ids")
    @Modifying
    int deleteByIds(@Param("ids") Collection<Long> ids);

    @Query("select executionState.executionId from ExecutionState executionState where executionState.status = :status and updateTime <= :time")
    List<Long> findByStatusInAndUpdateTimeLessThanEqual(List<ExecutionStatus> statuses, long time, Pageable pageable);

    @Query("delete from ExecutionState executionState where executionState.status in :statuses")
    void deleteByStatusIn(@Param("statuses") List<ExecutionStatus> statuses);
}
