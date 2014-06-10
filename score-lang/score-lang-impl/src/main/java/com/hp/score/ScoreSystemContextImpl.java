package com.hp.score;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * User: maromg
 * Date: 10/06/2014
 */
public class ScoreSystemContextImpl extends HashMap<String, Serializable> implements ScoreSystemContext {

    private static final long serialVersionUID = 2557429503280678353L;

    public ScoreSystemContextImpl() {
        super();
    }

    public ScoreSystemContextImpl(Map<? extends String, ? extends Serializable> m) {
        super(m);
    }

    @Override
    public void addEvent(String type, Serializable data) {
        this.put()
    }

    @Override
    public void pauseExecution() {
    }

    @Override
    public void setError(String error) {
    }

}
