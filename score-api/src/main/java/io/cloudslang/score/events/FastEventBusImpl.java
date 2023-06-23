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

import java.util.Objects;

public class FastEventBusImpl implements FastEventBus {

    private UninterruptibleScoreEventListener eventHandler;

    public void registerEventListener(UninterruptibleScoreEventListener eventHandler) {
        Objects.requireNonNull(eventHandler, "eventHandler must not be null");
        if (this.eventHandler == null ) {
            this.eventHandler = eventHandler;
        } else {
            StringBuilder errorMessage = new StringBuilder();
            errorMessage.append("Failed to register '").append(eventHandler.getClass().getName())
                    .append("' because another '").append(this.eventHandler.getClass().getName())
                    .append("' is registered. You cannot register more than one eventHandler for FastEventBus.");
            throw new RuntimeException(errorMessage.toString());
        }
    }

    public void dispatch(ScoreEvent event) {
        if (eventHandler != null) {
            eventHandler.onEvent(event);
        }
    }
}
