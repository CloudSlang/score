package io.cloudslang.runtime.api.python;


import java.io.Serializable;
import java.util.Map;

public interface ExternalPythonProcessRunService {

    PythonExecutionResult exec(String script, Map<String, Serializable> inputs);
    PythonEvaluationResult eval(String expression, String prepareEnvironmentScript, Map<String, Serializable> context);
    PythonEvaluationResult test(String expression, String prepareEnvironmentScript, Map<String, Serializable> context,
            long timeout);

}
