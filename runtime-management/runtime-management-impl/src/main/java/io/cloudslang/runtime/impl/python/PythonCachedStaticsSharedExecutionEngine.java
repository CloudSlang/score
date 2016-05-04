/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/

package io.cloudslang.runtime.impl.python;

import io.cloudslang.dependency.api.services.DependencyService;
import io.cloudslang.runtime.impl.CachedStaticsSharedExecutionEngine;
import org.python.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

public class PythonCachedStaticsSharedExecutionEngine extends CachedStaticsSharedExecutionEngine<PythonExecutor> implements PythonExecutionEngine{
    @Autowired
    private DependencyService dependencyService;

    @Value("#{systemProperties['python.executor.cache.size'] != null ? systemProperties['python.executor.cache.size'] : 100}")
    private int cacheSize;

    @Override
    public Map<String, Serializable> exec(Set<String> dependencies, String script, Map<String, Serializable> vars) {
        return allocateExecutor(dependencies).exec(script, vars);
    }

    @Override
    public Serializable eval(String prepareEnvironmentScript, String script, Map<String, Serializable> vars) {
        return allocateExecutor(Sets.<String>newHashSet()).eval(prepareEnvironmentScript, script, vars);
    }

    @Override
    protected DependencyService getDependencyService() {
        return dependencyService;
    }

    @Override
    protected int getCacheSize() {
        return cacheSize;
    }

    @Override
    protected PythonExecutor createNewExecutor(Set<String> filePaths) {
        return new PythonExecutor(filePaths);
    }
}
