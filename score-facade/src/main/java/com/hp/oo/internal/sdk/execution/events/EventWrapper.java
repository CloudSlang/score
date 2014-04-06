package com.hp.oo.internal.sdk.execution.events;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: Amit Levin
 * Date: 09/01/14
 */
public class EventWrapper implements Serializable{

    private String eventType;
    private Object data;

    public EventWrapper(String eventType, Object data) {
        this.eventType = eventType;
        this.data = data;
    }

    public String getEventType() {
        return eventType;
    }

    public Object getData() {
        return data;
    }

}
