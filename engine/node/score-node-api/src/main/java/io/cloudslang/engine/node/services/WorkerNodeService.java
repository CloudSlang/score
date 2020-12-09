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

package io.cloudslang.engine.node.services;

import com.google.common.collect.Multimap;
import io.cloudslang.engine.node.entities.WorkerKeepAliveInfo;
import io.cloudslang.engine.node.entities.WorkerNode;
import io.cloudslang.score.api.nodes.WorkerStatus;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by IntelliJ IDEA. User: Date: 08/11/12
 * <p>
 * A service responsible for handling a worker node record
 */
public interface WorkerNodeService {

    /**
     * Update the Worker Node entity with the current ack version for the keep alive mechanism
     *
     * @param uuid worker's unique identifier Maintained for backward compatibility only
     * @return the worker's recovery version (WRV)
     */
    String keepAlive(String uuid);

    /**
     * Update the Worker Node entity with the current ack version for the keep alive mechanism
     *
     * @param uuid worker's unique identifier
     * @return the WorkerKeepAliveInfo that contains the worker's recovery version (WRV) and the enable state
     */
    WorkerKeepAliveInfo newKeepAlive(String uuid);

    /**
     * Create a new worker
     *
     * @param uuid worker's unique identifier
     * @param password worker's password
     * @param hostName worker's host
     * @param installDir worker's installation directory
     */
    void create(String uuid, String password, String hostName, String installDir);

    /**
     * update a worker record to IS_DELETED state
     *
     * @param uuid the uuid of the worker to mark deleted
     */
    void updateWorkerToDeleted(String uuid);

    /**
     * update a worker record to its not deleted state
     *
     * @param uuid the uuid of the worker to mark as not deleted
     */
    void updateWorkerToNotDeleted(String uuid);

    /**
     * Reads all of the workers that are not marked with the IS_DELETED flag
     *
     * @return a List of {@link io.cloudslang.engine.node.entities.WorkerNode} that are ont marked with the IS_DELETED
     * flag
     */
    List<WorkerNode> readAllNotDeletedWorkers();

    /**
     * Notifies the orchestrator that a worker went up
     *
     * @param uuid the the uuid of the worker that went up
     * @param version the version of the worker that went up
     * @param versionId the versionId of the worker that went up
     * @return a String of the current recovery version of the worker
     */
    String up(String uuid, String version, String versionId);

    /**
     * Notifies the orchestrator that a worker went up
     *
     * @param uuid the the uuid of the worker that went up
     * @return a String of the current recovery version of the worker
     */
    String up(String uuid);

    /**
     * find not deleted worker by uuid
     *
     * @param uuid the uuid of the worker to find
     * @return a {@link io.cloudslang.engine.node.entities.WorkerNode} of the requested worker
     */
    WorkerNode readByUUID(String uuid);

    /**
     * Returns active status of the worker according to the worker UUID
     */
    boolean isActive(String workerUuid);

    /**
     * find worker without relating to the IS_DELETED property
     *
     * @param uuid the uuid of the worker to find
     * @return a {@link io.cloudslang.engine.node.entities.WorkerNode} of the requested worker
     */
    WorkerNode findByUuid(String uuid);

    /**
     * Reads all of the workers records
     *
     * @return a List of all existing {@link io.cloudslang.engine.node.entities.WorkerNode}
     */
    List<WorkerNode> readAllWorkers();

    /**
     * Read all of the worker that didn't send keep alive for a certain amount of time
     *
     * @return a List of String of the non-responding worker ids
     */
    List<String> readNonRespondingWorkers();

    /**
     * read all worker that there activation status is as a given status
     *
     * @param isActive the requested activation status.
     * @return a List of all {@link io.cloudslang.engine.node.entities.WorkerNode} the their activation status is as the
     * given status
     */
    List<WorkerNode> readWorkersByActivation(boolean isActive);

    /**
     * activates a worker
     *
     * @param uuid the uuid of the worker to activate
     */
    void activate(String uuid);

