/*
 * Copyright © 2014-2017 EntIT Software LLC, a Micro Focus company (L.P.)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.cloudslang.samples.controlactions;

import io.cloudslang.score.lang.ExecutionRuntimeServices;

import java.util.Map;

/**
 * User: stoneo
 * Date: 19/08/2014
 * Time: 10:33
 */
public class SessionDataActions {

    public static final String SESSION_BEFORE_PUT_DATA_EVENT = "sessionBeforePutDataEvent";
    public static final String SESSION_GET_DATA_EVENT = "sessionGetDataEvent";
    private static final String GLOBAL_SESSION_OBJECT = "globalSessionObject";

    private static String TEST_KEY = "sessionTestKey";
    public static String TEST_VALUE = "sessionTestValue";

    public void putObject(ExecutionRuntimeServices executionRuntimeServices, Map<String, Object> nonSerializableExecutionData){

        Map<String, Object> globalSessionObject = (Map<String, Object>) nonSerializableExecutionData.get(GLOBAL_SESSION_OBJECT);
        String sessionObject = (String) globalSessionObject.get(TEST_KEY);
        String value = sessionObject == null ? null : sessionObject;
        executionRuntimeServices.addEvent(SESSION_BEFORE_PUT_DATA_EVENT, value);
        if (sessionObject == null) {
            globalSessionObject.put(TEST_KEY, TEST_VALUE);
        }
    }

    public void getObject(ExecutionRuntimeServices executionRuntimeServices, Map<String, Object> nonSerializableExecutionData){
        Map<String, Object> globalSessionObject = (Map<String, Object>) nonSerializableExecutionData.get(GLOBAL_SESSION_OBJECT);
        String sessionObject = (String) globalSessionObject.get(TEST_KEY);
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

