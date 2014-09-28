package com.hp.score.engine.queue.services.cleaner;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: Amit Levin
 * Date: 14/10/13
 */
//TODO: Add Javadoc
public interface QueueCleanerService {

    Set<Long> getFinishedExecStateIds();

    void cleanFinishedSteps(Set<Long> ids);
}
