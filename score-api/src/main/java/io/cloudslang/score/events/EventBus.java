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

import java.util.Set;

/**
 * User:
 * Date: 09/01/14
 * Time: 12:06
 */
public interface EventBus {

    /**
     * register listener for event types
     * @param eventHandler  - the handler of the events
     * @param eventTypes - the types of events you want to listen to
     */
	void subscribe(ScoreEventListener eventHandler, Set<String> eventTypes);

    /**
     * remove the given handler
     * @param eventHandler - the listener to remove
     */
	void unsubscribe(ScoreEventListener eventHandler);

    /**
     * dispatch the given events, meaning relevant handlers will be called based on the event types
     * @param eventWrappers one or more score event to dispatch
     */
	void dispatch(ScoreEvent... eventWrappers) throws InterruptedException;
}
