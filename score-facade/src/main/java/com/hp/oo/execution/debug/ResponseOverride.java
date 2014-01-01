package com.hp.oo.execution.debug;

import java.util.Map;

/**
 * User: hajyhia
 * Date: 2/24/13
 * Time: 3:37 PM
 */
public class ResponseOverride extends AbstractExecutionInterrupt {
    private static final long serialVersionUID = -3579066267537803080L;


    private static final String UNIVERSAL_OVERRIDE_UUID = "d4fbb32f-7289-4984-be06-e709e1909b6a";


    private String response;
    private boolean prompt;

    public ResponseOverride() {
        super();
    }

    public ResponseOverride(Map<String, String> interruptData, String response) {
        super(interruptData);
        this.response = response;
    }

    public ResponseOverride(String uuid, Map<String, String> interruptData, String response) {
        super(uuid, interruptData);
        this.response = response;
    }

    public synchronized void setResponse(String response) {
        this.response = response;
    }

    public synchronized String getResponse() {
        return this.response;
    }

    public synchronized boolean isPrompt() {
        return prompt;
    }

    public synchronized void setPrompt(boolean prompt) {
        this.prompt = prompt;
    }


    public static ResponseOverride UNIVERSAL_OVERRIDE_RESPONSES = new ResponseOverride(UNIVERSAL_OVERRIDE_UUID, null,null) {

        private static final long serialVersionUID = 8530277904572002714L;

        @Override
            public synchronized boolean isPrompt() {
                return true;
            };
        };


    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
