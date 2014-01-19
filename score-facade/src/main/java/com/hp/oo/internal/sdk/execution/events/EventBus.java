package com.hp.oo.internal.sdk.execution.events;

import java.util.Set;

/**
 * User: Amit Levin
 * Date: 09/01/14
 * Time: 12:06
 */
public interface EventBus {

    void register(EventHandler eventHandler, Set<String> eventTypes);

    void unRegister(EventHandler eventHandler);

    void dispatch(EventWrapper... eventWrappers);
}
