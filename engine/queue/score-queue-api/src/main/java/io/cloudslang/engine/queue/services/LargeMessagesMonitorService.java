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
package io.cloudslang.engine.queue.services;

public interface LargeMessagesMonitorService {

    int DEFAULT_EXPIRATION_TIME = 60 * 60;
    int DEFAULT_NO_RETRIES = 5;

    String MESSAGE_EXPIRATION_TIME_PROP = "queue.message.expiration.time.seconds";
    String NUMBER_OF_RETRIES_KEY = "message.queue.no.retries";

    default Integer getMessageExpirationTime() {
        return Integer.getInteger(MESSAGE_EXPIRATION_TIME_PROP, DEFAULT_EXPIRATION_TIME);
    }

    default Integer getNoRetries() {
        return Integer.getInteger(NUMBER_OF_RETRIES_KEY, DEFAULT_NO_RETRIES);
    }

    void monitor();
}
