package com.hp.score.lang.runtime;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ReturnValues implements Serializable{

    private final HashMap<String, String> outputs;

    private final String answer;

    public ReturnValues(Map<String, String> outputs, String answer) {
        this.outputs = new HashMap<>(outputs);
        this.answer = answer;
    }

    public Map<String, String> getOutputs() {
        return outputs;
    }

    public String getAnswer() {
        return answer;
    }
}
