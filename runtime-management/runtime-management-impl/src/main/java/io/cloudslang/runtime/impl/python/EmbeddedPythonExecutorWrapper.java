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
import io.cloudslang.runtime.impl.python.security.BoundedStringWriter;
import org.apache.commons.io.input.NullInputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static org.apache.commons.io.output.NullOutputStream.NULL_OUTPUT_STREAM;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

public class EmbeddedPythonExecutorWrapper {
    private static final Logger logger = LogManager.getLogger(PythonExecutor.class);
    private static final int retriesForNoModuleFound = 3;
    private static final int exceptionMaxLength = Integer.getInteger("input.error.max.length", 1000);
    private static final Supplier<RuntimeException> outputStreamLengthExceededSupplier =
            () -> new IllegalStateException("Cannot exceed threshold for python standard output stream.");
    private static final Supplier<RuntimeException> errorStreamLengthExceededSupplier =
            () -> new IllegalStateException("Cannot exceed threshold for python standard error stream.");
    private static final String noModuleNamedIssue = "No module named";

    private final PythonInterpreter pythonInterpreter;
    private final AtomicBoolean closed;

    public EmbeddedPythonExecutorWrapper() {
        this(Collections.emptySet());
    }

    public EmbeddedPythonExecutorWrapper(Set<String> dependencies) {
        this.pythonInterpreter = new PythonInterpreter(null, getPySystemState(dependencies));
        this.closed = new AtomicBoolean(false);
        initialize();
    }

    /**
     * Called one time only in the lifecycle of an {@link EmbeddedPythonExecutorWrapper}
     */
    private void initialize() {
        try {
            pythonInterpreter.exec("import io");
        } catch (Exception initException) {
            logger.error("Could not initialize python interpreter: ", initException);
        }
    }

    /**
     * Contract method: Called for compiling and executing Python scripts
     */
    public PythonExecutionResult exec(String script, Map<String, Serializable> callArguments) {
        validateInterpreter();
        Writer outputWriter = new BoundedStringWriter(outputStreamLengthExceededSupplier);
        Writer errorWriter = new BoundedStringWriter(errorStreamLengthExceededSupplier);
        try {
            pythonInterpreter.setOut(outputWriter);
            pythonInterpreter.setErr(errorWriter);
            pythonInterpreter.setIn(new NullInputStream(0));
            prepareInterpreterContext(callArguments);
            Exception originalExc = null;
            for (int i = 0; i < retriesForNoModuleFound; i++) {
                try {
                    return doExec(script);
                } catch (Exception exc) {
                    if (!isNoModuleFoundIssue(exc)) {
                        throw new RuntimeException("Error executing python script: " + exc, exc);
                    }
                    if (originalExc == null) {
                        originalExc = exc;
                    }
                }
            }
            throw new RuntimeException("Error executing python script: " + originalExc, originalExc);
        } finally {
            String standardStreamOutput = outputWriter.toString();
            if (isNotEmpty(standardStreamOutput)) {
                logger.info("Script output: " + standardStreamOutput);
            }
            String standardStreamError = errorWriter.toString();
            if (isNotEmpty(standardStreamError)) {
                logger.error("Script error: " + standardStreamError);
            }
        }
    }

