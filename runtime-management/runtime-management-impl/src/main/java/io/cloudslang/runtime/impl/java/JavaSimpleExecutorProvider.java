package io.cloudslang.runtime.impl.java;

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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class JavaSimpleExecutorProvider extends ExecutorProvider implements JavaExecutorProvider{
    @Autowired
    private DependencyService dependencyService;

    @Override
    public JavaExecutor allocateExecutor(Set<String> dependencies) {
        return new JavaExecutor(dependencyService.getDependencies(dependencies));
    }
}
