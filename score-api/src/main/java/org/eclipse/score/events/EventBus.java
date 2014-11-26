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

import java.util.Set;

/**
 * User:
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
	void dispatch(ScoreEvent... eventWrappers) throws InterruptedException;
}
