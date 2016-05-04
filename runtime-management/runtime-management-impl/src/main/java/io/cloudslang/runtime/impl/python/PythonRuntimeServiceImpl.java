package io.cloudslang.runtime.impl.python;

/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/

import io.cloudslang.runtime.api.python.PythonRuntimeService;
import org.python.core.PySystemState;
import org.springframework.stereotype.Component;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

@Component
public class PythonRuntimeServiceImpl implements PythonRuntimeService {
    @Autowired
    private PythonExecutor pythonExecutor;

    @Autowired
    private PythonEvaluator pythonEvaluator;

    @Override
    public synchronized Map<String, Serializable> exec(Set<String> dependencies, String script, Map<String, Serializable> vars) {
        return pythonExecutor.executeScript(script, vars);
    }

    @Override
    public synchronized Serializable eval(String prepareEnvironmentScript, String script, Map<String, Serializable> vars) {
        return pythonEvaluator.evalExpr(prepareEnvironmentScript, script, vars);
    }

    @Override
    public void setPythonEncoding(String pythonEncoding) {
        if(pythonEncoding != null && !pythonEncoding.isEmpty()) {
            System.getProperties().setProperty(PySystemState.PYTHON_IO_ENCODING, pythonEncoding);
        }
    }
}
