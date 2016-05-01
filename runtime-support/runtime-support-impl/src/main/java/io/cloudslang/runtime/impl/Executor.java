package io.cloudslang.runtime.impl;

import java.util.ArrayList;
import java.util.List;

public abstract class Executor {
    protected final String dependenciesKey;
    protected final  List<String> dependencies = new ArrayList<>();

    protected Executor(List<String> deps, String depKey) {
        dependenciesKey = depKey;
        dependencies.addAll(deps);
    }

    public String getDependenciesKey() {
        return dependenciesKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Executor that = (Executor) o;

        return getDependenciesKey().equals(that.getDependenciesKey());

    }

    @Override
    public int hashCode() {
        return getDependenciesKey().hashCode();
    }

    @Override
    public String toString() {
        return "DependenciesKey=[" + dependenciesKey + "]";
    }
}
