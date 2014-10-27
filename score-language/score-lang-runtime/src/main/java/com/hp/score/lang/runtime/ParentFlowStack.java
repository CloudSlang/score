package com.hp.score.lang.runtime;

import java.io.Serializable;
import java.util.Stack;

/**
 * User: stoneo
 * Date: 22/10/2014
 * Time: 15:37
 */
public class ParentFlowStack implements Serializable{

    private Stack<ParentFlowData> stack = new Stack<>();

    public void pushParentFlowData(ParentFlowData newContext){
        stack.push(newContext);
    }

    public ParentFlowData popParentFlowData(){
        if(stack.empty())
            return null;
        return stack.pop();
    }

    public boolean isEmpty(){
        return stack.isEmpty();
    }
}
