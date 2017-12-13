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

package io.cloudslang.worker.execution.reflection;

import io.cloudslang.score.api.ControlActionMetadata;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: kravtsov
 * Date: 09/11/11
 * Time: 11:49
 *
 * An adapter that handles executing control actions in reflection
 *
 */
public interface ReflectionAdapter {

    /**
     *
     * Handle execution a control action in reflection
     *
     * @param actionMetadata the control action metadata
     * @param actionData the data to pass to the control action
     * @return tan Object of the invocation result
     */
    public Object executeControlAction(ControlActionMetadata actionMetadata, Map<String, ?> actionData);
}
