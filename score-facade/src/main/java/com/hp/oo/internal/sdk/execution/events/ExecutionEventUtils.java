package com.hp.oo.internal.sdk.execution.events;

import com.hp.oo.internal.sdk.execution.ExecutionConstants;
import org.apache.commons.lang.ArrayUtils;

import java.io.Serializable;
import java.util.Deque;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Ronen Shaban
 */
public  class ExecutionEventUtils {
    private static final String DELIMITER =  ".";

    public static ExecutionEventSequenceOrder startFlow(Map<String, Serializable> systemContext) {
        ExecutionEventSequenceOrder executionEventSequenceOrder = new ExecutionEventSequenceOrder();
        systemContext.put(ExecutionConstants.EXECUTION_EVENT_SEQUENCE_ORDER, executionEventSequenceOrder);

         return increaseDepth(systemContext);
    }


    public static ExecutionEventSequenceOrder increaseStep(Map<String, Serializable> systemContext) {
        ExecutionEventSequenceOrder executionEventSequenceOrder = (ExecutionEventSequenceOrder) systemContext.get(ExecutionConstants.EXECUTION_EVENT_SEQUENCE_ORDER);
        // increase Flow Sequence
        executionEventSequenceOrder.setFlowSequence(executionEventSequenceOrder.getFlowSequence() +1);
        executionEventSequenceOrder.setFlowSequenceForEvent(executionEventSequenceOrder.getFlowSequenceForEvent() +1);

        initEventInfo(executionEventSequenceOrder, 0);
         return executionEventSequenceOrder;
    }

    public static ExecutionEventSequenceOrder increaseStepForEvent(Map<String, Serializable> systemContext) {
        ExecutionEventSequenceOrder executionEventSequenceOrder = (ExecutionEventSequenceOrder) systemContext.get(ExecutionConstants.EXECUTION_EVENT_SEQUENCE_ORDER);
        // increase Flow Sequence for event
        executionEventSequenceOrder.setFlowSequenceForEvent(executionEventSequenceOrder.getFlowSequenceForEvent() +1);

        initEventInfo(executionEventSequenceOrder, 0);
         return executionEventSequenceOrder;
    }


    //  move to sub-flow, parallel or multiple step
    public static ExecutionEventSequenceOrder increaseDepth(Map<String, Serializable> systemContext) {
        ExecutionEventSequenceOrder executionEventSequenceOrder = (ExecutionEventSequenceOrder) systemContext.get(ExecutionConstants.EXECUTION_EVENT_SEQUENCE_ORDER);

        // push to flow stack  - flowSequence   and later on the  eventSequence
        executionEventSequenceOrder.increaseFlowDepth(executionEventSequenceOrder.getFlowSequence());
        executionEventSequenceOrder.increaseFlowDepthForEvent(executionEventSequenceOrder.getFlowSequenceForEvent());

        // push to event stack  -  the  eventSequence
        executionEventSequenceOrder.increaseEventDepth(executionEventSequenceOrder.getEventSequence());

        // initialize Flow Sequence
        initFlowSequence(executionEventSequenceOrder);
        initFlowSequenceForEvent(executionEventSequenceOrder);

        initEventInfo(executionEventSequenceOrder, 0);
         return executionEventSequenceOrder;
    }


    public static ExecutionEventSequenceOrder decreaseDepth(Map<String, Serializable> systemContext) {
        ExecutionEventSequenceOrder executionEventSequenceOrder = (ExecutionEventSequenceOrder) systemContext.get(ExecutionConstants.EXECUTION_EVENT_SEQUENCE_ORDER);
        // pop from flow stack   - flowSequence
        Integer lastFlowSequence = executionEventSequenceOrder.decreaseFlowDepth();
        Integer lastFlowSequenceForEvent = executionEventSequenceOrder.decreaseFlowDepthForEvent();

         // pop from event stack - eventSequence
        Integer lastEventSequence = executionEventSequenceOrder.decreaseEventDepth();
         // only for the event order: after decreaseDepth we should do increaseStep
        lastFlowSequenceForEvent++;

        // update  last Flow Sequence
        executionEventSequenceOrder.setFlowSequence(lastFlowSequence) ;
        executionEventSequenceOrder.setFlowSequenceForEvent(lastFlowSequenceForEvent);

        initEventInfo(executionEventSequenceOrder, lastEventSequence);
         return executionEventSequenceOrder;
    }


