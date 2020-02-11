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

import io.cloudslang.orchestrator.entities.ExecutionObjEntity;
import io.cloudslang.orchestrator.entities.SuspendedExecution;
import io.cloudslang.orchestrator.enums.SuspendedExecutionReason;
import io.cloudslang.score.facade.execution.ExecutionStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kravtsov
 * Date: 10/09/13
 * Time: 10:01
 */
public interface SuspendedExecutionsRepository extends JpaRepository<SuspendedExecution, Long> {
    List<SuspendedExecution> findBySplitIdIn(List<String> splitIds);

    @Query("from SuspendedExecution se where se.numberOfBranches=size(se.finishedBranches) and se.suspensionReason in :suspensionReasons")
    List<SuspendedExecution> findFinishedSuspendedExecutions(
            @Param("suspensionReasons") EnumSet<SuspendedExecutionReason> suspensionReasons,
            Pageable pageRequest);

    @Query("from SuspendedExecution se where size(se.finishedBranches) > 0 and se.suspensionReason in :suspensionReasons " +
            "and se.locked = false")
    List<SuspendedExecution> findUnmergedSuspendedExecutions(
            @Param("suspensionReasons") EnumSet<SuspendedExecutionReason> suspensionReasons,
            Pageable pageRequest);

    @Query("select se.executionId from SuspendedExecution se " +
            "left join io.cloudslang.orchestrator.entities.ExecutionState es " +
            "on se.executionId = cast(es.executionId as string)" +
            "where es.executionId IS NULL or (es.status = :status and se.suspensionReason = :suspendedReason)")
    List<String> collectCompletedSuspendedExecutions(Pageable pageable, ExecutionStatus status, SuspendedExecutionReason suspendedReason);

    @Query("delete from SuspendedExecution se where se.executionId in :ids")
    @Modifying
    int deleteByIds(@Param("ids") Collection<String> ids);

    SuspendedExecution findBySplitId(String splitId);

    @Modifying
    @Query("update SuspendedExecution se set se.executionObj = :newExecution, se.locked = false where se.id = :suspendedExecutionId")
    void updateSuspendedExecutionContexts(@Param("suspendedExecutionId") long suspendedExecutionId,
                                          @Param("newExecution") ExecutionObjEntity newExecution);
}
