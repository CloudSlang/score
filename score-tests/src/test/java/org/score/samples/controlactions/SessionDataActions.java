package org.score.samples.controlactions;

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

