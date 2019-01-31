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
package io.cloudslang.runtime.impl.sequential;

import io.cloudslang.runtime.api.sequential.SequentialExecutionParametersProvider;
import io.cloudslang.runtime.api.sequential.SequentialExecutionService;

public class DefaultSequentialExecutionServiceImpl implements SequentialExecutionService {

    private static final String SEQ_OPS_NOT_SUPPORTED = "CloudSlang does not support" +
            " executing sequential operations. To provide this functionality, you must extend all necessary classes.";

    @Override
    public Object execute(String dependency, SequentialExecutionParametersProvider parametersProvider) {
        throw new UnsupportedOperationException(SEQ_OPS_NOT_SUPPORTED);
    }
}
