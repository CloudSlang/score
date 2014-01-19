package com.hp.oo.internal.sdk.execution.events;

/**
 * Created with IntelliJ IDEA.
 * User: Amit Levin
 * Date: 09/01/14
 * Time: 11:59
 * To change this template use File | Settings | File Templates.
 */
public class EventWrapper {

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
