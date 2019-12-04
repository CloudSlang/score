package io.cloudslang.runtime.impl.python.external;

import io.cloudslang.runtime.api.python.PythonEvaluationResult;
import io.cloudslang.runtime.api.python.PythonExecutionResult;
import io.cloudslang.runtime.api.python.PythonRuntimeService;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;

public class ExternalPythonRuntimeServiceImpl implements PythonRuntimeService {
    @Resource(name = "externalPythonExecutionEngine")
    private ExternalPythonExecutionNotCachedEngine externalPythonExecutionNotCachedEngine;

    @Override
    public PythonExecutionResult exec(Set<String> dependencies, String script, Map<String, Serializable> vars) {
        return externalPythonExecutionNotCachedEngine.exec(dependencies, script, vars);
    }

    @Override
    public PythonEvaluationResult eval(String prepareEnvironmentScript, String script, Map<String, Serializable> vars) {
        return externalPythonExecutionNotCachedEngine.eval(prepareEnvironmentScript, script, vars);
    }
}
