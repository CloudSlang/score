package com.hp.score.lang.runtime;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * User: stoneo
 * Date: 20/10/2014
 * Time: 10:28
 */
public class RunEnvironment implements Serializable{

    private LinkedHashMap<String, Serializable> currentContext;

    private String answer;

    // stack holding the contexts of the parent scopes
    private ContextStack contextStack;

    // stack of the parent flows data (fo the sub-flow use-case)
    private ParentFlowStack parentFlowStack;

//    LinkedHashMap<String, Serializable> systemProperties;

    public RunEnvironment() {
        currentContext = new LinkedHashMap<>();
        contextStack = new ContextStack();
        parentFlowStack = new ParentFlowStack();

    }

    public RunEnvironment(Map<String, Serializable> values) {
        this();
        currentContext.putAll(values);
    }

    public ContextStack getStack(){
        return contextStack;
    }

    public ParentFlowStack getParentFlowStack() {
        return parentFlowStack;
    }

    public Map<String, Serializable> getCurrentContext(){
        return currentContext;
    }

    public void setCurrentContext(Map<String, ? extends Serializable> context) {
        currentContext = new LinkedHashMap<>();
        currentContext.putAll(context);
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}
