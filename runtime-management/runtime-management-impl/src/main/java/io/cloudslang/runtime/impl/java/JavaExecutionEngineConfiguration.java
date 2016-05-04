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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JavaExecutionEngineConfiguration {
    @Bean
    JavaExecutionEngine javaExecutorProvider() {
        return System.getProperty("java.executor.provider", "JavaCachedStaticsSharedExecutionEngine").equals("JavaCachedStaticsNotSharedExecutionEngine") ?
                new JavaCachedStaticsNotSharedExecutionEngine() : new JavaCachedStaticsSharedExecutionEngine();
    }
}
