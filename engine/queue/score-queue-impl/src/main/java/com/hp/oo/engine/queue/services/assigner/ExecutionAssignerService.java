package com.hp.oo.engine.queue.services.assigner;

import com.hp.oo.engine.queue.entities.ExecutionMessage;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Amit Levin
 * Date: 19/11/12
 */
public interface ExecutionAssignerService {

    List<ExecutionMessage> assignWorkers(List<ExecutionMessage> messages);
}
