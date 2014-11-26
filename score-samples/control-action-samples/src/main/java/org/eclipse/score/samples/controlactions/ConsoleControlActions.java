/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package org.eclipse.score.samples.controlactions;

import org.eclipse.score.lang.ExecutionRuntimeServices;

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
