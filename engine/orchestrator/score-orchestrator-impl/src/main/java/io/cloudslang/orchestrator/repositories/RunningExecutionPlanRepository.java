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

import io.cloudslang.facade.entities.RunningExecutionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import javax.persistence.QueryHint;
import java.lang.Long;
import java.lang.String;
import java.util.List;

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
}
