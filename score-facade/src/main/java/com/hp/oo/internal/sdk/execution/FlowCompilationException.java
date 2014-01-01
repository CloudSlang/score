package com.hp.oo.internal.sdk.execution;

/**
 * Created by IntelliJ IDEA.
 * User: butensky
 * Date: 12/13/11
 * Time: 11:05 AM
 * This class represents Exception that is thrown during Flow Compilation.
 */
public class FlowCompilationException extends RuntimeException {

    public FlowCompilationException(String message) {
        super(message);
    }

    public FlowCompilationException(String message, Throwable cause) {
        super(message, cause);
    }
}
