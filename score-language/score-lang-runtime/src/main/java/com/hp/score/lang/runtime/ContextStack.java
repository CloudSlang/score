package com.hp.score.lang.runtime;

import java.io.Serializable;
import java.util.Map;
import java.util.Stack;

/**
 * User: stoneo
 * Date: 07/10/2014
 * Time: 12:53
 */
public class ContextStack implements Serializable {

    private Stack<Map<String, Serializable>> stack = new Stack<>();

    public void pushContext(Map<String, Serializable> newContext){
        stack.push(newContext);
    }

    public Map<String, Serializable> popContext(){
        if(stack.empty())
            return null;
        return stack.pop();
    }

}
