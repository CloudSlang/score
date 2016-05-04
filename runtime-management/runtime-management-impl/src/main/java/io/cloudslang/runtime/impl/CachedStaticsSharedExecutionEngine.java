package io.cloudslang.runtime.impl;

import io.cloudslang.dependency.api.services.DependencyService;
import io.cloudslang.runtime.impl.java.ExecutionEngine;

import java.util.*;

public abstract class CachedStaticsSharedExecutionEngine<T extends Executor> extends ExecutionEngine {
    // key --> dependencies concatenated
    // value --> classloader which was build with classpath from these dependencies
    private final Map<String, T> executors = new HashMap<>();

    // if cached classloaders number is limited and limit is reached we will
    // remove the least recently used (first in the list) and will create the new one
    private final Set<String> recentlyUsed = new LinkedHashSet<>();

    protected T allocateExecutor(Set<String> dependencies) {
        String dependenciesKey = generatedDependenciesKey(dependencies);

        T executor;
        executor = executors.get(dependenciesKey);
        Set<String> filePaths = null;
        if (executor == null) {
            // may be first time execution - ensure resource resolution
            filePaths = getDependencyService().getDependencies(dependencies);
        }

        synchronized (recentlyUsed) {
            if (executor == null) {
                if (recentlyUsed.size() == getCacheSize()) {
                    Iterator<String> javaExecutorIterator = recentlyUsed.iterator();
                    String candidateForRemovalKey = javaExecutorIterator.next();
                    javaExecutorIterator.remove();
                    executors.remove(candidateForRemovalKey);
                }
                executor = createNewExecutor(filePaths);
                executors.put(dependenciesKey, executor);
            }
            recentlyUsed.remove(dependenciesKey);
            recentlyUsed.add(dependenciesKey);
        }
        return executor;
    }

    protected abstract DependencyService getDependencyService();
    protected abstract int getCacheSize();
    protected abstract T createNewExecutor(Set<String> filePaths);
}
