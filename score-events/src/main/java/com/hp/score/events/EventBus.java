package com.hp.score.events;

import com.hp.score.api.ScoreEvent;

import java.util.Set;

/**
 * User: Amit Levin
 * Date: 09/01/14
 * Time: 12:06
 */
public interface EventBus {

	void subscribe(ScoreEventListener eventHandler, Set<String> eventTypes);

	void unsubscribe(ScoreEventListener eventHandler);

	void dispatch(ScoreEvent... eventWrappers);
}
