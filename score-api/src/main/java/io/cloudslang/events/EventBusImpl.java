/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package io.cloudslang.events;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * User: hajyhia
 * Date: 1/19/14
 * Time: 5:51 PM
 */
public class EventBusImpl implements EventBus {

	private Map<ScoreEventListener, Set<String>> handlers = new ConcurrentHashMap<>();

	public void subscribe(ScoreEventListener eventListener, Set<String> eventTypes) {
		handlers.put(eventListener, eventTypes);
	}

	public void unsubscribe(ScoreEventListener eventListener) {
		handlers.remove(eventListener);
	}

    public void dispatch(ScoreEvent... events)  throws InterruptedException {
        for (ScoreEventListener eventHandler : handlers.keySet()) {
            Set<String> eventTypes = handlers.get(eventHandler);
            for (ScoreEvent eventWrapper : events) {
                if (eventTypes.contains(eventWrapper.getEventType())) {
                    eventHandler.onEvent(eventWrapper);
                }
			}
		}
	}

}
