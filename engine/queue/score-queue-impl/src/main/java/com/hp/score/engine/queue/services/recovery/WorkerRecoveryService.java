package com.hp.score.engine.queue.services.recovery;

/**
 * Created with IntelliJ IDEA.
 * User: kravtsov
 * Date: 8/6/14
 * Time: 9:12 AM
 */
public interface WorkerRecoveryService {

    /**
     * Used in order to recover non responsive worker
     * Also used during worker startup - in order to recover all data that was in worker before restart
     * @param workerUuid - the uuid of worker
     */
    void doWorkerRecovery(String workerUuid);

    /**
     * Used by the recovery job
     * Recovery will be done if the worker is non responsive or has not acknowledged messages
     * @param workerUuid - the uuid of worker
     */
    void doWorkerAndMessageRecovery(String workerUuid);
}
