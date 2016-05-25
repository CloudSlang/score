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

import io.cloudslang.runtime.api.python.PythonEvaluationResult;
import io.cloudslang.runtime.api.python.PythonExecutionResult;
import io.cloudslang.runtime.impl.Executor;
import org.apache.commons.lang.SerializationUtils;
import org.python.core.Py;
import org.python.core.PyArray;
import org.python.core.PyBoolean;
import org.python.core.PyDictionary;
import org.python.core.PyException;
import org.python.core.PyFile;
import org.python.core.PyFunction;
import org.python.core.PyList;
import org.python.core.PyModule;
import org.python.core.PyObject;
import org.python.core.PySet;
import org.python.core.PyString;
import org.python.core.PyStringMap;
import org.python.core.PySystemState;
import org.python.core.PyType;
import org.python.util.PythonInterpreter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Genadi Rabinovich, genadi@hpe.com on 05/05/2016.
 */
public class PythonExecutor implements Executor {
    private static final String TRUE = "true";
    private static final String FALSE = "false";

    private static final PythonInterpreter GLOBAL_INTERPRETER = new ThreadSafePythonInterpreter(null);

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
            return new ThreadSafePythonInterpreter(systemState);
        }
        return GLOBAL_INTERPRETER;
    }

    //we need this method to be synchronized so we will not have multiple scripts run in parallel on the same context
    public PythonExecutionResult exec(String script, Map<String, Serializable> callArguments) {
        try {
            initInterpreter();
            prepareInterpreterContext(callArguments);
            return exec(script);
        } catch (Exception e) {
            throw new RuntimeException("Error executing python script: " + e.getMessage(), e);
        }
    }

    private PythonExecutionResult exec(String script) {
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
        return new PythonExecutionResult(returnValue);
    }

    private Map<String, Serializable> getPythonLocals() {
        Map<String, Serializable> result = new HashMap<>();
        if(interpreter.getLocals() != null) {
            for (PyObject pyObject : interpreter.getLocals().asIterable()) {
                String key = pyObject.asString();
                PyObject value = interpreter.get(key);
                if (keyIsExcluded(key, value)) {
                    continue;
                }
                result.put(key, value);
            }
        }
        return result;
    }

    public PythonEvaluationResult eval(String prepareEnvironmentScript, String expr, Map<String, Serializable> context) {
        try {
            initInterpreter();
            prepareInterpreterContext(context);

            return new PythonEvaluationResult(eval(prepareEnvironmentScript, expr), getPythonLocals());
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

    protected Serializable eval(String prepareEnvironmentScript, String script) {
        if (interpreter.get(TRUE) == null)
            interpreter.set(TRUE, Boolean.TRUE);
        if (interpreter.get(FALSE) == null)
            interpreter.set(FALSE, Boolean.FALSE);

        if(prepareEnvironmentScript != null && !prepareEnvironmentScript.isEmpty()) {
            interpreter.exec(prepareEnvironmentScript);
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
        try {
            value.getType(); // sets the accessed flag to true
            return (Serializable)toJava(value);
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

    private Object toJava(PyObject value) {
        if (value instanceof PyBoolean) {
            return ((PyBoolean) value).getBooleanValue();
        }
        if (value instanceof PyList) {
            return new ArrayList<>((List<?>)value);
        }
        if (value instanceof PyDictionary) {
            return new ConcurrentHashMap<>((Map<?, ?>)value);
        }
        if (value instanceof PySet) {
            return new HashSet<>((Set<?>)value);
        }
        if (value instanceof PyArray) {
            return SerializationUtils.clone((Serializable)((PyArray) value).getArray());
        }
        if (value instanceof PyType) {
            return ((PyType) value).getName();
        }
        return Py.tojava(value, Serializable.class);
    }

    private boolean keyIsExcluded(String key, PyObject value) {
        return (key.startsWith("__") && key.endsWith("__")) ||
                value instanceof PyFile ||
                value instanceof PyModule ||
                value instanceof PyFunction ||
                value instanceof PySystemState;
    }

    private static class ThreadSafePythonInterpreter extends PythonInterpreter {
        ThreadSafePythonInterpreter() {
            this(null);
        }

        ThreadSafePythonInterpreter(PySystemState systemState) {
            super(null, systemState, true);
        }
    }
}
