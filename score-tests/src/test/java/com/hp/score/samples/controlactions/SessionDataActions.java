/*
 * Licensed to Hewlett-Packard Development Company, L.P. under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
*/
package com.hp.score.samples.controlactions;

import com.hp.oo.sdk.content.plugin.GlobalSessionObject;
import com.hp.oo.sdk.content.plugin.SessionResource;
import com.hp.score.lang.ExecutionRuntimeServices;

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

        Object sessionObject = nonSerializableExecutionData.get(TEST_KEY);
        String value = sessionObject == null ? null : (String)((GlobalSessionObject)sessionObject).get();
        executionRuntimeServices.addEvent(SESSION_BEFORE_PUT_DATA_EVENT, value);
        if (sessionObject == null) {
            sessionObject = new GlobalSessionObject<String>();
            ((GlobalSessionObject)sessionObject).setResource(new SampleSessionResource(TEST_VALUE));
            nonSerializableExecutionData.put(TEST_KEY, sessionObject);
        }
    }

    public void getObject(ExecutionRuntimeServices executionRuntimeServices, Map<String, Object> nonSerializableExecutionData){

        Object sessionObject = nonSerializableExecutionData.get(TEST_KEY);
        String value = sessionObject == null ? null : (String)((GlobalSessionObject)sessionObject).get();
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

    private static class SampleSessionResource extends SessionResource<String> {
        private String value;

        public SampleSessionResource(String value) {
            this.value = value;
        }

        @Override
        public String get() {
            return value;
        }

        @Override
        public void release() {
            value = null;
        }
    }

}

