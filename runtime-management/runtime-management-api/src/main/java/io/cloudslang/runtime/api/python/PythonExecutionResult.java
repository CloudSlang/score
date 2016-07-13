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
public class PythonExecutionResult {

    private Map<String, Serializable> executionResult;

    public PythonExecutionResult(Map<String, Serializable> executionResult) {
        this.executionResult = executionResult;
    }

    public Map<String, Serializable> getExecutionResult() {
        return executionResult;
    }

    public void setExecutionResult(Map<String, Serializable> executionResult) {
        this.executionResult = executionResult;
    }
}
