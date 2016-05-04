/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/

package io.cloudslang.runtime.impl.python;

import io.cloudslang.runtime.api.python.PythonRuntimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

@Component
public class PythonRuntimeServiceImpl implements PythonRuntimeService {
    @Autowired
    private PythonExecutionEngine pythonExecutionEngine;

    @Override
    public synchronized Map<String, Serializable> exec(Set<String> dependencies, String script, Map<String, Serializable> vars) {
        return pythonExecutionEngine.exec(dependencies, script, vars);
    }

    @Override
    public synchronized Serializable eval(String prepareEnvironmentScript, String script, Map<String, Serializable> vars) {
        return pythonExecutionEngine.eval(prepareEnvironmentScript, script, vars);
    }
}
