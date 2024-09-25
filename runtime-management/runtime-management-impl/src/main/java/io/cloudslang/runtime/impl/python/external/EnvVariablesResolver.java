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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.cloudslang.runtime.api.python.enums.EnvVariablesStrategy.FULL_ENV;
import static io.cloudslang.runtime.api.python.enums.EnvVariablesStrategy.NO_ENV;
import static io.cloudslang.runtime.api.python.enums.EnvVariablesStrategy.PARTIAL_ENV;
import static io.cloudslang.runtime.api.python.enums.EnvVariablesStrategy.getEnvVariableStrategy;
import static java.lang.System.getenv;
import static java.util.Arrays.asList;

public class EnvVariablesResolver {

    private static final EnvVariablesStrategy ENV_VARIABLES_STRATEGY = getEnvVariableStrategy(System.getProperty("python.envVariablesStrategy"), NO_ENV);
    private static final String ALLOWED_PATHS_ENV_VARIABLES = System.getProperty("python.allowedPathsForEnvVariables");

    public static void processEnvironmentVariablesAllowedForPython(ProcessBuilder processBuilder) {
        if (ENV_VARIABLES_STRATEGY == FULL_ENV) {
            return;
        }
        if (ENV_VARIABLES_STRATEGY == NO_ENV) {
            processBuilder.environment().clear();
            return;
        }
        if (ENV_VARIABLES_STRATEGY == PARTIAL_ENV) {
            processBuilder.environment().clear();
            if (ALLOWED_PATHS_ENV_VARIABLES.isEmpty()) {
                return;
            }
            Map<String, String> env = getenv();
            List<String> allowedEnvVariables = new ArrayList<>(asList(ALLOWED_PATHS_ENV_VARIABLES.split(",")));
            Map<String, String> newAllowedEnvVariables = new HashMap<>(allowedEnvVariables.size());
            for (String envName : allowedEnvVariables) {
                if (env.containsKey(envName)) {
                    newAllowedEnvVariables.put(envName, env.get(envName));
                }
            }
            processBuilder.environment().putAll(newAllowedEnvVariables);
        }
    }
}
