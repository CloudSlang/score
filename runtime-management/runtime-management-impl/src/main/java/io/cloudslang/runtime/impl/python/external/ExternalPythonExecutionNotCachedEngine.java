package io.cloudslang.runtime.impl.python.external;

import io.cloudslang.runtime.api.python.PythonEvaluationResult;
import io.cloudslang.runtime.api.python.PythonExecutionResult;
import io.cloudslang.runtime.impl.python.PythonExecutionEngine;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

public class ExternalPythonExecutionNotCachedEngine implements PythonExecutionEngine {

    @Override
    public PythonExecutionResult exec(Set<String> dependencies, String script, Map<String, Serializable> vars) {
        ExternalPythonExecutor pythonExecutor = new ExternalPythonExecutor();
        return pythonExecutor.exec(script, vars);
    }

    @Override
    public PythonEvaluationResult eval(String prepareEnvironmentScript, String script, Map<String, Serializable> vars) {
        throw new NotImplementedException();
    }
}
