/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/

package io.cloudslang.runtime.impl.java;

import io.cloudslang.dependency.api.services.DependencyService;
import io.cloudslang.runtime.api.java.JavaExecutionParametersProvider;
import io.cloudslang.runtime.impl.ExecutionEngine;
import org.python.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by Genadi Rabinovich, genadi@hpe.com on 05/05/2016.
 */
public class JavaExecutionNoCachedEngine extends ExecutionEngine implements JavaExecutionEngine {
    @Autowired
    private DependencyService dependencyService;

    @Override
    public Object execute(String dependency, String className, String methodName, JavaExecutionParametersProvider parametersProvider) {
        JavaExecutor executor = new JavaExecutor(dependencyService.getDependencies((dependency == null || dependency.isEmpty()) ? Sets.<String>newHashSet() :
                Sets.newHashSet(dependency)));
        try {
            return executor.execute(className, methodName, parametersProvider);
        } finally {
            executor.close();
        }
    }
}
