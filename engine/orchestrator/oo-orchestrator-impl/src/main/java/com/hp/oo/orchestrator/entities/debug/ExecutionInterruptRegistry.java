package com.hp.oo.orchestrator.entities.debug;

import com.hp.oo.execution.debug.ExecutionInterrupt;

import java.io.Serializable;
import java.util.Collection;

public interface ExecutionInterruptRegistry extends Serializable {

    /**
     *
     * @param interruptId
     * @return
     */
	ExecutionInterrupt getInterruptById(String interruptId);

    /**
     *
     * @param key
     * @param value
     * @return
     */
    ExecutionInterrupt getInterruptByKey(String key, String value);

	/** returns the interrupt object used for interrupt all, which is not necessarily a singleton */
	ExecutionInterrupt getUniversalInterrupt();

    /**
     * add ExecutionInterrupt to registry
     * @param bp
     * @return
     */
	boolean registerDebugInterrupt(ExecutionInterrupt bp);

    /**
     * Remove ExecutionInterrupt from registry
     * @param bp
     * @return
     */
	boolean unregisterDebugInterrupt(ExecutionInterrupt bp);

    /**
     * Disable all registered interrupts
     */
	void disableAll();

    /**
     * * Enable all registered interrupts
     */
	void enableAll();
	
	/**
	 * removes all debug interrupts from the registry and returns them 
	 * @return the cleared interrupts.
	 */
	Collection<ExecutionInterrupt> clearAll();

    /**
     * Return number of interrupts registered on this registry
     * @return
     */
	int size();
	
	/**
	 * This is a way to say "I want all steps in all flow to produce interrupts
	 * of all kinds". For instance, I want to override the responses of all
	 * steps, or I want to have a breakpoint in all of the steps. Clients of the
	 * registry should check this flag prior to checking anything else.
	 *  
	 * @param interruptAll whether all steps produce interrupts.
	 * @return the old value
	 */
	boolean setInterruptAll(boolean interruptAll);
	
	boolean isInterruptAll();
}