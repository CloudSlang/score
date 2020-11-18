package io.cloudslang.runtime.impl.python.external.model;

import java.io.File;

public class TempEnvironment {

    private final String mainScriptName;
    private final File parentFolder;

    public TempEnvironment(String mainScriptName, File parentFolder) {
        this.mainScriptName = mainScriptName;
        this.parentFolder = parentFolder;
    }

    public String getMainScriptName() {
        return mainScriptName;
    }

    public File getParentFolder() {
        return parentFolder;
    }

}
