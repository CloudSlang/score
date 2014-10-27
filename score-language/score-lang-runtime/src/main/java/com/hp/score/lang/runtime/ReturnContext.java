package com.hp.score.lang.runtime;

import java.util.HashMap;
import java.util.Map;

/**
 * User: stoneo
 * Date: 26/10/2014
 * Time: 09:54
 */
public class ReturnContext {

    private Map<String, String> returnValues = new HashMap<>();

    private String answer;

    public ReturnContext(Map<String, String> returnValues, String answer) {
        this.returnValues = returnValues;
        this.answer = answer;
    }

    public Map<String, String> getReturnValues() {
        return returnValues;
    }

    public String getAnswer() {
        return answer;
    }
}
