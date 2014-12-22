/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package org.eclipse.score.samples.controlactions;

import org.openscore.lang.ExecutionRuntimeServices;

import java.util.Map;

/**
 * User: stoneo
 * Date: 19/08/2014
 * Time: 10:33
 */
public class SessionDataActions {

    public static final String SESSION_BEFORE_PUT_DATA_EVENT = "sessionBeforePutDataEvent";
    public static final String SESSION_GET_DATA_EVENT = "sessionGetDataEvent";

    private static String TEST_KEY = "sessionTestKey";
    public static String TEST_VALUE = "sessionTestValue";

    public void putObject(ExecutionRuntimeServices executionRuntimeServices, Map<String, Object> nonSerializableExecutionData){

        String sessionObject = (String) nonSerializableExecutionData.get(TEST_KEY);
        String value = sessionObject == null ? null : sessionObject;
        executionRuntimeServices.addEvent(SESSION_BEFORE_PUT_DATA_EVENT, value);
        if (sessionObject == null) {
            nonSerializableExecutionData.put(TEST_KEY, TEST_VALUE);
        }
    }

    public void getObject(ExecutionRuntimeServices executionRuntimeServices, Map<String, Object> nonSerializableExecutionData){
        String sessionObject = (String) nonSerializableExecutionData.get(TEST_KEY);
        String value = sessionObject == null ? null : sessionObject;
        executionRuntimeServices.addEvent(SESSION_GET_DATA_EVENT, value);
    }

    public void sleepAction(){
        try {
            System.out.println("Sleeping...");
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

