package com.hp.score.samples.controlactions;

import com.hp.score.lang.ExecutionRuntimeServices;

/**
 * User: maromg
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
