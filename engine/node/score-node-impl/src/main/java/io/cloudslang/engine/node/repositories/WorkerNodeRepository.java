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

package io.cloudslang.engine.node.repositories;

import io.cloudslang.engine.node.entities.WorkerNode;
import io.cloudslang.score.api.nodes.WorkerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User:
 * Date: 11/11/12
 */
public interface WorkerNodeRepository extends JpaRepository<WorkerNode,Long> {
    WorkerNode findByUuidAndDeleted(String uuid, boolean deleted);

    WorkerNode findByUuid(String uuid);

    List<WorkerNode> findByActiveAndDeleted(boolean active, boolean deleted);

    List<WorkerNode> findByDeletedOrderByIdAsc(boolean deleted);

	List<WorkerNode> findByActiveAndStatusAndDeleted(boolean isActive, WorkerStatus status, boolean deleted);

	@Query("select w from WorkerNode w where (w.active = ?1) and (w.status = ?2) and (w.deleted = ?3) and ((w.versionId = ?4) or (w.versionId is null))")
	List<WorkerNode> findByActiveAndStatusAndDeletedAndVersionId(boolean isActive, WorkerStatus status, boolean deleted, String versionId);

	List<WorkerNode> findByGroupsAndDeleted(String group, boolean deleted);

	@Query("select w.uuid from WorkerNode w where (w.ackVersion < ?1) and w.status <> ?2")
	List<String> findNonRespondingWorkers(long ackVersion, WorkerStatus status);

	@Query("select distinct g from WorkerNode w join w.groups g where w.deleted = false")
	List<String> findGroups();

	@Query("select distinct g from WorkerNode w join w.groups g where g in ?1")
	List<String> findGroups(List<String> groupName);

	@Modifying @Query("update WorkerNode w set w.uuid = w.uuid where w.uuid = ?1")
	void lockByUuid(String uuid);
}