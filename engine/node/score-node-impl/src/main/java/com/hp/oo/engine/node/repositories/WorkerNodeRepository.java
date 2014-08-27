package com.hp.oo.engine.node.repositories;

import com.hp.oo.engine.node.entities.WorkerNode;
import com.hp.oo.enginefacade.Worker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Amit Levin
 * Date: 11/11/12
 */
public interface WorkerNodeRepository extends JpaRepository<WorkerNode,Long> {
    WorkerNode findByUuidAndDeleted(String uuid, boolean deleted);

    WorkerNode findByUuid(String uuid);

    List<WorkerNode> findByActiveAndDeleted(boolean active, boolean deleted);

    List<WorkerNode> findByDeleted(boolean deleted);

	List<WorkerNode> findByActiveAndStatusAndDeleted(boolean isActive, Worker.Status status, boolean deleted);

	List<WorkerNode> findByGroupsAndDeleted(String group, boolean deleted);

	@Query("select w.uuid from WorkerNode w where (w.ackVersion < ?1) and w.status <> ?2")
	List<String> findNonRespondingWorkers(long ackVersion, Worker.Status status);

	@Query("select distinct g from WorkerNode w join w.groups g where w.deleted = false")
	List<String> findGroups();

	@Query(value = "update WorkerNode w set w.ackTime = current_time where w.uuid = ?1")
	@Modifying
	void updateAckTime(String uuid);


	@Query("select distinct g from WorkerNode w join w.groups g where g in ?1")
	List<String> findGroups(List<String> groupName);

	@Modifying @Query("update WorkerNode w set w.uuid = w.uuid where w.uuid = ?1")
	void lockByUuid(String uuid);
}