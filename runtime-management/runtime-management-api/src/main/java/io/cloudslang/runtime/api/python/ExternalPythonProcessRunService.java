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
package io.cloudslang.runtime.api.python;


import com.fasterxml.jackson.core.StreamReadConstraints;

import java.io.Serializable;
import java.util.Map;

public interface ExternalPythonProcessRunService {

    int DEFAULT_MAX_DEPTH = Integer.getInteger("jackson.core.maxNestingDepth", StreamReadConstraints.DEFAULT_MAX_DEPTH);
    int DEFAULT_MAX_NUM_LEN = Integer.getInteger("jackson.core.maxNumLen", StreamReadConstraints.DEFAULT_MAX_NUM_LEN);
    int DEFAULT_MAX_STRING_LEN = Integer.getInteger("jackson.core.maxStringLen", StreamReadConstraints.DEFAULT_MAX_STRING_LEN);

    PythonExecutionResult exec(String script, Map<String, Serializable> inputs);
    PythonEvaluationResult eval(String expression, String prepareEnvironmentScript, Map<String, Serializable> context);
    PythonEvaluationResult test(String expression, String prepareEnvironmentScript, Map<String, Serializable> context,
            long timeout);
    String getStrategyName();

}
