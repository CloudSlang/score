package com.hp.oo.execution.services;

import com.hp.oo.internal.sdk.execution.events.EventBus;
import com.hp.oo.internal.sdk.execution.events.EventHandler;
import com.hp.score.api.ScoreEvent;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.*;

public class EventBusTest {

    private EventHandler eventHandler = mock(EventHandler.class);

    private EventBus eventBus = new EventBusImpl();

    @Before
    public void init(){
        eventBus = new EventBusImpl();
    }

    @Test
    public void testDispatch() throws Exception {
        Set<String> handlerTypes = new HashSet<>();
        handlerTypes.add("type1");

        eventBus.register(eventHandler,handlerTypes);


        ScoreEvent event = new ScoreEvent("type1","event");
        eventBus.dispatch(event);

        verify(eventHandler,times(1)).handleEvent(event);

        event = new ScoreEvent("typeX","event");
        eventBus.dispatch(event);

        verify(eventHandler,times(0)).handleEvent(event);
    }

}