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

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * User: hajyhia
 * Date: 1/19/14
 * Time: 5:51 PM
 */
public class EventBusImpl implements EventBus {

	private ReentrantLock reentrantLock = new ReentrantLock();

	private Map<ScoreEventListener, Set<String>> handlers = new ConcurrentHashMap<>();

	public void subscribe(ScoreEventListener eventListener, Set<String> eventTypes) {
		acquireLock();
		try {
			handlers.put(eventListener, eventTypes);
		} finally {
			releaseLock();
		}
	}

	public void unsubscribe(ScoreEventListener eventListener) {
		acquireLock();
		try {
			handlers.remove(eventListener);
		} finally {
			releaseLock();
		}
	}

    public void dispatch(ScoreEvent... events)  throws InterruptedException {
		acquireLock();
		try {
			for (ScoreEventListener eventHandler : handlers.keySet()) {
				Set<String> eventTypes = handlers.get(eventHandler);
				for (ScoreEvent eventWrapper : events) {
					if (eventTypes.contains(eventWrapper.getEventType())) {
						eventHandler.onEvent(eventWrapper);
					}
				}
			}
		} finally {
			releaseLock();
		}
	}

	private void acquireLock() {
		reentrantLock.lock();
		if (reentrantLock.getHoldCount() > 1) {
			throw new RuntimeException("Lock acquired twice by the same thread: two mutually exclusive regions are accessed.");
		}
	}

	private void releaseLock() {
		reentrantLock.unlock();
	}

}
