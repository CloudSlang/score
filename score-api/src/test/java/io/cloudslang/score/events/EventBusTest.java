/*
 * Copyright © 2014-2017 EntIT Software LLC, a Micro Focus company (L.P.)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.cloudslang.score.events;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import io.cloudslang.score.events.EventBus;
import io.cloudslang.score.events.EventBusImpl;
import io.cloudslang.score.events.ScoreEvent;
import io.cloudslang.score.events.ScoreEventListener;

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