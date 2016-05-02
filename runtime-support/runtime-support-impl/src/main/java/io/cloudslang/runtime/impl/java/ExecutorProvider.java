package io.cloudslang.runtime.impl.java;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public abstract class ExecutorProvider {
    private static final String NO_DEPENDENCIES_KEY = "";

    protected String generatedDependenciesKey(List<String> dependencies) {
        // optimistic - this is java (which has only one dependency) or python with on dependency
        if(dependencies.size() == 1) {
            return dependencies.get(0);
        }
        // optimistic - this is old content - no dependencies
        if(dependencies.isEmpty()) {
            return NO_DEPENDENCIES_KEY;
        }

        List<String> actionDependencies = new LinkedList<>();
        actionDependencies.addAll(dependencies);
        Collections.sort(actionDependencies);
        StringBuilder sb = new StringBuilder();
        for (String dependency : actionDependencies) {
            sb.append(dependency).append(";");
        }
        return sb.toString();
    }
}
