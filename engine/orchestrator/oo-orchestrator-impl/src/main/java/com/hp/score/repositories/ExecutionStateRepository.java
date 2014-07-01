package com.hp.score.repositories;

import com.hp.oo.enginefacade.execution.ExecutionEnums;
import com.hp.score.entities.ExecutionState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * User: maromg
 * Date: 12/05/2014
 */
public interface ExecutionStateRepository extends JpaRepository<ExecutionState, String> {

    public ExecutionState findByExecutionIdAndBranchId(Long executionId, String branchId);

    public List<ExecutionState> findByExecutionId(Long executionId);

    public ExecutionState findByExecutionIdAndBranchIdAndStatusIn(Long executionId, String branchId, List<ExecutionEnums.ExecutionStatus> statuses);

    @Query("select executionState.executionId from ExecutionState executionState where executionState.status in :statuses")
    public List<Long> findExecutionIdByStatuses(@Param("statuses") List<ExecutionEnums.ExecutionStatus> statuses);
}
