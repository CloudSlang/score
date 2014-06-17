package com.hp.score.api;

import java.io.Serializable;

/**
 * User: maromg
 * Date: 10/06/2014
 */
public class ScoreEvent implements Serializable {

    private String eventType;
    private Serializable data;

    public ScoreEvent(String eventType, Serializable data) {
        this.eventType = eventType;
        this.data = data;
    }

    public String getEventType() {
        return eventType;
    }

    public Serializable getData() {
        return data;
    }
}
