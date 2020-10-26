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

import io.cloudslang.dependency.api.services.DependencyService;
import io.cloudslang.runtime.api.python.PythonEvaluationResult;
import io.cloudslang.runtime.api.python.PythonExecutionResult;
import io.cloudslang.runtime.impl.ExecutionCachedEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptySet;

/**
 * Created by Genadi Rabinovich, genadi@hpe.com on 05/05/2016.
 */
public class PythonExecutionCachedEngine extends ExecutionCachedEngine<PythonExecutor> implements PythonExecutionEngine {
    @Autowired
    private DependencyService dependencyService;

    @Value("#{systemProperties['" + PythonExecutionConfigurationConsts.PYTHON_EXECUTOR_CACHE_SIZE + "'] != null ? systemProperties['" + PythonExecutionConfigurationConsts.PYTHON_EXECUTOR_CACHE_SIZE + "'] : " + PythonExecutionConfigurationConsts.PYTHON_EXECUTOR_CACHE_DEFAULT_SIZE + "}")
    private int cacheSize;

    @Override
    public PythonExecutionResult exec(Set<String> dependencies, String script, Map<String, Serializable> vars) {
        PythonExecutor executor = allocateExecutor(dependencies);
        try {
            return executor.exec(script, vars);
        } finally {
            releaseExecutor(executor);
        }
    }

    @Override
    public PythonEvaluationResult eval(String prepareEnvironmentScript, String script, Map<String, Serializable> vars) {
        PythonExecutor executor = allocateExecutor(emptySet());
        try {
            return executor.eval(prepareEnvironmentScript, script, vars);
        } finally {
            releaseExecutor(executor);
        }
    }

    @Override
    public PythonEvaluationResult test(String prepareEnvironmentScript, String script, Map<String, Serializable> vars, long timeout) {
        PythonExecutor executor = allocateExecutor(emptySet());
        try {
            return executor.eval(prepareEnvironmentScript, script, vars);
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
    protected PythonExecutor createNewExecutor(Set<String> filePaths) {
        return new PythonExecutor(filePaths);
    }
}
