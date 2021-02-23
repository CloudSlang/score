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
package io.cloudslang.runtime.impl.python.pool;


import io.cloudslang.runtime.impl.python.EmbeddedPythonExecutorWrapper;
import org.vibur.objectpool.ConcurrentPool;
import org.vibur.objectpool.util.ConcurrentLinkedQueueCollection;

import static java.lang.Integer.getInteger;
import static java.lang.Long.getLong;
import static java.util.concurrent.TimeUnit.SECONDS;

public class ViburEmbeddedPythonPoolServiceImpl implements ViburEmbeddedPythonPoolService {

    private final ConcurrentPool<EmbeddedPythonExecutorWrapper> poolService;
    private final long timeout;

    public ViburEmbeddedPythonPoolServiceImpl(int numberOfThreads) {
        final int minPoolSize = getInteger("jython.executor.minPoolSize", numberOfThreads);
        final int maxPoolSize = getInteger("jython.executor.maxPoolSize", numberOfThreads);
        this.poolService = new ConcurrentPool<>(new ConcurrentLinkedQueueCollection<>(),
                new ViburEmbeddedPythonFactory(),
                minPoolSize,
                maxPoolSize,
                false);
        this.timeout = getLong("jython.executor.checkoutTimeoutSeconds", 10 * 60L); // 10 minutes
    }

    @Override
    public EmbeddedPythonExecutorWrapper tryTakeWithTimeout() {
        return poolService.tryTake(timeout, SECONDS);
    }

    @Override
    public void restore(EmbeddedPythonExecutorWrapper pooledObject) {
        poolService.restore(pooledObject);
    }

    @Override
    public void close() {
        try {
            poolService.close();
        } catch (Exception ignored) {
        }
    }
}
