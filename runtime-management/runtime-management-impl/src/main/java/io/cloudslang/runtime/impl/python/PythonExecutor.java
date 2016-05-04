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

import io.cloudslang.runtime.impl.Executor;
import org.python.core.*;
import org.python.util.PythonInterpreter;

import java.io.Serializable;
import java.util.*;

public class PythonExecutor implements Executor {
    private static final String TRUE = "true";
    private static final String FALSE = "false";

    private static final PythonInterpreter GLOBAL_INTERPRETER = new PythonInterpreter();

    static {
        //here to avoid jython preferring io.cloudslang package over python io package
        GLOBAL_INTERPRETER.exec("import io");
    }

    private final PythonInterpreter interpreter;

    public PythonExecutor() {
        this(Collections.<String>emptySet());
    }

    public PythonExecutor(Set<String> dependencies) {
        interpreter = initInterpreter(dependencies);
    }

    protected PythonInterpreter initInterpreter(Set<String> dependencies) {
        if(!dependencies.isEmpty()) {
            PySystemState systemState = new PySystemState();
            for (String dependency: dependencies) {
                systemState.path.append(new PyString(dependency));
            }
            return new PythonInterpreter(null, systemState);
        }
        return GLOBAL_INTERPRETER;
    }

    //we need this method to be synchronized so we will not have multiple scripts run in parallel on the same context
    public Map<String, Serializable> exec(String script, Map<String, Serializable> callArguments) {
        try {
            initInterpreter();
            prepareInterpreterContext(callArguments);
            return exec(script);
        } catch (Exception e) {
            throw new RuntimeException("Error executing python script: " + e.getMessage(), e);
        }
    }

    private Map<String, Serializable> exec(String script) {
        interpreter.exec(script);
        Iterator<PyObject> localsIterator = interpreter.getLocals().asIterable().iterator();
        Map<String, Serializable> returnValue = new HashMap<>();
        while (localsIterator.hasNext()) {
            String key = localsIterator.next().asString();
            PyObject value = interpreter.get(key);
            if (keyIsExcluded(key, value)) {
                continue;
            }
            Serializable javaValue = resolveJythonObjectToJavaExec(value, key);
            returnValue.put(key, javaValue);
        }
        return returnValue;
    }

    public Serializable eval(String prepareEnvironmentScript, String expr, Map<String, Serializable> context) {
        try {
            initInterpreter();
            prepareInterpreterContext(context);

            return eval(prepareEnvironmentScript, expr);
        } catch (Exception exception) {
            String message;
            if (exception instanceof PyException) {
                PyException pyException = (PyException) exception;
                message = pyException.value.toString();
            } else {
                message = exception.getMessage();
            }
            throw new RuntimeException(message, exception);
        }
    }

    private Serializable eval(String prepareEnvironmentScript, String script) {
        if (interpreter.get(TRUE) == null)
            interpreter.set(TRUE, Boolean.TRUE);
        if (interpreter.get(FALSE) == null)
            interpreter.set(FALSE, Boolean.FALSE);

        if(prepareEnvironmentScript != null && !prepareEnvironmentScript.isEmpty()) {
            exec(prepareEnvironmentScript);
        }
        PyObject evalResultAsPyObject = interpreter.eval(script);
        Serializable evalResult;
        evalResult = resolveJythonObjectToJavaEval(evalResultAsPyObject, script);
        return evalResult;
    }

    @Override
    public void release() {
        if(interpreter != GLOBAL_INTERPRETER) {
            try {interpreter.getSystemState().close();} catch (Throwable e) {e.printStackTrace();}
            try {interpreter.cleanup();} catch (Throwable e) {e.printStackTrace();}
            try {interpreter.close();} catch (Throwable e) {e.printStackTrace();}
        }
    }

    private void initInterpreter() {
        interpreter.setLocals(new PyStringMap());
    }

    private void prepareInterpreterContext(Map<String, Serializable> context) {
        for (Map.Entry<String, Serializable> entry : context.entrySet()) {
            interpreter.set(entry.getKey(), entry.getValue());
        }
    }

    private Serializable resolveJythonObjectToJavaExec(PyObject value, String key) {
        String errorMessage =
                "Non-serializable values are not allowed in the output context of a Python script:\n" +
                        "\tConversion failed for '" + key + "' (" + String.valueOf(value) + "),\n" +
                        "\tThe error can be solved by removing the variable from the context in the script: e.g. 'del " + key + "'.\n";
        return resolveJythonObjectToJava(value, errorMessage);
    }

    private Serializable resolveJythonObjectToJavaEval(PyObject value, String expression) {
        String errorMessage =
                "Evaluation result for a Python expression should be serializable:\n" +
                        "\tConversion failed for '" + expression + "' (" + String.valueOf(value) + ").\n";
        return resolveJythonObjectToJava(value, errorMessage);
    }

    private Serializable resolveJythonObjectToJava(PyObject value, String errorMessage) {
        if (value == null) {
            return null;
        }
        if (value instanceof PyBoolean) {
            PyBoolean pyBoolean = (PyBoolean) value;
            return pyBoolean.getBooleanValue();
        }
        try {
            return Py.tojava(value, Serializable.class);
        } catch (PyException e) {
            PyObject typeObject = e.type;
            if (typeObject instanceof PyType) {
                PyType type = (PyType) typeObject;
                String typeName = type.getName();
                if ("TypeError".equals(typeName)) {
                    throw new RuntimeException(errorMessage, e);
                }
            }
            throw e;
        }
    }

    private boolean keyIsExcluded(String key, PyObject value) {
        return (key.startsWith("__") && key.endsWith("__")) ||
                value instanceof PyFile ||
                value instanceof PyModule ||
                value instanceof PyFunction ||
                value instanceof PySystemState;
    }
}
