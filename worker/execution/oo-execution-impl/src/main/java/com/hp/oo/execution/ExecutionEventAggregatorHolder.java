package com.hp.oo.execution;

import com.hp.oo.internal.sdk.execution.events.ExecutionEvent;

import java.util.List;

public class ExecutionEventAggregatorHolder {
    private static final ThreadLocal<List<ExecutionEvent>> aggregatedExecutionEvents = new ThreadLocal<>();

    public static void setAggregatedExecutionEvents(List<ExecutionEvent> aggregatedExecutionEvents) {
        ExecutionEventAggregatorHolder.aggregatedExecutionEvents.set(aggregatedExecutionEvents);
    }

    public static void removeAggregatedExecutionEvents() {
        ExecutionEventAggregatorHolder.aggregatedExecutionEvents.remove();
    }

    public void aggregateExecutionEvent(ExecutionEvent event) {
        ExecutionEventAggregatorHolder.aggregatedExecutionEvents.get().add(event);
    }
}
