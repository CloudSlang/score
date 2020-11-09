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

import io.cloudslang.orchestrator.entities.FinishedBranch;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
/**
 * Created with IntelliJ IDEA.
 * User: kravtsov
 * Date: 07/11/13
 * Time: 14:16
 */
public interface FinishedBranchRepository extends JpaRepository<FinishedBranch, Long> {
    @Query("select distinct fe.id from FinishedBranch fe " +
            "left join io.cloudslang.orchestrator.entities.SuspendedExecution se " +
            "on fe.suspendedExecution.id = se.id " +
            "where se.id IS NULL")
    List<Long> collectOrphanFinishedBranches(Pageable pageable);

    @Query("delete from FinishedBranch fe where fe.id in :ids")
    @Modifying
    void deleteByIds(@Param("ids") Collection<Long> ids);
}
