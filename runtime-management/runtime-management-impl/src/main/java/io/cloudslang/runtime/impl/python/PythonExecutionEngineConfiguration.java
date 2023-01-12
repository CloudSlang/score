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
import io.cloudslang.runtime.impl.python.external.ExternalPythonExecutorServiceImpl;
import io.cloudslang.runtime.impl.python.external.ExternalPythonRuntimeServiceImpl;
import io.cloudslang.runtime.impl.python.external.StatefulRestEasyClientsHolder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.concurrent.Semaphore;

import static io.cloudslang.runtime.impl.python.PythonExecutionConfigurationConsts.PYTHON_EXECUTOR_ENGINE;

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
        Integer pythonTestingProcessPermits = Integer.getInteger("python.testing.concurrent.execution.permits", 10);
        return new ExternalPythonRuntimeServiceImpl(new Semaphore(pythonProcessPermits), new Semaphore(pythonTestingProcessPermits));
    }

    @Bean(name = "externalPythonExecutorService")
    public PythonRuntimeService externalPythonExecutorService(StatefulRestEasyClientsHolder statefulRestEasyClient) {
        Integer pythonProcessPermits = Integer.getInteger("python.concurrent.execution.permits", 30);
        Integer pythonTestingProcessPermits = Integer.getInteger("python.testing.concurrent.execution.permits", 10);
        return new ExternalPythonExecutorServiceImpl(statefulRestEasyClient, new Semaphore(pythonProcessPermits), new Semaphore(pythonTestingProcessPermits));
    }

    @Bean(name = "jythonExecutionEngine")
    PythonExecutionEngine pythonExecutionEngine() {
        String pooledAndCachedEngine = PythonExecutionPooledAndCachedEngine.class.getSimpleName();
        String cacheEngine = PythonExecutionCachedEngine.class.getSimpleName();
        String simpleEngine = PythonExecutionNotCachedEngine.class.getSimpleName();

        String value = System.getProperty(PYTHON_EXECUTOR_ENGINE, pooledAndCachedEngine);

        if (StringUtils.equalsIgnoreCase(value, pooledAndCachedEngine)) {
            return new PythonExecutionPooledAndCachedEngine();
        } else if (StringUtils.equalsIgnoreCase(value, cacheEngine)) {
            return new PythonExecutionCachedEngine();
        } else if (StringUtils.equalsIgnoreCase(value, simpleEngine)) {
            return new PythonExecutionNotCachedEngine();
        } else {
            return new PythonExecutionPooledAndCachedEngine();
        }
    }

    @Bean(name = "externalPythonExecutionEngine")
    PythonExecutionEngine externalPythonExecutionEngine() {
        return new ExternalPythonExecutionEngine();
    }


}
