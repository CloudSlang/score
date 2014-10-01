package com.hp.score.engine.node.services;

import com.google.common.collect.Multimap;
import com.hp.score.api.nodes.WorkerStatus;
import com.hp.score.engine.node.entities.WorkerNode;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Amit Levin
 * Date: 08/11/12
 */
//TODO: Add Javadoc
public interface WorkerNodeService {

    /**
     * Update the Worker Node entity with the current ack version for the keep alive mechanism
     * @param uuid worker's unique identifier
     * @return the worker's recovery version (WRV)
     */
	String keepAlive(String uuid);

    /**
     * Create a new worker
     * @param uuid  worker's unique identifier
     * @param password worker's password
     * @param hostName  worker's host
     * @param installDir worker's installation directory
     */
	void create(String uuid, String password, String hostName, String installDir);

    void updateWorkerToDeleted(String uuid);

    List<WorkerNode> readAllNotDeletedWorkers();

	String up(String uuid);

   // find not deleted worker by uuid
	WorkerNode readByUUID(String uuid);

    // is not relating to IS_DELETED property
    WorkerNode findByUuid(String uuid);

	List<WorkerNode> readAllWorkers();

	List<String> readNonRespondingWorkers();

	List<WorkerNode> readWorkersByActivation(boolean isActive);

	void activate(String uuid);

	void deactivate(String uuid);

	void updateEnvironmentParams(String uuid, String os, String jvm, String dotNetVersion);

	void updateStatus(String uuid, WorkerStatus status);

    void updateStatusInSeparateTransaction(String uuid, WorkerStatus status);

    List<String> readAllWorkerGroups();

    List<String> readWorkerGroups(String uuid);

	void updateWorkerGroups(String uuid, String... groupNames);

	Multimap<String, String> readGroupWorkersMapActiveAndRunning();

	void addGroupToWorker(String workerUuid, String group);

	void removeGroupFromWorker(String workerUuid, String group);

	List<String> readWorkerGroups(List<String> groups);

    void updateBulkNumber(String workerUuid, String bulkNumber);

    void updateWRV(String workerUuid, String wrv);

    List<String> readAllWorkersUuids();
}