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
package io.cloudslang.runtime.impl.python.external;

import io.cloudslang.runtime.api.python.enums.EnvVariablesStrategy;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.cloudslang.runtime.api.python.enums.EnvVariablesStrategy.INHERIT_ALL;
import static io.cloudslang.runtime.api.python.enums.EnvVariablesStrategy.INHERIT_NONE;
import static io.cloudslang.runtime.api.python.enums.EnvVariablesStrategy.INHERIT_SUBSET;
import static io.cloudslang.runtime.api.python.enums.EnvVariablesStrategy.getEnvVariableStrategy;
import static java.lang.System.getProperty;
import static java.util.Arrays.stream;
import static org.apache.commons.lang3.StringUtils.split;

public class EnvVariablesUtil {

    private static final EnvVariablesStrategy ENVIRONMENT_VARIABLES_STRATEGY = getEnvVariableStrategy(getProperty("python.environmentVariablesStrategy"));
    private static final List<String> ENVIRONMENT_VARIABLES_SUBSET = stream(split(getProperty("python.environmentVariablesSubset", ""), ","))
            .collect(Collectors.toList());

    private EnvVariablesUtil() {
    }

    public static void processEnvironmentVariablesAllowedForPython(ProcessBuilder processBuilder) {
        if (ENVIRONMENT_VARIABLES_STRATEGY == INHERIT_ALL) {
            return;
        }
        if (ENVIRONMENT_VARIABLES_STRATEGY == INHERIT_NONE) {
            processBuilder.environment().clear();
            return;
        }
        if (ENVIRONMENT_VARIABLES_STRATEGY == INHERIT_SUBSET) {
            Map<String, String> environmentVariables = processBuilder.environment();
            environmentVariables.entrySet()
                    .removeIf(entry -> !ENVIRONMENT_VARIABLES_SUBSET.contains(entry.getKey()));
        }
    }
}
