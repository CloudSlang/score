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

import io.cloudslang.dependency.api.services.DependencyService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class PythonCachedStaticsNotSharedExecutionEngine implements PythonExecutionEngine{
    @Autowired
    private DependencyService dependencyService;

    @Override
    public Map<String, Serializable> exec(Set<String> dependencies, String script, Map<String, Serializable> vars) {
        PythonExecutor pythonExecutor = new PythonExecutor(dependencyService.getDependencies(dependencies));
        try {
            return pythonExecutor.exec(script, vars);
        } finally {
            pythonExecutor.release();
        }
    }

    @Override
    public Serializable eval(String prepareEnvironmentScript, String script, Map<String, Serializable> vars) {
        PythonExecutor pythonExecutor = new PythonExecutor(Collections.<String>emptySet());
        try {
            return pythonExecutor.eval(prepareEnvironmentScript, script, vars);
        } finally {
            pythonExecutor.release();
        }
    }
}
