package com.hp.oo.internal.sdk.execution.events;

/**
 * Created with IntelliJ IDEA.
 * User: Amit Levin
 * Date: 09/01/14
 * To change this template use File | Settings | File Templates.
 */
public interface EventHandler {

    void handleEvent(EventWrapper eventWrapper);

    boolean equals(Object o);

    int hashCode();

}
