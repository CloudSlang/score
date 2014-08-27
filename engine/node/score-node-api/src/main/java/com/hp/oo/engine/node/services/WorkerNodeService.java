package com.hp.oo.engine.node.services;

import com.google.common.collect.Multimap;
import com.hp.oo.engine.node.entities.WorkerNode;
import com.hp.oo.enginefacade.Worker;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Amit Levin
 * Date: 08/11/12
 */
public interface WorkerNodeService {

	String keepAlive(String uuid);

	void create(String uuid, String password, String hostName, String installDir);

	void delete(String uuid);

    void updateWorkerToDeleted(String uuid);

    List<WorkerNode> readAllNotDeletedWorkers();

	String up(String uuid);

	void down(String uuid);

	void changePassword(String uuid, String password);
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

	void updateDescription(String uuid, String description);

	void updateStatus(String uuid, Worker.Status status);

    void updateStatusInSeparateTransaction(String uuid, Worker.Status status);

    List<String> readAllWorkerGroups();

    List<String> readWorkerGroups(String uuid);

	void updateWorkerGroups(String uuid, String... groupNames);

	List<WorkerNode> readWorkersByGroup(String groupName, boolean onlyForActiveWorkers);

	Multimap<String, String> readGroupWorkersMap(boolean onlyForActiveWorkers);

	Multimap<String, String> readGroupWorkersMapActiveAndRunning();

	void addGroupToWorker(String workerUuid, String group);

	void removeGroupFromWorker(String workerUuid, String group);

	List<String> readWorkerGroups(List<String> groups);

	void lock(String uuid);

    void updateBulkNumber(String workerUuid, String bulkNumber);

    void updateWRV(String workerUuid, String wrv);

    List<String> readAllWorkersUuids();
}