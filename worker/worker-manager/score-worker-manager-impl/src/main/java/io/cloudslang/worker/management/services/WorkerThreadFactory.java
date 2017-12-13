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

package io.cloudslang.worker.management.services;

import java.util.concurrent.ThreadFactory;

/**
 * Created with IntelliJ IDEA.
 * User: kravtsov
 * Date: 25/11/12
 * Time: 09:01
 */
class WorkerThreadFactory implements ThreadFactory {

    private int index;
    private String name;

    public WorkerThreadFactory(String commonName) {
        name = commonName;
    }

    public Thread newThread(final Runnable command) {
        return new Thread(new Runnable() {
            public void run() {
                command.run();
            }
        }, name + "-" + getThreadIDX());
    }

    private int getThreadIDX() {
        int idx;
        synchronized (this) {
            idx = index++;
        }
        return idx;
    }
}
