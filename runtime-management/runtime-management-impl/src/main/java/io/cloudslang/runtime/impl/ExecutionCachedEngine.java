/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/

package io.cloudslang.runtime.impl;

import io.cloudslang.dependency.api.services.DependencyService;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Genadi Rabinovich, genadi@hpe.com on 05/05/2016.
 */
public abstract class ExecutionCachedEngine<T extends Executor> extends ExecutionEngine {
    // key --> dependencies concatenated
    // value --> classloader/pythoninterpreter which was build with classpath from these dependencies
    // if we reached the limit of cache we will release the least recently used
    private final Map<String, T> executors = new LinkedHashMap<>();

    private final Lock lock = new ReentrantLock();

    public T allocateExecutor(Set<String> dependencies) {
        String dependenciesKey = generatedDependenciesKey(dependencies);

        T executor;
        executor = executors.get(dependenciesKey);
        Set<String> filePaths = null;
        if (executor == null) {
            // may be first time execution - ensure resource resolution
            filePaths = getDependencyService().getDependencies(dependencies);
        }

        T candidateForRemove = null;
        try {
            lock.lock();
            if (executor == null) {
                if (executors.size() == getCacheSize()) {
                    Iterator<Map.Entry<String, T>> iterator = executors.entrySet().iterator();
                    candidateForRemove = iterator.next().getValue();
                    iterator.remove();
                }
                executor = createNewExecutor(filePaths);
            } else {
                // remove it and place at the end - most recently used
                executors.remove(dependenciesKey);
            }
            executor.allocate();
            executors.put(dependenciesKey, executor);
        } finally {
            lock.unlock();
        }
        if(candidateForRemove != null) {
            candidateForRemove.close();
        }
        return executor;
    }

    protected void releaseExecutor(T executor) {
        executor.release();
    }

    protected abstract DependencyService getDependencyService();
    protected abstract int getCacheSize();
    protected abstract T createNewExecutor(Set<String> filePaths);
}
