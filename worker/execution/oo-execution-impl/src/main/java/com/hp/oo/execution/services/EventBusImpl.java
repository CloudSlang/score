package com.hp.oo.execution.services;

import com.hp.oo.internal.sdk.execution.events.EventBus;
import com.hp.oo.internal.sdk.execution.events.EventHandler;
import com.hp.oo.internal.sdk.execution.events.EventWrapper;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * User: hajyhia
 * Date: 1/19/14
 * Time: 5:51 PM
 */
public class EventBusImpl implements EventBus {

    private Map<EventHandler,Set<String>> handlers = new ConcurrentHashMap<>() ;


    public void register(EventHandler eventHandler, Set<String> eventTypes){
        handlers.put(eventHandler,eventTypes);
    }

    public void unRegister(EventHandler eventHandler){
        handlers.remove(eventHandler);
    }

    public void dispatch(EventWrapper... eventWrappers){
        for (EventHandler eventHandler : handlers.keySet()){
            Set<String> eventTypes = handlers.get(eventHandler);
            for (EventWrapper eventWrapper:eventWrappers){
                if (eventTypes.contains(eventWrapper.getEventType()))
                    eventHandler.handleEvent(eventWrapper);
            }
        }
    }

}
