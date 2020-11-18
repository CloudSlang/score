package io.cloudslang.runtime.impl.python.external.model;

import java.io.File;


public class TempEvalEnvironment extends TempEnvironment {

    public TempEvalEnvironment(String mainScriptName, File parentFolder) {
        super(mainScriptName, parentFolder);
    }

}
