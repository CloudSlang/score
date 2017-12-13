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

import java.io.Serializable;
import java.util.Map;

/**
 * Python execution result
 *
 * Created by Ifat Gavish on 25/05/2016
 */
public class PythonEvaluationResult {

    private Serializable evalResult;
    private Map<String, Serializable> resultContext;

    public PythonEvaluationResult(Serializable evalResult, Map<String, Serializable> resultContext) {
        this.evalResult = evalResult;
        this.resultContext = resultContext;
    }

    public Serializable getEvalResult() {
        return evalResult;
    }

    public void setEvalResult(Serializable evalResult) {
        this.evalResult = evalResult;
    }

    public Map<String, Serializable> getResultContext() {
        return resultContext;
    }

    public void setResultContext(Map<String, Serializable> resultContext) {
        this.resultContext = resultContext;
    }
}
