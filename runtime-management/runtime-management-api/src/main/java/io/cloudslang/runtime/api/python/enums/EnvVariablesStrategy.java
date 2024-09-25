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
package io.cloudslang.runtime.api.python.enums;

import static java.util.Arrays.stream;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

public enum EnvVariablesStrategy {
    INHERIT_NONE("inherit-none"), // This is the default strategy if misconfigured or unspecified
    INHERIT_SUBSET("inherit-subset"),
    INHERIT_ALL("inherit-all");

    private final String envVarStrategy;

    EnvVariablesStrategy(String envVarStrategy) {
        this.envVarStrategy = envVarStrategy;
    }

    public static EnvVariablesStrategy getEnvVariableStrategy(final String givenStrategy) {
        return stream(EnvVariablesStrategy.values())
                .filter(strategy -> equalsIgnoreCase(strategy.getEnvVariableStrategy(), givenStrategy))
                .findFirst()
                .orElse(INHERIT_NONE);
    }

    public String getEnvVariableStrategy() {
        return envVarStrategy;
    }
}
