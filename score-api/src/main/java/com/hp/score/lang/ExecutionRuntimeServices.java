package com.hp.score.lang;

import com.hp.score.api.ScoreEvent;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

/**
 * User: maromg
 * Date: 11/06/2014
 */
public class ExecutionRuntimeServices implements Serializable {

    private static final long serialVersionUID = 2557429503280678353L;

    protected static final String EXECUTION_PAUSED = "EXECUTION_PAUSED";

    protected static final String SCORE_EVENTS_QUEUE = "SCORE_EVENTS_QUEUE";

    protected Map<String, Serializable> myMap = new HashMap<>();

    public void pause() {
        myMap.put(EXECUTION_PAUSED, Boolean.TRUE);
    }

    public boolean isPaused() {
        return myMap.containsKey(EXECUTION_PAUSED) && myMap.get(EXECUTION_PAUSED).equals(Boolean.TRUE);
    }

    public void addEvent(String eventType, Serializable eventData) {
        @SuppressWarnings("unchecked")
        Queue<ScoreEvent> eventsQueue = getFromMap(SCORE_EVENTS_QUEUE);
        if (eventsQueue == null) {
            eventsQueue = new ArrayDeque<>();
            myMap.put(SCORE_EVENTS_QUEUE, (ArrayDeque) eventsQueue);
        }
        eventsQueue.add(new ScoreEvent(eventType, eventData));
    }

    public ArrayDeque<ScoreEvent> getEvents() {
        return getFromMap(SCORE_EVENTS_QUEUE);
    }

    protected <T> T getFromMap(String key) {
        if (myMap.containsKey(key)) {
            Serializable value = myMap.get(key);
            if (value != null) {
                @SuppressWarnings("unchecked")
                T retVal = (T) value;
                return retVal;
            }
        }
        return null;
    }

}
