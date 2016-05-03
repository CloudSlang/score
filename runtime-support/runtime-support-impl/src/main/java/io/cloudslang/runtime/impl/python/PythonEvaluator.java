package io.cloudslang.runtime.impl.python;

import org.python.core.PyException;
import org.python.util.PythonInterpreter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.*;

@Component
public class PythonEvaluator extends AbstractScriptInterpreter {

    private static final String TRUE = "true";
    private static final String FALSE = "false";

    @Autowired
    @Qualifier("evalInterpreter")
    private PythonInterpreter interpreter;

    public synchronized Serializable evalExpr(String prepareEnvironmentScript, String expr, Map<String, Serializable> context) {
        try {
            cleanInterpreter(interpreter);
            prepareInterpreterContext(context);
            if(prepareEnvironmentScript != null && !prepareEnvironmentScript.isEmpty()) {
                exec(interpreter, prepareEnvironmentScript);
            }
            return eval(interpreter, expr);
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

    private void prepareInterpreterContext(Map<String, Serializable> context) {
        super.prepareInterpreterContext(interpreter, context);
        if (interpreter.get(TRUE) == null)
            interpreter.set(TRUE, Boolean.TRUE);
        if (interpreter.get(FALSE) == null)
            interpreter.set(FALSE, Boolean.FALSE);
    }
}
