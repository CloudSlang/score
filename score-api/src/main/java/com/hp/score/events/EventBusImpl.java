/*
 * Licensed to Hewlett-Packard Development Company, L.P. under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
*/
package com.hp.score.events;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * User: hajyhia
 * Date: 1/19/14
 * Time: 5:51 PM
 */
public class EventBusImpl implements EventBus {

	private Map<ScoreEventListener, Set<String>> handlers = new ConcurrentHashMap<>();

	public void subscribe(ScoreEventListener eventListener, Set<String> eventTypes) {
		handlers.put(eventListener, eventTypes);
	}

	public void unsubscribe(ScoreEventListener eventListener) {
		handlers.remove(eventListener);
	}

    public void dispatch(ScoreEvent... events)  throws InterruptedException {
        for (ScoreEventListener eventHandler : handlers.keySet()) {
            Set<String> eventTypes = handlers.get(eventHandler);
            for (ScoreEvent eventWrapper : events) {
                if (eventTypes.contains(eventWrapper.getEventType())) {
                    eventHandler.onEvent(eventWrapper);
                }
			}
		}
	}

}
