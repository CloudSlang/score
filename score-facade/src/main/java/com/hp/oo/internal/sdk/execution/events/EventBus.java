package com.hp.oo.internal.sdk.execution.events;

import com.hp.score.api.ScoreEvent;

import java.util.Set;

/**
 * User: Amit Levin
 * Date: 09/01/14
 * Time: 12:06
 */
public interface EventBus {

    void register(EventHandler eventHandler, Set<String> eventTypes);

    void unRegister(EventHandler eventHandler);

    void dispatch(ScoreEvent... eventWrappers);
}