    public static ExecutionEventSequenceOrder increaseEvent(Map<String, Serializable> systemContext) {
        ExecutionEventSequenceOrder executionEventSequenceOrder = (ExecutionEventSequenceOrder) systemContext.get(ExecutionConstants.EXECUTION_EVENT_SEQUENCE_ORDER);
        // increase Event Sequence
        executionEventSequenceOrder.setEventSequence(executionEventSequenceOrder.getEventSequence() + 1);

         // update event Path
        StringBuffer eventPath = createEventPath(executionEventSequenceOrder.getFlowDepthForEvent(), executionEventSequenceOrder.getFlowSequenceForEvent() );
        eventPath.append(DELIMITER).append(executionEventSequenceOrder.getEventSequence());
        executionEventSequenceOrder.setEventPath(eventPath);

         return executionEventSequenceOrder;
    }


    private static void initEventInfo(ExecutionEventSequenceOrder executionEventSequenceOrder, Integer eventSequence) {
        executionEventSequenceOrder.setEventSequence(eventSequence);
         // update flowPath
        StringBuffer updatedFlowPath = createPath(executionEventSequenceOrder.getFlowDepth(), executionEventSequenceOrder.getFlowSequence() );
        executionEventSequenceOrder.setFlowPath(updatedFlowPath);
         // update Event Path
        StringBuffer updateEventPath = createEventPath(executionEventSequenceOrder.getFlowDepthForEvent(), executionEventSequenceOrder.getFlowSequenceForEvent() );
         executionEventSequenceOrder.setEventPath(updateEventPath.append(DELIMITER).append(executionEventSequenceOrder.getEventSequence()));
    }


    private static StringBuffer createPath(Deque<Integer> flowDepth, Integer nextFlowSequence ) {
        StringBuffer flowPath = new StringBuffer();

        if (ArrayUtils.isNotEmpty(flowDepth.toArray())) {
            for (Iterator<Integer> iterator = flowDepth.descendingIterator(); iterator.hasNext(); ) {
                Integer stepDepth = iterator.next();
                if ( stepDepth >= 0 ){
                    flowPath.append(stepDepth).append(DELIMITER);
                }else{
                    flowPath.append("0").append(DELIMITER);
                }
            }

            if(nextFlowSequence >= 0){
                flowPath.append(nextFlowSequence);
            } else {
                flowPath.deleteCharAt(flowPath.length() -1);
            }
        }
        return flowPath;
    }


    private static StringBuffer createEventPath(Deque<Integer> flowDepth, Integer nextFlowSequence ) {
        StringBuffer eventPath = new StringBuffer();

        if (ArrayUtils.isNotEmpty(flowDepth.toArray())) {
            for (Iterator<Integer> iterator = flowDepth.descendingIterator(); iterator.hasNext(); ) {
                Integer  stepDepth = iterator.next();
                if ( stepDepth >= 0 ){
                    eventPath.append(stepDepth).append(DELIMITER);
                }else{
                    eventPath.append("0").append(DELIMITER);
                }
            }
            eventPath.append(nextFlowSequence);
        }
        return eventPath;
    }

    private static void initFlowSequence(ExecutionEventSequenceOrder executionEventSequenceOrder){
        executionEventSequenceOrder.setFlowSequence(-1);
    }


    private static void initFlowSequenceForEvent(ExecutionEventSequenceOrder executionEventSequenceOrder){
        executionEventSequenceOrder.setFlowSequenceForEvent(0);
    }

}
