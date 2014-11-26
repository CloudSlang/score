/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package org.eclipse.score.events;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.HashSet;
import java.util.Set;

public class EventBusTest {

	private ScoreEventListener eventHandler = Mockito.mock(ScoreEventListener.class);

	private EventBus eventBus = new EventBusImpl();

	@Before
	public void init() {
		eventBus = new EventBusImpl();
	}

	@Test
	public void testDispatch() throws Exception {
		Set<String> handlerTypes = new HashSet<>();
		handlerTypes.add("type1");

		eventBus.subscribe(eventHandler, handlerTypes);

		ScoreEvent event = new ScoreEvent("type1", "event");
		eventBus.dispatch(event);

		Mockito.verify(eventHandler, Mockito.times(1)).onEvent(event);

		event = new ScoreEvent("typeX", "event");
		eventBus.dispatch(event);

		Mockito.verify(eventHandler, Mockito.times(0)).onEvent(event);
	}

	@Test
	public void testUnsubscribe() throws Exception {
		Set<String> handlerTypes = new HashSet<>();
		handlerTypes.add("type1");

		eventBus.subscribe(eventHandler, handlerTypes);
		eventBus.unsubscribe(eventHandler);

		ScoreEvent event = new ScoreEvent("type1", "event");
		eventBus.dispatch(event);

		Mockito.verify(eventHandler, Mockito.times(0)).onEvent(event);
	}

}