/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package io.cloudslang.score.exceptions;

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