    /**
     * deactivate a worker
     *
     * @param uuid the uuid of the worker to deactivate
     */
    void deactivate(String uuid);

    /**
     * updates the environment params of a given worker
     *
     * @param uuid the uuid of the worker to update
     * @param os the operating system the worker is running on
     * @param jvm the jvm version the worker is running on
     * @param dotNetVersion the dot-net version the worker is using
     */
    void updateEnvironmentParams(String uuid, String os, String jvm, String dotNetVersion);

    /**
     * updates the status of a given worker
     *
     * @param uuid the uuid of the worker to update
     * @param status the status to update the given worker to
     */
    void updateStatus(String uuid, WorkerStatus status);

    /**
     * updates the status of a given worker in a separate transaction
     *
     * @param uuid the uuid of the worker to update
     * @param status the status to update the given worker to
     */
    void updateStatusInSeparateTransaction(String uuid, WorkerStatus status);

    /**
     * updates the migration password field of a worker node
     *
     * @param uuid the uuid of the worker to update
     * @param password the migrated password to be used
     */
    void migratePassword(String uuid, String password);

    /**
     * Reads all of the worker groups
     *
     * @return a List of String of all existing worker groups
     */
    List<String> readAllWorkerGroups();

    /**
     * Read all of the groups associated with a worker
     *
     * @param uuid the the uuid of the worker to find groups for
     * @return a List of String of the associated group names
     */
    List<String> readWorkerGroups(String uuid);

    /**
     * updates the groups associated with a worker
     *
     * @param uuid the uuid of the worker to update groups for
     * @param groupNames the groups to associate with the worker
     */
    void updateWorkerGroups(String uuid, String... groupNames);

    /**
     * Reads all of the worker that are active and running and their groups
     *
     * @param versionId - the version of workers
     * @return A {@link com.google.common.collect.Multimap} of the active and running workers in specific version and
     * their groups
     */
    Multimap<String, String> readGroupWorkersMapActiveAndRunningAndVersion(String versionId);

    /**
     * adds group to be associated with a worker
     *
     * @param workerUuid the uuid of the worker to associate the group to
     * @param group the group to associate with the worker
     */
    void addGroupToWorker(String workerUuid, String group);

    /**
     * removes a group association with a worker
     *
     * @param workerUuid the uuid of the worker to remove the group association from
     * @param group the group to remove its association from the worker
     */
    void removeGroupFromWorker(String workerUuid, String group);

    /**
     * Reads all of the groups that matches the given group names
     *
     * @param groups the group names to match
     * @return t List of String of the matched groups
     */
    List<String> readWorkerGroups(List<String> groups);

    /**
     * updates the worker recovery bulk number
     *
     * @param workerUuid the uuid of the worker to update
     * @param bulkNumber the new recovery bulk number
     */
    void updateBulkNumber(String workerUuid, String bulkNumber);

    /**
     * updates the worker recovery version of a given worker
     *
     * @param workerUuid the uuid of the worker to update
     * @param wrv the new worker recovery version
     */
    void updateWRV(String workerUuid, String wrv);

    /**
     * Retrieves the worker and worker groups as a map
     */
    Map<String, Set<String>> readWorkerGroupsMap();

    /**
     * Read all workers uuids
     *
     * @return a List of String of the worker uuids
     */
    List<String> readAllWorkersUuids();

    /**
     * updates the worker version of a given worker
     *
     * @param workerUuid the uuid of the worker to update
     * @param version the worker's version
     * @param versionId comparable worker's version
     */
    void updateVersion(String workerUuid, String version, String versionId);

    /**
     * updates worker's password encoding
     *
     * @param workerUuid the uuid of the worker to be updated
     * @param encodedPassword the newly encoded password of the worker
     */
    void updateMigratedPassword(String workerUuid, String encodedPassword);
    void updateWorkerBusynessValue(String uuid, String workerBusynessValue);
}
