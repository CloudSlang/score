package com.hp.oo.internal.sdk.execution.events;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: Amit Levin
 * Date: 09/01/14
 */
//we implement Serializable just because RAS uses spring-remoting that requires it
//it should be removed from score and handled in oo
public class EventWrapper implements Serializable {

    private String eventType;
    private Object event;

    public EventWrapper(String eventType, Object event) {
        this.eventType = eventType;
        this.event = event;
    }

    public String getEventType() {
        return eventType;
    }

    public Object getEvent() {
        return event;
    }

}
