/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package io.cloudslang.score.events;

import java.util.Set;

public interface EventBus {

    /**
     * Register listener for event types
     * @param eventHandler  - the handler of the events
     * @param eventTypes - the types of events you want to listen to
     * @deprecated Use io.cloudslang.score.events.ConfigurationAwareEventBus#registerSubscriberForEvents(java.lang.Object, java.util.Set) instead.
     */
    @Deprecated
	void subscribe(ScoreEventListener eventHandler, Set<String> eventTypes);

    /**
     * Remove the given handler
     * @param eventHandler - the listener to remove
     * @deprecated Use io.cloudslang.score.events.ConfigurationAwareEventBus#unregisterSubscriberForEvents(java.lang.Object, java.util.Set) instead.
     */
    @Deprecated
	void unsubscribe(ScoreEventListener eventHandler);


    /**
     * Dispatch the given events, meaning relevant handlers will be called based on the event types
     * @param eventWrappers one or more score event to dispatch
     * @deprecated Use either io.cloudslang.score.events.ConfigurationAwareEventBus#dispatchEvent(io.cloudslang.score.events.ScoreEvent) or
     * io.cloudslang.score.events.ConfigurationAwareEventBus#dispatchEvents(java.util.ArrayList)
     */
    @Deprecated
    void dispatch(ScoreEvent... eventWrappers) throws InterruptedException;
}
