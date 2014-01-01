package com.hp.oo.internal.sdk.execution;

/**
 * Created by IntelliJ IDEA.
 * User: kravtsov
 * Date: 15/12/11
 * Time: 16:19
 */
public class NoProperNavigationException extends FlowExecutionException {

    public NoProperNavigationException(String message,String stepName) {
        super(message,stepName);
    }

    public NoProperNavigationException(String message) {
        super(message);
    }

    public NoProperNavigationException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoProperNavigationException(String message, Throwable cause,String stepName) {
        super(message, cause,stepName);
    }
}
