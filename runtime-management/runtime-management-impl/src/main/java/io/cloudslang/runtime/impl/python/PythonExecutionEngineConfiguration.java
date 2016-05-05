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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by Genadi Rabinovich, genadi@hpe.com on 05/05/2016.
 */
@Configuration
public class PythonExecutionEngineConfiguration {
    @Bean
    PythonExecutionEngine pythonExecutionEngine() {
        String noCacheEngine = PythonExecutionNotCachedEngine.class.getSimpleName();
        String cacheEngine = PythonExecutionCachedEngine.class.getSimpleName();
        return System.getProperty("python.executor.engine", cacheEngine).equals(noCacheEngine) ?
                new PythonExecutionNotCachedEngine() : new PythonExecutionCachedEngine();
    }
}
