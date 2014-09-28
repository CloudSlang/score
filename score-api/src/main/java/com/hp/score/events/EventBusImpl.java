package com.hp.score.events;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * User: hajyhia
 * Date: 1/19/14
 * Time: 5:51 PM
 */
//TODO: Add Javadoc
//TODO: Move to impl module
public class EventBusImpl implements EventBus {

	private Map<ScoreEventListener, Set<String>> handlers = new ConcurrentHashMap<>();

	public void subscribe(ScoreEventListener eventListener, Set<String> eventTypes) {
		handlers.put(eventListener, eventTypes);
	}

	public void unsubscribe(ScoreEventListener eventListener) {
		handlers.remove(eventListener);
	}

	public void dispatch(ScoreEvent... events) {
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
