package com.hp.score.worker.management.services.dbsupport;
import com.hp.score.facade.entities.RunningExecutionPlan;

/**
 * Created by IntelliJ IDEA.
 * User: kravtsov
 * Date: 03/07/12
 * Time: 08:39
 */
//TODO: Add Javadoc
public interface WorkerDbSupportService {
    RunningExecutionPlan readExecutionPlanById(Long id);
}
