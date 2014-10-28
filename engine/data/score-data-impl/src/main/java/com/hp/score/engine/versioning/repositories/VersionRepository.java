/*
 * Licensed to Hewlett-Packard Development Company, L.P. under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
*/
package com.hp.score.engine.versioning.repositories;

import com.hp.score.engine.versioning.entities.VersionCounter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * User: wahnonm
 * Date: 31/10/13
 * Time: 16:26
 */
public interface VersionRepository extends JpaRepository<VersionCounter, Long> {

    VersionCounter findByCounterName(String counterName);

    @Modifying
    @Query("update VersionCounter v set v.versionCount=v.versionCount+1 where v.counterName = :counterName")
    int incrementCounterByName(@Param("counterName") String counterName);
}
