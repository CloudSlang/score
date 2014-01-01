package com.hp.oo.execution.services;


import com.hp.oo.execution.debug.ExecutionInterrupt;

import java.util.Map;
import java.util.Set;

import static com.hp.oo.execution.debug.ExecutionInterrupt.InterruptType;

/**
 * User: hajyhia
 * Date: 1/17/13
 * Time: 12:55 PM
 */
public interface ExecutionInterruptsService {

    /**
     *
     * @param executionId
     * @param executionInterrupts
     * @return
     */
    Long createExecutionBreakpointInterrupts(String executionId, Set<ExecutionInterrupt> executionInterrupts);

    /**
     *
     * @param executionId
     * @param executionInterrupts
     * @return
     */
    Long createExecutionResponsesOverrideInterrupts(String executionId, Set<ExecutionInterrupt> executionInterrupts);

    /**
     *
     * @param executionId
     * @param map
     * @return
     */
    Long createExecutionDebugInterrupts(String executionId, Map<String, String> map);

    /**
     *
     * @param executionId
     * @param interruptType
     * @param key
     * @param value
     * @return
     */
    ExecutionInterrupt readExecutionDebugInterrupts(String executionId, InterruptType interruptType, String key, String value);

    /**
     *
     * @param executionId
     * @param interruptType
     * @param key
     * @param value
     * @param unRegister
     * @return
     */
    ExecutionInterrupt readExecutionDebugInterrupts(String executionId, InterruptType interruptType, String key, String value, boolean unRegister);

    /**
     *
     * @param executionId
     * @param overrideAll
     * @return
     */
    Long setExecutionOverrideAllResponses(String executionId, boolean overrideAll);

    /**
     * delete execution debug interrupts
     * @param executionId
     */
    void removeExecutionDebugInterrupts(String executionId);

    void removeExecutionDebugInterrupts(String executionId, InterruptType interruptType);
}
