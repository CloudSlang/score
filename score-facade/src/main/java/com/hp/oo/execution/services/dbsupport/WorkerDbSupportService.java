package com.hp.oo.execution.services.dbsupport;
import com.hp.oo.broker.entities.BranchContextHolder;
import com.hp.oo.broker.entities.RunningExecutionPlan;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: kravtsov
 * Date: 03/07/12
 * Time: 08:39
 */
public interface WorkerDbSupportService {
    RunningExecutionPlan readExecutionPlanById(Long id);

     //For parallel execution
    List<BranchContextHolder> readBranchContextById(String splitId);
    Long createBranchContext(BranchContextHolder branchContextHolder);
    void deleteParallelRecordsById(String splitId);

    int countBranchContextBySplitId(String splitId);
}
