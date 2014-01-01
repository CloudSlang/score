package com.hp.oo.internal.sdk.execution.events;

import java.util.List;

/**
 * User: zruya
 * Date: 31/07/12
 * Time: 15:56
 */
public class ExecutionEventContainer {
    private List<ExecutionEvent> executionEventList;

    public ExecutionEventContainer() {
    }

    public ExecutionEventContainer(List<ExecutionEvent> executionEventList) {
        this.executionEventList = executionEventList;
    }

    public List<ExecutionEvent> getExecutionEventList() {
        return executionEventList;
    }

}
