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
package io.cloudslang.score.api;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Map;

public class StatefulSessionStack implements Serializable {

    private static final long serialVersionUID = -7408054784258769720L;

    private ArrayDeque<Map<String, StatefulQueue>> stack;

    public StatefulSessionStack() {
        stack = new ArrayDeque<>();
    }

    public void pushSessionsMap(Map<String, StatefulQueue> newContext) {
        stack.push(newContext);
    }

    public Map<String, StatefulQueue> popSessionMap() {
        if (stack.isEmpty()) {
            return null;
        }
        return stack.pop();
    }

    public Map<String, StatefulQueue> peakSessionMap() {
        return stack.peek();
    }

    public boolean isEmpty() {
        return stack.isEmpty();
    }

    public int size() {
        return stack.size();
    }
}
