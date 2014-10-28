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
package com.hp.score.engine.node.repositories;

import com.hp.score.engine.node.entities.WorkerLock;
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
