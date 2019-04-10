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
package io.cloudslang.engine.queue.enums;


import static java.util.Arrays.stream;
import static org.apache.commons.lang3.StringUtils.endsWithIgnoreCase;
import static org.apache.commons.lang3.Validate.notNull;

public enum AssignStrategy {

    RANDOM("random"),
    SECURE_RANDOM("securerandom"),
    ROUND_ROBIN("roundrobin");

    private final String strategyName;

    AssignStrategy(String strategy) {
        this.strategyName = strategy;
    }

    public static AssignStrategy getAssignedStrategy(final String givenStrategy, final AssignStrategy defaultStrategy) {
        notNull(defaultStrategy, "Default strategy cannot be null.");
        return stream(AssignStrategy.values())
                .filter(strategy -> endsWithIgnoreCase(strategy.getStrategyName(), givenStrategy))
                .findFirst()
                .orElse(defaultStrategy);
    }

    public String getStrategyName() {
        return strategyName;
    }

}
