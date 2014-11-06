package com.hp.score.lang.runtime.env;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ReturnValues implements Serializable{

    private final HashMap<String, String> outputs;

    private final String result;

    public ReturnValues(Map<String, String> outputs, String result) {
        this.outputs = new HashMap<>(outputs);
        this.result = result;
    }

    public Map<String, String> getOutputs() {
        return outputs;
    }

    public String getResult() {
        return result;
    }
}
