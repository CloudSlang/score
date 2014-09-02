package com.hp.score.worker.management.services.dbsupport;
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
}
