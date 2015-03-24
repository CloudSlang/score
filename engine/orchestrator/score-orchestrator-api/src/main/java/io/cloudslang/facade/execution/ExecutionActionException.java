package io.cloudslang.facade.execution;

/**
 * User: noym
 * Date: 25/11/2014
 * Time: 10:43
 */
public class ExecutionActionException extends RuntimeException {

    private ExecutionActionResult result;

    public ExecutionActionException(String message) {
        super(message);
    }

    public ExecutionActionException(String message, ExecutionActionResult result) {
        super(message);
        this.result = result;
    }

    public ExecutionActionResult getResult() {
        return result;
    }
}
