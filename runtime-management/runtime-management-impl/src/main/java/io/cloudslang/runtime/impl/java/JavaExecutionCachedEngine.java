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

import io.cloudslang.dependency.api.services.DependencyService;
import io.cloudslang.runtime.api.java.JavaExecutionParametersProvider;
import io.cloudslang.runtime.impl.ExecutionCachedEngine;
import org.python.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.Set;

/**
 * Created by Genadi Rabinovich, genadi@hpe.com on 05/05/2016.
 */
public class JavaExecutionCachedEngine extends ExecutionCachedEngine<JavaExecutor> implements JavaExecutionEngine {
    @Autowired
    private DependencyService dependencyService;

    @Value("#{systemProperties['" + JavaExecutionConfigurationConsts.JAVA_EXECUTOR_CACHE_SIZE + "'] != null ? systemProperties['" + JavaExecutionConfigurationConsts.JAVA_EXECUTOR_CACHE_SIZE + "'] : " + JavaExecutionConfigurationConsts.JAVA_EXECUTOR_CACHE_DEFAULT_SIZE + "}")
    private int cacheSize;

    @Override
    public Object execute(String dependency, String className, String methodName, JavaExecutionParametersProvider parametersProvider) {
        JavaExecutor executor = allocateExecutor((dependency == null || dependency.isEmpty()) ? Sets.<String>newHashSet() :
                Sets.newHashSet(dependency));
        try {
            return executor.execute(className, methodName, parametersProvider);
        } finally {
            releaseExecutor(executor);
        }
    }

    @Override
    protected DependencyService getDependencyService() {
        return dependencyService;
    }

    @Override
    protected int getCacheSize() {
        return cacheSize;
    }

    @Override
    protected JavaExecutor createNewExecutor(Set<String> filePaths) {
        return new JavaExecutor(filePaths);
    }
}
