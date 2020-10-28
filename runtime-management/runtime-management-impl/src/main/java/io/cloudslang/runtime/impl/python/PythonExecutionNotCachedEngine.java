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
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptySet;

/**
 * Created by Genadi Rabinovich, genadi@hpe.com on 05/05/2016.
 */
public class PythonExecutionNotCachedEngine implements PythonExecutionEngine {
    @Autowired
    private DependencyService dependencyService;

    @Override
    public PythonExecutionResult exec(Set<String> dependencies, String script, Map<String, Serializable> vars) {
        PythonExecutor pythonExecutor = new PythonExecutor(dependencyService.getDependencies(dependencies));
        try {
            return pythonExecutor.exec(script, vars);
        } finally {
            pythonExecutor.close();
        }
    }

    @Override
    public PythonEvaluationResult eval(String prepareEnvironmentScript, String script, Map<String, Serializable> vars) {
        PythonExecutor pythonExecutor = new PythonExecutor(emptySet());
        try {
            return pythonExecutor.eval(prepareEnvironmentScript, script, vars);
        } finally {
            pythonExecutor.close();
        }
    }

    @Override
    public PythonEvaluationResult test(String prepareEnvironmentScript, String script, Map<String, Serializable> vars, long timeout) {
        PythonExecutor pythonExecutor = new PythonExecutor(emptySet());
        try {
            // For Jython test is identical with eval
            return pythonExecutor.eval(prepareEnvironmentScript, script, vars);
        } finally {
            pythonExecutor.close();
        }
    }
}
