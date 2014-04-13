package com.hp.oo.engine.execution.events.services;

import com.hp.oo.enginefacade.execution.ExecutionEnums;
import com.hp.oo.enginefacade.execution.log.StepInfo;
import com.hp.oo.enginefacade.execution.log.StepLog;
import com.hp.oo.internal.sdk.execution.events.ExecutionEvent;

import java.lang.Long;import java.lang.String;import java.util.List;

/**
 * Date: 3/6/12
 *
 * @author Dima Rassin
 */
public interface ExecutionEventService {
    /**
     *
     * @param events
     */
    void createEvents(List<ExecutionEvent> events);

    /**
     *
     * @param executionId
     * @return
     */
    List<ExecutionEvent> readEventsByExecutionId(String executionId); // used by FlowExecutionController in flow-execution-impl

    /**
     * Returns a representation of the steps under the specified path for a given executionId
     * This method will aggregate the event table into an hierarchical representation of it for a given
     * execution
     * @param executionId the execution id
     * @param path a path (in the form of %d.%d.%d) for a given parent step
     * @return a list of the child steps
     */
    List<StepInfo> readStepsInfoByPath(String executionId, String path);

    /**
     * Returns a representation of the steps under the specified path for a given executionId
     * This method will aggregate the event table into an hierarchical representation of it for a given
     * execution
     * @param executionId the execution id
     * @param path a path (in the form of %d.%d.%d) for a given parent step
     * @param count the max number of steps to returned
     * @return a list of the child steps
     */
    List<StepInfo> readStepsInfoByPath(String executionId, String path, int count);

    /**
     * Returns a representation of all the steps for a given executionId for use when execution logLevel = ERROR
     * This method will aggregate the event table into an hierarchical representation of it for a given
     * execution
     * @param executionId the execution id
     * @return a list of the child steps
     */
    List<StepInfo> readAllStepsInfo(String executionId);

    /**
     * Returns an aggregation of all the events for a given step
     * @param executionId the execution id
     * @param path a path (in the form of %d.%d.%d) for a given step
     * @return an object of aggregated data
     */
    StepLog readExecutionStepLogByPath(String executionId, String path);

    /**
     * Returns the event containing the data for a specific pause
     * @param executionId the uuid of the relevant execution
     * @param pauseId the id of the pause entity
     * @return an event
     */
    ExecutionEvent readPauseEvent(String executionId, long pauseId);

    List<ExecutionEvent> readEventsByExecutionIdByEventType(String executionId, ExecutionEnums.Event... eventType);

    List<ExecutionEvent> readEventsByExecutionIdAndIndexGreaterByEventType(String executionId, Long index , ExecutionEnums.Event... eventType) ;

    /**
     *
     * @param executionEvents
     * @return
     */
    StepLog aggregateEventsToStepLog(List<ExecutionEvent> executionEvents);

    /**
     * Reads <param>numOfEvents</param> events for the given <param>executionId</param>. Only events with the step path
     * bigger than or equal to <param>stepPath</param> will be read.
     * @param executionId the execution id for which to read events
     * @param sequenceOrder the step path from which to start reading (inclusive)
     * @param numOfEvents the number of events to read
     */
    List<ExecutionEvent> readEventsByExecutionIdAndSeqOrder(String executionId, String sequenceOrder, int numOfEvents);

    /**
     * Reads events for the given <param>executionId</param>. Only events with the step path
     * @param executionId
     * @param stepPath
     * @return
     */
    List<ExecutionEvent> readEventsByExecutionIdAndStepPath(String executionId, String stepPath);

    void deleteEventsByExecutionId(String executionId);
}
