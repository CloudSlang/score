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
    private final ConcurrentMap<Long, Boolean> map;
    private final Thread waitingThread;

    public ExternalPythonTimeoutRunnable(long uniqueKey, ConcurrentMap<Long, Boolean> map, Thread thread) {
        this.uniqueKey = uniqueKey;
        this.map = map;
        this.waitingThread = thread;
    }

    @Override
    public void run() {
        map.put(uniqueKey, Boolean.TRUE);
        waitingThread.interrupt();
    }

}
