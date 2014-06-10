package com.hp.score.lang;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * User: maromg
 * Date: 10/06/2014
 */
public class ScoreSystemContext extends HashMap<String, Serializable> {

    private static final long serialVersionUID = 2557429503280678353L;

    public ScoreSystemContext() {
        super();
    }

    public ScoreSystemContext(Map<? extends String, ? extends Serializable> m) {
        super(m);
    }

    public void addEvent(String type, Serializable data) {
    }

    public void pauseExecution() {
    }

    public void setError(String error) {
    }

}
