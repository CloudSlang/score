package com.hp.score.repositories;

import com.hp.oo.enginefacade.execution.ExecutionEnums;
import com.hp.score.entities.RunState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * User: maromg
 * Date: 12/05/2014
 */
public interface RunStateRepository extends JpaRepository<RunState, String> {

    public RunState findByRunIdAndBranchId(String runId, String branchId);

    public List<RunState> findByRunId(String runId);

    public RunState findByRunIdAndBranchIdAndStatusIn(String runId, String branchId, List<ExecutionEnums.ExecutionStatus> statuses);

    @Query("select runState.runId from RunState runState where runState.status in :statuses")
    public List<String> findRunIdByStatuses(@Param("statuses") List<ExecutionEnums.ExecutionStatus> statuses);
}
