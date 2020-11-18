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

import io.cloudslang.runtime.api.python.ExternalPythonProcessRunService;
import io.cloudslang.runtime.api.python.PythonEvaluationResult;
import io.cloudslang.runtime.api.python.PythonExecutionResult;
import io.cloudslang.runtime.impl.python.PythonExecutionEngine;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class ExternalPythonExecutionEngine implements PythonExecutionEngine {
    private static final Logger logger = LogManager.getLogger(ExternalPythonExecutionEngine.class);
    private static final Supplier<ExternalPythonProcessRunService> pythonRunServiceSupplier;

    static {
        String timeoutStrategy = System.getProperty("python.timeoutStrategy", "completable-future");
        if (StringUtils.equalsIgnoreCase(timeoutStrategy, "scheduled-executor")) {
            pythonRunServiceSupplier = () -> new ExternalPythonExecutorScheduledExecutorTimeout();
        } else if (StringUtils.equalsIgnoreCase(timeoutStrategy, "waitfor")) {
            pythonRunServiceSupplier = () -> new ExternalPythonExecutorWaitForTimeout();
        } else if (StringUtils.equalsIgnoreCase(timeoutStrategy, "completable-future")) {
            pythonRunServiceSupplier = () -> new ExternalPythonExecutorCompletableFutureTimeout();
        } else { // Use default
            pythonRunServiceSupplier = () -> new ExternalPythonExecutorCompletableFutureTimeout();
        }
        logger.info("python timeout strategy: " + pythonRunServiceSupplier.get().getStrategyName());
    }

    @Override
    public PythonExecutionResult exec(Set<String> dependencies, String script, Map<String, Serializable> vars) {
        ExternalPythonProcessRunService pythonExecutor = pythonRunServiceSupplier.get();
        return pythonExecutor.exec(script, vars);
    }

    @Override
    public PythonEvaluationResult eval(String prepareEnvironmentScript, String script, Map<String, Serializable> vars) {
        ExternalPythonProcessRunService pythonExecutor = pythonRunServiceSupplier.get();
        return pythonExecutor.eval(script, prepareEnvironmentScript, vars);
    }

    @Override
    public PythonEvaluationResult test(String prepareEnvironmentScript, String script, Map<String, Serializable> vars, long timeout) {
        ExternalPythonProcessRunService pythonExecutor = pythonRunServiceSupplier.get();
        return pythonExecutor.test(script, prepareEnvironmentScript, vars, timeout);

    }
}
