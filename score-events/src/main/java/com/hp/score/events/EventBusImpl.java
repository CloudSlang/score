package com.hp.score.events;

import com.hp.score.api.ScoreEvent;

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

	public void subscribe(ScoreEventListener eventHandler, Set<String> eventTypes) {
		handlers.put(eventHandler, eventTypes);
	}

	public void unsubscribe(ScoreEventListener eventHandler) {
		handlers.remove(eventHandler);
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
