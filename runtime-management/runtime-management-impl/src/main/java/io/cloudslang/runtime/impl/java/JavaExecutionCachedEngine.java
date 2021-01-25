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

package io.cloudslang.runtime.impl.java;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.annotations.VisibleForTesting;
import io.cloudslang.dependency.api.services.DependencyService;
import io.cloudslang.runtime.api.java.JavaExecutionParametersProvider;
import io.cloudslang.runtime.impl.ExecutionEngine;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;

public class JavaExecutionCachedEngine extends ExecutionEngine implements JavaExecutionEngine {
    private static final int CACHE_SIZE = Integer.getInteger("java.executor.cache.size", 200);

    @Autowired
    private DependencyService dependencyService;

    private final Cache<String, JavaExecutor> executorCache;

    public JavaExecutionCachedEngine() {
        this.executorCache = Caffeine.newBuilder()
                .maximumSize(CACHE_SIZE)
                .build();
    }

    @Override
    public Object execute(String dependency, String className, String methodName, JavaExecutionParametersProvider parametersProvider) {
        Set<String> dependencies = (dependency == null || dependency.isEmpty()) ? emptySet() : singleton(dependency);
        String dependenciesKey = generatedDependenciesKey(dependencies);
        JavaExecutor executor = getExecutorFromCache(dependenciesKey);
        if (executor == null) {
            final JavaExecutor createdExecutor = createNewExecutor(dependencies);
            executor = executorCache.get(dependenciesKey, k -> createdExecutor);
        }
        //noinspection ConstantConditions
        return executor.execute(className, methodName, parametersProvider);
    }

    @VisibleForTesting
    JavaExecutor createNewExecutor(Set<String> dependencies) {
        return new JavaExecutor(dependencyService.getDependencies(dependencies));
    }

    @VisibleForTesting
    JavaExecutor getExecutorFromCache(String dependenciesKey) {
        return executorCache.getIfPresent(dependenciesKey);
    }

}
