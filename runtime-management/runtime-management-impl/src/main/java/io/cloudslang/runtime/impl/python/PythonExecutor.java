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

import org.python.util.PythonInterpreter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Map;

/**
 * @author Bonczidai Levente
 * @since 1/19/2016
 */
@Component
public class PythonExecutor extends AbstractScriptInterpreter {

    @Autowired
    @Qualifier("execInterpreter")
    private PythonInterpreter interpreter;

    //we need this method to be synchronized so we will not have multiple scripts run in parallel on the same context
    public synchronized Map<String, Serializable> executeScript(String script, Map<String, Serializable> callArguments) {
        try {
            cleanInterpreter(interpreter);
            prepareInterpreterContext(interpreter, callArguments);
            return exec(interpreter, script);
        } catch (Exception e) {
            throw new RuntimeException("Error executing python script: " + e.getMessage(), e);
        }
    }
}
