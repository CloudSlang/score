package com.hp.score.events;

import com.hp.score.api.ScoreEvent;
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