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

import java.util.HashSet;
import java.util.Set;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

public class EventBusTest {

	private ScoreEventListener eventHandler = Mockito.mock(ScoreEventListener.class);
    private static final String EVENT_TYPE_1 = "type_1";
    private static final String EVENT_DATA_1 = "event_data_1";

    private EventBus eventBus = new EventBusImpl();
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

	@Before
	public void init() {
		eventBus = new EventBusImpl();
	}

	@Test
	public void testDispatch() throws Exception {
		Set<String> handlerTypes = new HashSet<>();
		handlerTypes.add(EVENT_TYPE_1);

		eventBus.subscribe(eventHandler, handlerTypes);

		ScoreEvent event = new ScoreEvent(EVENT_TYPE_1, EVENT_DATA_1);
		eventBus.dispatch(event);

		Mockito.verify(eventHandler, Mockito.times(1)).onEvent(event);

		event = new ScoreEvent("typeX", EVENT_DATA_1);
		eventBus.dispatch(event);

		Mockito.verify(eventHandler, Mockito.times(0)).onEvent(event);
	}

	@Test
	public void testUnsubscribe() throws Exception {
		Set<String> handlerTypes = new HashSet<>();
		handlerTypes.add(EVENT_TYPE_1);

		eventBus.subscribe(eventHandler, handlerTypes);
		eventBus.unsubscribe(eventHandler);

		ScoreEvent event = new ScoreEvent(EVENT_TYPE_1, EVENT_DATA_1);
		eventBus.dispatch(event);

		Mockito.verify(eventHandler, Mockito.times(0)).onEvent(event);
	}

	@Test
	public void testTwoMethodsInvokedBySameThread() throws Exception {
		final EventBus localEventBus = new EventBusImpl();
        Set<String> handlerTypes = new HashSet<>();
        handlerTypes.add(EVENT_TYPE_1);

        localEventBus.subscribe(
                new ScoreEventListener() {
                    @Override
                    public void onEvent(ScoreEvent event) throws InterruptedException {
                        localEventBus.unsubscribe(this);
                    }
                },
                handlerTypes
        );
        ScoreEvent event = new ScoreEvent(EVENT_TYPE_1, EVENT_DATA_1);

        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("Lock acquired twice by the same thread");

        localEventBus.dispatch(event);
	}

}