package com.hp.score.worker.execution.services;

import com.hp.oo.internal.sdk.execution.Execution;

import java.util.List;

/**
 * Date: 8/1/11
 *
 * @author Dima Rassin
 */
public interface ExecutionService {
	Execution execute(Execution execution) throws InterruptedException;
    List<Execution> executeSplit(Execution execution); //returns null in case this execution is paused or cancelled and the split was not done
    boolean isSplitStep(Execution execution);
}
