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
package io.cloudslang.runtime.impl.python;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.cloudslang.dependency.api.services.DependencyService;
import io.cloudslang.runtime.api.python.PythonEvaluationResult;
import io.cloudslang.runtime.api.python.PythonExecutionResult;
import io.cloudslang.runtime.impl.ExecutionEngine;
import io.cloudslang.runtime.impl.python.pool.ViburEmbeddedPythonPoolService;
import io.cloudslang.runtime.impl.python.pool.ViburEmbeddedPythonPoolServiceImpl;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.vibur.objectpool.util.ConcurrentLinkedQueueCollection;

import jakarta.annotation.PostConstruct;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static io.cloudslang.runtime.api.python.enums.PythonStrategy.JYTHON;
import static io.cloudslang.runtime.api.python.enums.PythonStrategy.PYTHON_EXECUTOR;
import static io.cloudslang.runtime.api.python.enums.PythonStrategy.getPythonStrategy;
import static java.lang.Integer.getInteger;
import static java.lang.Math.max;
import static java.lang.Runtime.getRuntime;
import static java.util.concurrent.Executors.newFixedThreadPool;

/**
 * Uses io.cloudslang.runtime.impl.python.EmbeddedPythonExecutorWrapper that brings more security
 * than io.cloudslang.runtime.impl.python.PythonExecutor
 * For python that has no dependencies, it uses a pool of EmbeddedPythonExecutorWrapper instances,
 * that are totally isolated not using ThreadLocal at all.
 * For python with dependencies, it uses a dedicated cache of EmbeddedPythonExecutorWrapper.
 */
public class PythonExecutionPooledAndCachedEngine extends ExecutionEngine implements PythonExecutionEngine {

    @Autowired
    private DependencyService dependencyService;

    @Autowired
    @Qualifier("numberOfExecutionThreads")
    private Integer numberOfThreads;

    private ViburEmbeddedPythonPoolService pythonExecutorPool;
    private Cache<String, EmbeddedPythonExecutorWrapper> cachedExecutors;

    @PostConstruct
    public void init() {
        this.cachedExecutors = Caffeine.newBuilder()
                .maximumSize(getInteger("jython.executor.cacheSize", numberOfThreads * 4 / 3))
                .build();
        doSetPythonExecutorPool();
    }

    private void doSetPythonExecutorPool() {
        final boolean useExternalPython = getPythonStrategy(System.getProperty("python.expressionsEval"), PYTHON_EXECUTOR) != JYTHON;
        // 25% of number of thread, in case of external python expression evaluation
        // 75% of number of threads in case of jython expression evaluation
        int defaultPoolSize = useExternalPython ? max(2, numberOfThreads / 4) : max(2, numberOfThreads * 3 / 4);
        int maxPoolSize = getInteger("jython.executor.maxPoolSize", defaultPoolSize);
        if (maxPoolSize > 100) { // 100 = hard limit for EmbeddedPythonExecutorWrapper pools
            maxPoolSize = 100;
        }
        ExecutorService executorService = newFixedThreadPool(max(2, getRuntime().availableProcessors()));
        ConcurrentLinkedQueueCollection<EmbeddedPythonExecutorWrapper> collection =
                new ConcurrentLinkedQueueCollection<>();
        for (int i = 0; i < maxPoolSize; i++) {
            executorService.submit(() -> {
                try {
                    collection.offerLast(new EmbeddedPythonExecutorWrapper());
                } catch (Exception ignored) {
                }
            });
        }
        try {
            executorService.shutdown();
            //noinspection ResultOfMethodCallIgnored
            executorService.awaitTermination(7, TimeUnit.MINUTES);
            executorService.shutdownNow();
        } catch (Exception ignored) {
        }
        while (collection.size() < maxPoolSize) {
            collection.offerLast(new EmbeddedPythonExecutorWrapper());
        }
        this.pythonExecutorPool = new ViburEmbeddedPythonPoolServiceImpl(collection, maxPoolSize, maxPoolSize);
    }

    @Override
    public PythonExecutionResult exec(Set<String> dependencies, String script, Map<String, Serializable> vars) {
        Set<String> resultedDependencies = dependencyService.getDependencies(dependencies);
        // When dependencies are not set we take an EmbeddedPythonExecutorWrapper from the 'pythonExecutorPool' pool
        // When dependencies are set we take an EmbeddedPythonExecutorWrapper from the 'cachedExecutors' cache
        if (resultedDependencies.isEmpty()) {
            EmbeddedPythonExecutorWrapper pooledPythonExecutor = null;
            try {
                pooledPythonExecutor = pythonExecutorPool.tryTakeWithTimeout();
                return pooledPythonExecutor.exec(script, vars);
            } finally {
                doRestoreExecutor(pooledPythonExecutor);
            }
        } else {
            String dependenciesKey = generatedDependenciesKey(dependencies);
            EmbeddedPythonExecutorWrapper executor = cachedExecutors.getIfPresent(dependenciesKey);
            if (executor == null) {
                EmbeddedPythonExecutorWrapper newPythonExecutor = new EmbeddedPythonExecutorWrapper(resultedDependencies);
                executor = cachedExecutors.get(dependenciesKey, k -> newPythonExecutor);
            }
            // At this point executor cannot be null, since it was created in in the function  k -> newPythonExecutor
            // That is the reason we don't want to call an extra Objects.requireNotNull(executor)
            return executor.exec(script, vars);
        }
    }

    @Override
    public PythonEvaluationResult eval(String prepareEnvironmentScript, String script, Map<String, Serializable> vars) {
        EmbeddedPythonExecutorWrapper embeddedPythonExecutor = null;
        try {
            embeddedPythonExecutor = pythonExecutorPool.tryTakeWithTimeout();
            return embeddedPythonExecutor.eval(prepareEnvironmentScript, script, vars);
        } finally {
            doRestoreExecutor(embeddedPythonExecutor);
        }
    }

    @Override
    public PythonEvaluationResult test(String prepareEnvironmentScript, String script,
                                       Map<String, Serializable> vars, long timeout) {
        EmbeddedPythonExecutorWrapper embeddedPythonExecutor = null;
        try {
            embeddedPythonExecutor = pythonExecutorPool.tryTakeWithTimeout();
            // For Jython test is identical with eval
            return embeddedPythonExecutor.eval(prepareEnvironmentScript, script, vars);
        } finally {
            doRestoreExecutor(embeddedPythonExecutor);
        }
    }

    private void doRestoreExecutor(EmbeddedPythonExecutorWrapper pooledPythonExecutor) {
        try {
            pythonExecutorPool.restore(pooledPythonExecutor);
        } catch (Exception ignored) {
        }
    }

    @PreDestroy
    public void closeResources() {
        pythonExecutorPool.close();
        try {
            cachedExecutors.asMap().values().forEach(EmbeddedPythonExecutorWrapper::close);
        } catch (Exception ignored) {
        }
    }

}
