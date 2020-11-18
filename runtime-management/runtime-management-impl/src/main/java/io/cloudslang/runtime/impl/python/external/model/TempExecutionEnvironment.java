package io.cloudslang.runtime.impl.python.external.model;

import java.io.File;


public class TempExecutionEnvironment extends TempEnvironment {
    private final String userScriptName;

    public TempExecutionEnvironment(String userScriptName, String mainScriptName, File parentFolder) {
        super(mainScriptName, parentFolder);
        this.userScriptName = userScriptName;
    }

    public String getUserScriptName() {
        return userScriptName;
    }

}
