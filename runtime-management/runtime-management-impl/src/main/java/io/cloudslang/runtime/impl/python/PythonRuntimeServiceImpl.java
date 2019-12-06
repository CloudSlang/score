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

import io.cloudslang.runtime.api.python.PythonEvaluationResult;
import io.cloudslang.runtime.api.python.PythonExecutionResult;
import io.cloudslang.runtime.api.python.PythonRuntimeService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * Created by Genadi Rabinovich, genadi@hpe.com on 05/05/2016.
 */
public class PythonRuntimeServiceImpl implements PythonRuntimeService {
    @Resource(name = "jythonExecutionEngine")
    private PythonExecutionEngine pythonExecutionEngine;

    @Override
    public PythonExecutionResult exec(Set<String> dependencies, String script, Map<String, Serializable> vars) {
        return pythonExecutionEngine.exec(dependencies, script, vars);
    }

    @Override
    public PythonEvaluationResult eval(String prepareEnvironmentScript, String script, Map<String, Serializable> vars) {
        return pythonExecutionEngine.eval(prepareEnvironmentScript, script, vars);
    }
}
