package com.hp.oo.execution.services;

/**
 * Date: 6/13/13
 *
 * @author Dima Rassin
 */
public interface WorkerRecoveryManager {
	void doRecovery();
	boolean isInRecovery();
}
