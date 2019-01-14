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

package io.cloudslang.runtime.impl;

import io.cloudslang.runtime.impl.java.JavaExecutionEngineConfiguration;
import io.cloudslang.runtime.impl.python.PythonExecutionEngineConfiguration;
import io.cloudslang.runtime.impl.rpa.RpaExecutionEngineConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Created by Genadi Rabinovich, genadi@hpe.com on 08/05/2016.
 */
@Configuration
@Import({
        JavaExecutionEngineConfiguration.class,
        PythonExecutionEngineConfiguration.class,
        RpaExecutionEngineConfiguration.class
})
public class RuntimeManagementConfiguration {
}
