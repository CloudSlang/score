package io.cloudslang.runtime.impl.java;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public abstract class ExecutorProvider {
    private static final String NO_DEPENDENCIES_KEY = "";

    protected String generatedDependenciesKey(Set<String> dependencies) {
        // optimistic - this is java (which has only one dependency) or python with on dependency
        if(dependencies.size() == 1) {
            return dependencies.iterator().next();
        }
        // optimistic - this is old content - no dependencies
        if(dependencies.isEmpty()) {
            return NO_DEPENDENCIES_KEY;
        }

        List<String> actionDependencies = new ArrayList<>();
        actionDependencies.addAll(dependencies);
        Collections.sort(actionDependencies);
        StringBuilder sb = new StringBuilder();
        for (String dependency : actionDependencies) {
            sb.append(dependency).append(";");
        }
        return sb.toString();
    }
}
