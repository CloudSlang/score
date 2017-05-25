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

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class EventBusImpl implements ConfigurationAwareEventBus {

	private static final String ILLEGAL_SUBSCRIBER_TYPE = "Unknown subscriber type for bus.";
	private Map<ScoreEventListener, Set<String>> handlers = new ConcurrentHashMap<>();

	public void subscribe(ScoreEventListener eventListener, Set<String> eventTypes) {
		handlers.put(eventListener, eventTypes);
	}

	public void unsubscribe(ScoreEventListener eventListener) {
		handlers.remove(eventListener);
	}

	public void dispatch(ScoreEvent... events) throws InterruptedException {
		for (ScoreEventListener eventHandler : handlers.keySet()) {
			Set<String> eventTypes = handlers.get(eventHandler);
			for (ScoreEvent eventWrapper : events) {
				if (eventTypes.contains(eventWrapper.getEventType())) {
					eventHandler.onEvent(eventWrapper);
				}
			}
		}
	}

	@Override
	public void registerSubscriberForEvents(Object subscriber, Set<String> eventTypes) {
		if (!(subscriber instanceof ScoreEventListener)) {
			throw new IllegalStateException(ILLEGAL_SUBSCRIBER_TYPE);
		}
		handlers.put(((ScoreEventListener) subscriber), eventTypes);
	}

	@Override
	public void unregisterSubscriberForEvents(Object subscriber, Set<String> eventTypes) {
		if (!(subscriber instanceof ScoreEventListener)) {
			throw new IllegalStateException(ILLEGAL_SUBSCRIBER_TYPE);
		}
		handlers.remove(subscriber);
	}

	@Override
	public void dispatchEvent(ScoreEvent scoreEvent) throws InterruptedException {
		doDispatch(scoreEvent);
	}

	@Override
	public void dispatchEvents(ArrayList<ScoreEvent> scoreEvents) throws InterruptedException {
		for (ScoreEvent scoreEvent : scoreEvents) {
			doDispatch(scoreEvent);
		}
	}

	@Override
	public void initialize() {
	}

	@Override
	public void destroy() {
	}

	private void doDispatch(ScoreEvent scoreEvent) throws InterruptedException {
		for (ScoreEventListener eventHandler : handlers.keySet()) {
			Set<String> eventTypes = handlers.get(eventHandler);
			if (eventTypes.contains(scoreEvent.getEventType())) {
				eventHandler.onEvent(scoreEvent);
			}
		}
	}

}
