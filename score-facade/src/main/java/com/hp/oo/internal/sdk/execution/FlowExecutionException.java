package com.hp.oo.internal.sdk.execution;

/**
 * Created by IntelliJ IDEA.
 * User: butensky
 * Date: 9/13/11
 * Time: 9:38 AM
 * This class represents Exception that is thrown during Flow Execution.
 */
public class FlowExecutionException extends RuntimeException {

    private static final long serialVersionUID = -8309066019240283966L;

    private String stepName ;

    public FlowExecutionException(String message) {
        super(message);
    }

    public FlowExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public FlowExecutionException(String message, Throwable cause,String stepName) {
        super(message, cause);
    }

    public FlowExecutionException(String message, String stepName) {
        super(message);
        this.stepName = stepName ;
    }

    @Override
    public String getMessage() {
        return stepName == null ? super.getMessage() :super.getMessage() + " \nIn step: " + stepName ;
    }
}
