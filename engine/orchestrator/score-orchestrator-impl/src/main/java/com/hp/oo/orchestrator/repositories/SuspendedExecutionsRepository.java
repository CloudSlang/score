package com.hp.oo.orchestrator.repositories;

import com.hp.oo.orchestrator.entities.SuspendedExecution;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kravtsov
 * Date: 10/09/13
 * Time: 10:01
 */
public interface SuspendedExecutionsRepository extends JpaRepository<SuspendedExecution, Long> {
    public List<SuspendedExecution> findBySplitIdIn(List<String> splitIds);

    @Query("from SuspendedExecution se where se.numberOfBranches=size(se.finishedBranches)")
    public List<SuspendedExecution> findFinishedSuspendedExecutions(Pageable pageRequest);
}
