/*
 * Copyright © 2014-2017 EntIT Software LLC, a Micro Focus company (L.P.)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.cloudslang.runtime.impl.python;

import io.cloudslang.dependency.impl.services.DependenciesManagementConfiguration;
import io.cloudslang.runtime.api.python.PythonRuntimeService;
import io.cloudslang.runtime.impl.python.external.ExternalPythonExecutionEngine;
import io.cloudslang.runtime.impl.python.external.ExternalPythonRuntimeServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.concurrent.Semaphore;

/**
 * Created by Genadi Rabinovich, genadi@hpe.com on 05/05/2016.
 */
@Configuration
@ComponentScan("io.cloudslang.runtime.impl.python")
@Import({DependenciesManagementConfiguration.class})
public class PythonExecutionEngineConfiguration {
    @Bean(name = "jythonRuntimeService")
    public PythonRuntimeService pythonRuntimeService() {
        return new PythonRuntimeServiceImpl();
    }

    @Bean(name = "externalPythonRuntimeService")
    public PythonRuntimeService externalPythonRuntimeService() {
        Integer pythonProcessPermits = Integer.getInteger("python.concurrent.execution.permits", 30);
        return new ExternalPythonRuntimeServiceImpl(new Semaphore(pythonProcessPermits));
    }

    @Bean(name = "jythonExecutionEngine")
    PythonExecutionEngine pythonExecutionEngine() {
        String noCacheEngine = PythonExecutionNotCachedEngine.class.getSimpleName();
        String cacheEngine = PythonExecutionCachedEngine.class.getSimpleName();
        return System.getProperty(PythonExecutionConfigurationConsts.PYTHON_EXECUTOR_ENGINE, cacheEngine).equals(noCacheEngine) ?
                new PythonExecutionNotCachedEngine() : new PythonExecutionCachedEngine();
    }

    @Bean(name = "externalPythonExecutionEngine")
    PythonExecutionEngine externalPythonExecutionEngine() {
        return new ExternalPythonExecutionEngine();
    }


}
