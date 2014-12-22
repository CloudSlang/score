/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package org.openscore.samples.controlactions;

import org.openscore.lang.ExecutionRuntimeServices;

/**
 * User:
 * Date: 22/06/2014
 * <p/>
 * A simple control action that prints "Hello score" to the system out, and sends an event on it.
 */
public class ConsoleControlActions {

    public void echoHelloScore(ExecutionRuntimeServices executionRuntimeServices) {
        System.out.println("Hello score");

        executionRuntimeServices.addEvent("Hello score","Hello score");//send an Event with same msg
    }

}
