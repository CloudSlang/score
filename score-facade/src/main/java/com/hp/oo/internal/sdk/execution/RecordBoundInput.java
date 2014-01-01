package com.hp.oo.internal.sdk.execution;

import java.io.Serializable;

/**
 * User: hanael
 */
public class RecordBoundInput implements Serializable {

    private static final long serialVersionUID = 7451660603268678045L;

    private String name;

    private String termName;

    private String value;

    public RecordBoundInput() {
    }

    public RecordBoundInput(String name, String termName, String value) {
        this.name = name;
        this.termName = termName;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTermName() {
        return termName;
    }

    public void setTermName(String termName) {
        this.termName = termName;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
