package com.hp.oo.internal.sdk.execution.events;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * @author Ronen Shaban
 */
public class ExecutionEventSequenceOrder implements java.io.Serializable {
    private Deque<Integer> flowDepth = new ArrayDeque<>();
    private Deque<Integer> eventDepth = new ArrayDeque<>();

    private  StringBuffer flowPath =  new StringBuffer();
    private  StringBuffer eventPath =  new StringBuffer();

    private Integer flowSequence = 0;
    private Integer eventSequence = 0;

    private Deque<Integer> flowDepthForEvent = new ArrayDeque<>();
    private Integer flowSequenceForEvent = 0;

    public StringBuffer getFlowPath() {
        return flowPath;
    }

    public void setFlowPath(StringBuffer flowPath) {
        this.flowPath = flowPath;
    }

    public StringBuffer getEventPath() {
        return eventPath;
    }

    public void setEventPath(StringBuffer eventPath) {
        this.eventPath = eventPath;
    }

    public Integer getFlowSequence() {
        return flowSequence;
    }

    public void setFlowSequence(int flowSequence) {
        this.flowSequence = flowSequence;
    }


    public Integer getFlowSequenceForEvent() {
        return flowSequenceForEvent;
    }

    public void setFlowSequenceForEvent(Integer flowSequenceForEvent) {
        this.flowSequenceForEvent = flowSequenceForEvent;
    }

    public void increaseFlowDepthForEvent(Integer depthNumber) {
         flowDepthForEvent.addFirst(depthNumber);
    }

    public Integer decreaseFlowDepthForEvent() {
         return flowDepthForEvent.pollFirst();
    }

    public Deque<Integer> getFlowDepthForEvent() {
         return flowDepthForEvent;
    }


    public Integer getEventSequence() {
        return eventSequence;
    }

    public void setEventSequence(int eventSequence) {
        this.eventSequence = eventSequence;
    }

    public void increaseFlowDepth(Integer depthNumber) {
         flowDepth.addFirst(depthNumber);
    }

    public Integer decreaseFlowDepth() {
         return flowDepth.pollFirst();
    }

    public Deque<Integer> getFlowDepth() {
         return flowDepth;
    }

    public void increaseEventDepth(Integer depthNumber) {
        eventDepth.addFirst(depthNumber);
    }

    public Integer decreaseEventDepth() {
         return eventDepth.pollFirst();
    }

    public Deque<Integer> getEventDepth() {
         return eventDepth;
    }

}
