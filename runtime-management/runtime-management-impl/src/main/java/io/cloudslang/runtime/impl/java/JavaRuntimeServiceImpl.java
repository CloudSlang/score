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

import io.cloudslang.runtime.api.java.JavaRuntimeService;
import org.python.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JavaRuntimeServiceImpl implements JavaRuntimeService {
    @Autowired
    private JavaExecutorProvider javaExecutorProvider;

    @Override
    public Object execute(String dependency, String className, String methodName, Object ... args) {
        return javaExecutorProvider.allocateExecutor((dependency == null || dependency.isEmpty()) ? Sets.<String>newHashSet() :
                Sets.newHashSet(dependency)).execute(className, methodName, args);
    }
}
