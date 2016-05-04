package io.cloudslang.runtime.impl;

/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/

import io.cloudslang.dependency.api.services.DependencyService;

import java.util.*;

public abstract class CachedStaticsSharedExecutionEngine<T extends Executor> extends ExecutionEngine {
    // key --> dependencies concatenated
    // value --> classloader/pyinterpreter which was build with classpath from these dependencies
    private final Map<String, T> executors = new HashMap<>();

    // if cached classloaders/intepreters number is limited and limit is reached we will
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
                    Iterator<String> executorIterator = recentlyUsed.iterator();
                    String candidateForRemovalKey = executorIterator.next();
                    executorIterator.remove();
                    T removedExecutor = executors.remove(candidateForRemovalKey);
                    removedExecutor.release();
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
