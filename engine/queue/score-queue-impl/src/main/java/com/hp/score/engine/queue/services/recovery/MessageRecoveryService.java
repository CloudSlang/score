package com.hp.score.engine.queue.services.recovery;

import com.hp.score.engine.queue.entities.ExecStatus;
import com.hp.score.engine.queue.entities.ExecutionMessage;

import java.util.List;

/**
 * User: varelasa
 * Date: 22/07/14
 * Time: 13:20
 */
//TODO: Add Javadoc
//TODO: move to api module
public interface MessageRecoveryService {

    boolean recoverMessagesBulk(String workerName, int defaultPoolSize);

    void logMessageRecovery(List<ExecutionMessage> messages);

    void enqueueMessages(List<ExecutionMessage> messages, ExecStatus messageStatus);
}
