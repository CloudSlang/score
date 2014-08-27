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
