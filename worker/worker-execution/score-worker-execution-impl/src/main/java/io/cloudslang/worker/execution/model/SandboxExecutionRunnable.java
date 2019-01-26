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
package io.cloudslang.worker.execution.model;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;


public class SandboxExecutionRunnable<T> implements Runnable {

    private final ClassLoader classLoader;
    private final AtomicReference<Pair<T, RuntimeException>> bundle;
    private final Callable<T> callable;

    public SandboxExecutionRunnable(ClassLoader classLoader, Callable<T> callable) {
        this.classLoader = classLoader;
        this.bundle = new AtomicReference<>();
        this.callable = callable;
    }

    @Override
    public void run() {
        T processingResult = null;
        RuntimeException processingException = null;

        try {
            Thread.currentThread().setContextClassLoader(classLoader);
            processingResult = callable.call();
        } catch (RuntimeException re) {
            processingException = re;
        } catch (Exception exception) {
            processingException = new RuntimeException(exception);
        }

        bundle.set(new ImmutablePair<>(processingResult, processingException));
    }
}
