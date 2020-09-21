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

package io.cloudslang.runtime.impl.python;

import io.cloudslang.runtime.api.python.PythonEvaluationResult;
import io.cloudslang.runtime.api.python.PythonExecutionResult;
import io.cloudslang.runtime.impl.Executor;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.python.core.Py;
import org.python.core.PyBoolean;
import org.python.core.PyClass;
import org.python.core.PyException;
import org.python.core.PyFile;
import org.python.core.PyFunction;
import org.python.core.PyModule;
import org.python.core.PyObject;
import org.python.core.PyReflectedFunction;
import org.python.core.PyString;
import org.python.core.PyStringMap;
import org.python.core.PySystemState;
import org.python.core.PyType;
import org.python.util.PythonInterpreter;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Genadi Rabinovich, genadi@hpe.com on 05/05/2016.
 */
public class PythonExecutor implements Executor {
    public static final String THREADED_MODULES_ISSUE = "No module named";
    private static final Logger logger = Logger.getLogger(PythonExecutor.class);

    private static final String TRUE = "true";
    private static final String FALSE = "false";

    private static final PythonInterpreter GLOBAL_INTERPRETER = new ThreadSafePythonInterpreter(null);

    /**
     * There is an issue in loaded environment - existing python module not found in PySystem.modules.table
     * although it exists in the table.
     * Meanwhile we execute retries ans will open an issue to the jython.org
     */
    public static final int RETRIES_NUMBER_ON_THREADED_ISSUE = 3;
    public static final int MAX_LENGTH = Integer.getInteger("input.error.max.length", 1000);

    static {
        //here to avoid jython preferring io.cloudslang package over python io package
        GLOBAL_INTERPRETER.exec("import io");
    }

    private final PythonInterpreter interpreter;

    private final Lock allocationLock = new ReentrantLock();
    private int allocations = 0;
    //Executor marked to be actuallyClosed. Executor may be still in use thus we don't close it immediately
    private boolean markedClosed = false;
    //Executor was finally actuallyClosed
    private boolean actuallyClosed = false;

    private final Set<String> dependencies;

    public PythonExecutor() {
        this(Collections.<String>emptySet());
    }

    public PythonExecutor(Set<String> dependencies) {
        this.dependencies = dependencies;
        interpreter = initInterpreter(dependencies);
    }

    protected PythonInterpreter initInterpreter(Set<String> dependencies) {
        logger.info("Creating python interpreter with [" + dependencies.size() + "] dependencies [" + dependencies + "]");
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
        checkValidInterpreter();
        initInterpreter();
        prepareInterpreterContext(callArguments);

        Exception originException = null;
        for(int i = 0; i < RETRIES_NUMBER_ON_THREADED_ISSUE; i++) {
            try {
                return exec(script);
            } catch (Exception e) {
                if(!isThreadsRelatedModuleIssue(e)) {
                    throw new RuntimeException("Error executing python script: " + e, e);
                }
                if(originException == null) {
                    originException = e;
                }
            }
        }
        throw new RuntimeException("Error executing python script: " + originException, originException);
    }

    private boolean isThreadsRelatedModuleIssue(Exception e) {
        if (e instanceof PyException) {
            PyException pyException = (PyException) e;
            String message = pyException.value.toString();
            return message.contains(THREADED_MODULES_ISSUE);
        }
        return false;
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
        checkValidInterpreter();
        try {
            initInterpreter();
            prepareInterpreterContext(context);

            return new PythonEvaluationResult(eval(prepareEnvironmentScript, expr), getPythonLocals());
        } catch (PyException exception) {
            throw new RuntimeException("Error in running script expression: '" +
                    getTruncatedExpression(expr) + "',\n\tException is: " +
                    handleExceptionSpecialCases(exception.value.toString()), exception);
        } catch (Exception exception) {
            throw new RuntimeException("Error in running script expression: '" +
                    getTruncatedExpression(expr) + "',\n\tException is: " +
                    handleExceptionSpecialCases(exception.getMessage()), exception);
        }
    }

    private String getTruncatedExpression(String expr) {
        return expr.length() > MAX_LENGTH ? expr.substring(0, MAX_LENGTH) + "..." : expr;
    }

    private String handleExceptionSpecialCases(String message) {
        String processedMessage = message;
        if (StringUtils.isNotEmpty(message) && message.contains("get_sp") && message.contains("not defined")) {
            processedMessage =  message + ". Make sure to use correct syntax for the function: get_sp('fully.qualified.name', optional_default_value).";
        }
        return processedMessage;
    }

    private void checkValidInterpreter() {
        if(isClosed()) {
            throw new RuntimeException("Trying to execute script on already closed python interpreter");
        }
    }

    protected Serializable eval(String prepareEnvironmentScript, String script) {
        if (interpreter.get(TRUE) == null) {
            interpreter.set(TRUE, Boolean.TRUE);
        }
        if (interpreter.get(FALSE) == null) {
            interpreter.set(FALSE, Boolean.FALSE);
        }

        if(prepareEnvironmentScript != null && !prepareEnvironmentScript.isEmpty()) {
            interpreter.exec(prepareEnvironmentScript);
        }
        PyObject evalResultAsPyObject = interpreter.eval(script);
        Serializable evalResult;
        evalResult = resolveJythonObjectToJavaEval(evalResultAsPyObject, script);
        return evalResult;
    }

    @Override
    public void allocate() {
        allocationLock.lock();
        try {
            allocations++;
        } finally {
            allocationLock.unlock();
        }
    }

    @Override
    public void release() {
        allocationLock.lock();
        try {
            allocations--;
            if(markedClosed && (allocations == 0)) {
                close();
            }
        } finally {
            allocationLock.unlock();
        }
    }

    @Override
    public void close() {
        allocationLock.lock();
        try {
            markedClosed = true;
            if ((interpreter != GLOBAL_INTERPRETER) && (allocations == 0)) {
                logger.info("Removing LRU python executor for dependencies [" + dependencies + "]");
                try {interpreter.close();} catch (Throwable e) {}
                actuallyClosed = true;
            }
        } finally {
            allocationLock.unlock();
        }
    }

    public boolean isClosed() {
        return actuallyClosed;
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
                        "\tConversion failed for '" + key + "' (" + value + "),\n" +
                        "\tThe error can be solved by removing the variable from the context in the script: e.g. 'del " + key + "'.\n";
        return resolveJythonObjectToJava(value, errorMessage);
    }

    private Serializable resolveJythonObjectToJavaEval(PyObject value, String expression) {
        String errorMessage =
                "Evaluation result for a Python expression should be serializable:\n" +
                        "\tConversion failed for '" + expression + "' (" + value + ").\n";
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
                value instanceof PySystemState ||
                value instanceof PyClass ||
                value instanceof PyType ||
                value instanceof PyReflectedFunction;
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
