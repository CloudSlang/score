package com.hp.score.lang.compiler.domain;

public class Input {

    private final String name;

    private final String defaultValue;

    private final String expression ;

    private final boolean encrypted;

    private final boolean required;

    public Input(String name) {
        this.name = name;
        this.defaultValue = null;
        this.expression = null;
        this.encrypted = false ;
        this.required = true ;
    }

    public Input(String name, String defaultValue,String expression,boolean encrypted,boolean required) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.expression = expression;
        this.encrypted = encrypted ;
        this.required = required ;
    }

    public String getName() {
        return name;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public String getExpression() {
        return expression;
    }

    public boolean isEncrypted() {
        return encrypted;
    }

    public boolean isRequired() {
        return required;
    }

}
