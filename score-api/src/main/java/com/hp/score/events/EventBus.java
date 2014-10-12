package com.hp.score.events;

import java.util.Set;

/**
 * User: Amit Levin
 * Date: 09/01/14
 * Time: 12:06
 */
public interface EventBus {

    /**
     *  register listener to event types
     * @param eventHandler  - the handler of the events
     * @param eventTypes - the types of events you want to listen to
     */
	void subscribe(ScoreEventListener eventHandler, Set<String> eventTypes);

    /**
     * removw the given handler
     * @param eventHandler - the listener to remove
     */
	void unsubscribe(ScoreEventListener eventHandler);

    /**
     * dispatch the given events, means according to their types relevant handlers will be called
     * @param eventWrappers one or more score event to dispatch
     */
	void dispatch(ScoreEvent... eventWrappers);
}
