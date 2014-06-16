package com.hp.score.lang;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * User: maromg
 * Date: 11/06/2014
 */
public class ExecutionRuntimeServices implements Serializable {

    private static final long serialVersionUID = 2557429503280678353L;

    protected static final String EXECUTION_PAUSED = "EXECUTION_PAUSED";

    protected Map<String, Serializable> myMap = new HashMap<>();

    public void pause() {
        myMap.put(EXECUTION_PAUSED, Boolean.TRUE);
    }

    public boolean isPaused() {
        return myMap.containsKey(EXECUTION_PAUSED) && myMap.get(EXECUTION_PAUSED).equals(Boolean.TRUE);
    }

}
