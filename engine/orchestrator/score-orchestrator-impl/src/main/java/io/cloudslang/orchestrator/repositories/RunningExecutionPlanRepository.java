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

import io.cloudslang.score.facade.entities.RunningExecutionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.QueryHint;
import java.lang.Long;
import java.lang.String;
import java.util.List;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

/**
 * Created by IntelliJ IDEA.
 * User: lernery
 * Date: 11/24/11
 * Time: 9:41 AM
 */
public interface RunningExecutionPlanRepository extends JpaRepository<RunningExecutionPlan,Long> {

    //We are not using the default name findByUuid() because then we won't be able to use the query cache
    //enhancement request should be opened to spring JPA
    @Query("from RunningExecutionPlan r where r.flowUUID = :flowUUID")
    @QueryHints({ @QueryHint(name = "org.hibernate.cacheable", value ="true") })
    public List<RunningExecutionPlan> findByUuidCached(@Param("flowUUID") String flowUUID);

	@Query("select distinct r from RunningExecutionPlan r where r.id = :exeId and r.flowUUID = :flowId")
	RunningExecutionPlan getExecution(@Param("flowId") String flowId, @Param("exeId") Long exeId);


	@Query("select executionPlanZipped from RunningExecutionPlan r where r.id = :exeId")
	byte[] getZippedExecutionPlan(@Param("exeId") Long exeId);

    @Query("delete from RunningExecutionPlan r where r.id in (:runningPlansId) AND r.inUseCount = 0")
    @Modifying
    int deleteFinishedExecPlans(@Param("runningPlansId") List<Long> runningPlansId);

    @Query("update RunningExecutionPlan r  set r.inUseCount = (r.inUseCount + 1) WHERE r.id = :id")
    @Modifying
    @Transactional(isolation=Isolation.READ_COMMITTED,propagation = Propagation.REQUIRES_NEW)
    int incrementUseOfExecutionPlan(@Param("id") Long id);

    @Query("update RunningExecutionPlan r  set r.inUseCount = (r.inUseCount - 1) WHERE r.id in (:runningPlansId)")
    @Modifying
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    int decrementUseOfExecutionPlan(@Param("runningPlansId") List<Long> runningPlansId);
}
