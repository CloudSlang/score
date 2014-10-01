package com.hp.score.worker.management.services;

/**
 * Date: 6/13/13
 *
 * @author Dima Rassin
 */
//TODO: Natasha/Evgeny Add Javadoc
public interface WorkerRecoveryManager {
	void doRecovery();
	boolean isInRecovery();
    String getWRV();
    void setWRV(String newWrv);
}
