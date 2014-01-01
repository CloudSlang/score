package com.hp.oo.execution.services;

import com.hp.oo.internal.sdk.execution.Execution;

import java.util.List;

/**
 * Date: 8/1/11
 *
 * @author Dima Rassin
 */
public interface ExecutionService {
    static final String EXECUTION_METHOD = "execute"; //this string value should be identical to the main execution method
	Execution execute(Execution execution);
    List<Execution> executeSplit(Execution execution); //returns null in case this execution is paused or cancelled and the split was not done
    boolean isSplitStep(Execution execution);
}