    /**
     * Contract method: Called for compiling and evaluating a Python expression
     */
    public PythonEvaluationResult eval(String prepareEnvironmentScript, String expr, Map<String, Serializable> context) {
        validateInterpreter();
        try {
            pythonInterpreter.setOut(NULL_OUTPUT_STREAM);
            pythonInterpreter.setErr(NULL_OUTPUT_STREAM);
            pythonInterpreter.setIn(new NullInputStream(0));
            prepareInterpreterContext(context);
            Serializable eval = doEval(prepareEnvironmentScript, expr);
            return new PythonEvaluationResult(eval, getPythonLocals());
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

    /**
     * Contract method: Called for closing a Python executor
     */
    public void close() {
        if (closed.compareAndSet(false, true)) {
            try {
                pythonInterpreter.close();
            } catch (Exception ignore) {
            }
        }
    }

    private int getMapCapacity(int expectedSize) {
        return (expectedSize < 3) ? (expectedSize + 1) :
                ((expectedSize < 1073741824) ? (expectedSize + (expectedSize / 3)) : 2147483647);
    }

    private void prepareInterpreterContext(Map<String, Serializable> context) {
        // Set a new locals map with values taken from context
        pythonInterpreter.setLocals(new PyStringMap(getMapCapacity(context.size())));
        for (Map.Entry<String, Serializable> entry : context.entrySet()) {
            pythonInterpreter.set(entry.getKey(), entry.getValue());
        }
    }

    private PythonExecutionResult doExec(String script) {
        pythonInterpreter.exec(script);
        return processExecResults();
    }

    private boolean isNoModuleFoundIssue(Exception e) {
        if (e instanceof PyException) {
            PyException pyException = (PyException) e;
            String message = pyException.value.toString();
            return message.contains(noModuleNamedIssue);
        }
        return false;
    }

    private PythonExecutionResult processExecResults() {
        Iterator<PyObject> localsIterator = pythonInterpreter.getLocals().asIterable().iterator();
        Map<String, Serializable> returnValue = new HashMap<>();
        while (localsIterator.hasNext()) {
            PyObject next = localsIterator.next();
            String key = next.asString();
            PyObject value = pythonInterpreter.get(key);
            if (!isLocalEntryExcluded(key, value)) {
                returnValue.put(key, resolveJythonObjectToJavaForExec(value, key));
            }
        }
        return new PythonExecutionResult(returnValue);
    }

    private Serializable resolveJythonObjectToJavaForExec(PyObject value, String key) {
        String errorMessage =
                "Non-serializable values are not allowed in the output context of a Python script:\n" +
                        "\tConversion failed for '" + key + "' (" + value + "),\n" +
                        "\tThe error can be solved by removing the variable from the context in the script: e.g. 'del " + key + "'.\n";
        return resolveJythonObjectToJava(value, errorMessage);
    }

    private Serializable resolveJythonObjectToJavaForEval(PyObject value, String expression) {
        String errorMessage =
                "Evaluation result for a Python expression should be serializable:\n" +
                        "\tConversion failed for '" + expression + "' (" + value + ").\n";
        return resolveJythonObjectToJava(value, errorMessage);
    }

    private Serializable resolveJythonObjectToJava(PyObject value, String errorMessage) {
        if (value == null) {
            return null;
        } else if (value instanceof PyBoolean) {
            PyBoolean pyBoolean = (PyBoolean) value;
            return pyBoolean.getBooleanValue();
        } else {
            try {
                return Py.tojava(value, Serializable.class);
            } catch (PyException pyException) {
                PyObject typeObject = pyException.type;
                if (typeObject instanceof PyType) {
                    PyType type = (PyType) typeObject;
                    String typeName = type.getName();
                    if ("TypeError".equals(typeName)) {
                        throw new RuntimeException(errorMessage, pyException);
                    }
                }
                throw pyException;
            }
        }
    }

    private boolean isLocalEntryExcluded(String key, PyObject value) {
        return (key.startsWith("__") && key.endsWith("__")) ||
                value instanceof PyFile ||
                value instanceof PyModule ||
                value instanceof PyFunction ||
                value instanceof PySystemState ||
                value instanceof PyClass ||
                value instanceof PyType ||
                value instanceof PyReflectedFunction;
    }

    private Map<String, Serializable> getPythonLocals() {
        Map<String, Serializable> retVal = new HashMap<>();
        for (PyObject pyObject : pythonInterpreter.getLocals().asIterable()) {
            String key = pyObject.asString();
            PyObject value = pythonInterpreter.get(key);
            if (!isLocalEntryExcluded(key, value)) {
                retVal.put(key, value);
            }
        }
        return retVal;
    }

    private String getTruncatedExpression(String expr) {
        return expr.length() > exceptionMaxLength ? expr.substring(0, exceptionMaxLength) + "..." : expr;
    }

    private String handleExceptionSpecialCases(String message) {
        String processedMessage = message;
        if (isNotEmpty(message) && message.contains("get_sp") && message.contains("not defined")) {
            processedMessage = message + ". Make sure to use correct syntax for the function: " +
                    "get_sp('fully.qualified.name', optional_default_value).";
        }
        return processedMessage;
    }

    private void validateInterpreter() {
        if (closed.get()) {
            throw new RuntimeException("Trying to execute Python code on an already closed interpreter");
        }
    }

    private Serializable doEval(String prepareEnvironmentScript, String script) {
        // Set boolean values
        pythonInterpreter.set("true", Boolean.TRUE);
        pythonInterpreter.set("false", Boolean.FALSE);
        // Prepare environment if required
        if (org.apache.commons.lang3.StringUtils.isNotEmpty(prepareEnvironmentScript)) {
            pythonInterpreter.exec(prepareEnvironmentScript);
        }
        PyObject evalResultPyObject = pythonInterpreter.eval(script);
        return resolveJythonObjectToJavaForEval(evalResultPyObject, script);
    }

    private PySystemState getPySystemState(Set<String> dependencies) {
        PySystemState pySystemState = new PySystemState();
        if (!dependencies.isEmpty()) {
            for (String dependency : dependencies) {
                pySystemState.path.append(new PyString(dependency));
            }
        }
        return pySystemState;
    }

}
