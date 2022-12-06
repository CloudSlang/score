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
import static org.apache.commons.lang3.Validate.notNull;

public enum PythonStrategy {

    PYTHON_SERVER("python-server"),
    PYTHON("python"),
    JYTHON("jython");

    private final String strategyName;

    PythonStrategy(String strategyName) {
        this.strategyName = strategyName;
    }

    public static PythonStrategy getPythonStrategy(final String givenStrategy, final PythonStrategy defaultStrategy) {
        notNull(defaultStrategy, "Default strategy cannot be null.");
        return stream(PythonStrategy.values())
                .filter(strategy -> equalsIgnoreCase(strategy.getStrategyName(), givenStrategy))
                .findFirst()
                .orElse(defaultStrategy);
    }

    public String getStrategyName() {
        return strategyName;
    }
}
