package com.hp.score.worker.management.services.dbsupport;
import com.hp.score.facade.entities.RunningExecutionPlan;

/**
 * Created by IntelliJ IDEA.
 * User: kravtsov
 * Date: 03/07/12
 * Time: 08:39
 */
public interface WorkerDbSupportService {
    /**
     *
     * @param id of the running execution plan
     * @return the running execution plan of the given id
     */
    RunningExecutionPlan readExecutionPlanById(Long id);
}
