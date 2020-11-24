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


import org.apache.commons.lang3.mutable.MutableBoolean;

public class ExternalPythonTimeoutRunnable implements Runnable {

    private final MutableBoolean processDestroyed;
    private final Process process;

    public ExternalPythonTimeoutRunnable(MutableBoolean processDestroyed, Process process) {
        this.processDestroyed = processDestroyed;
        this.process = process;
    }

    @Override
    public void run() {
        try {
            processDestroyed.setTrue();
            process.destroy();
        } catch (Exception ignore) {
        }
    }

}
