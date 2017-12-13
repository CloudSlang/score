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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Created by Genadi Rabinovich, genadi@hpe.com on 05/05/2016.
 */
@Configuration
@ComponentScan("io.cloudslang.runtime.impl.python")
@Import({DependenciesManagementConfiguration.class})
public class PythonExecutionEngineConfiguration {
    @Bean
    public PythonRuntimeService pythonRuntimeService() {
        return new PythonRuntimeServiceImpl();
    }

    @Bean
    PythonExecutionEngine pythonExecutionEngine() {
        String noCacheEngine = PythonExecutionNotCachedEngine.class.getSimpleName();
        String cacheEngine = PythonExecutionCachedEngine.class.getSimpleName();
        return System.getProperty(PythonExecutionConfigurationConsts.PYTHON_EXECUTOR_ENGINE, cacheEngine).equals(noCacheEngine) ?
                new PythonExecutionNotCachedEngine() : new PythonExecutionCachedEngine();
    }
}
