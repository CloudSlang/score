package io.cloudslang.score.events;


import java.util.ArrayList;
import java.util.Set;

public interface ConfigurationAwareEventBus extends EventBus {

    void registerSubscriberForEvents(Object subscriber, Set<String> eventTypes);

    void unregisterSubscriberForEvents(Object subscriber, Set<String> eventTypes);

    void dispatchEvent(ScoreEvent scoreEvent) throws InterruptedException;

    void dispatchEvents(ArrayList<ScoreEvent> scoreEvent) throws InterruptedException;

    void initialize();

    void destroy();

}
