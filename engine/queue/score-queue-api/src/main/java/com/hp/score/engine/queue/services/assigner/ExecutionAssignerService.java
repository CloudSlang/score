package com.hp.score.engine.queue.services.assigner;

import com.hp.score.engine.queue.entities.ExecutionMessage;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Amit Levin
 * Date: 19/11/12
 */
//TODO: Add Javadoc
public interface ExecutionAssignerService {

    List<ExecutionMessage> assignWorkers(List<ExecutionMessage> messages);
}
