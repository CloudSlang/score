/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
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
