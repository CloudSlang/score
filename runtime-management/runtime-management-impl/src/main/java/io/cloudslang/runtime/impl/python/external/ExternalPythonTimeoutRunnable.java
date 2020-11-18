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
package io.cloudslang.runtime.impl.python.external;


import java.util.concurrent.ConcurrentMap;

public class ExternalPythonTimeoutRunnable implements Runnable {

    private final long uniqueKey;
    private final ConcurrentMap<Long, Object> map;

    public ExternalPythonTimeoutRunnable(long uniqueKey, ConcurrentMap<Long, Object> map) {
        this.uniqueKey = uniqueKey;
        this.map = map;
    }

    @Override
    public void run() {
        Object value = map.remove(uniqueKey);
        if (value instanceof Process) {
            Process process = (Process) value;
            map.put(uniqueKey, Boolean.TRUE);
            try {
                // To force waitFor to return after timeout
                process.destroy();
            } catch (Exception ignore) {
            }
        }
    }
}
