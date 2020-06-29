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
package io.cloudslang.worker.execution.model;


import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public class StepActionDataHolder {

    protected final List<Map<String, ?>> holder;

    public StepActionDataHolder() {
        this.holder = new ArrayList<>(4);
    }

    // Used by subclasses only
    private StepActionDataHolder(StepActionDataHolder stepActionDataHolder) {
        this.holder = stepActionDataHolder.holder;
    }

    public void addNotNullPartToHolder(Map<String, ?> actionDataPart) {
        this.holder.add(actionDataPart);
    }

    public void addNullablePartToHolder(Map<String, ?> actionDataPart) {
        if (actionDataPart != null) {
            this.holder.add(actionDataPart);
        }
    }

    public static class ReadonlyStepActionDataAccessor extends StepActionDataHolder {

        public ReadonlyStepActionDataAccessor(StepActionDataHolder stepActionDataHolder) {
            super(stepActionDataHolder);
        }

        public void addNotNullPartToHolder(Map<String, ?> actionDataPart) {
            throw new UnsupportedOperationException("Cannot mutate ReadonlyStepActionDataAccessor");
        }

        public void addNullablePartToHolder(Map<String, ?> actionDataPart) {
            throw new UnsupportedOperationException("Cannot mutate ReadonlyStepActionDataAccessor");
        }

        public Object getValue(String key) {
            ListIterator<Map<String, ?>> listIterator = this.holder.listIterator(this.holder.size());
            while (listIterator.hasPrevious()) {
                Map<String, ?> element = listIterator.previous();
                if (element.containsKey(key)) {
                    return element.get(key);
                }
            }
            return null;
        }
    }
}
