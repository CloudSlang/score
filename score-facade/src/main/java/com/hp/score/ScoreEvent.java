package com.hp.score;

import java.io.Serializable;

/**
 * User: maromg
 * Date: 10/06/2014
 */
public class ScoreEvent {

    private String type;
    private Serializable data;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Serializable getData() {
        return data;
    }

    public void setData(Serializable data) {
        this.data = data;
    }
}
