package com.hp.score.lang.compiler.domain;

public class Input {

    private final String name;
    private final String value;

    public Input(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }
}
