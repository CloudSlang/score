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
package io.cloudslang.orchestrator.entities;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public record BranchContexts(boolean isBranchCancelled, Map<String, Serializable> contexts,
                             Map<String, Serializable> systemContext) implements Serializable {
    public BranchContexts(boolean isBranchCancelled, Map<String, Serializable> contexts, Map<String, Serializable> systemContext) {
        Validate.notNull(contexts);
        Validate.notNull(systemContext);

        this.isBranchCancelled = isBranchCancelled;
        this.contexts = new HashMap<>(contexts);
        this.systemContext = new HashMap<>(systemContext);
    }

    @Override
    public Map<String, Serializable> contexts() {
        return Collections.unmodifiableMap(contexts);
    }

    @Override
    public Map<String, Serializable> systemContext() {
        return Collections.unmodifiableMap(systemContext);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BranchContexts that)) return false;

        return new EqualsBuilder()
                .append(this.contexts, that.contexts)
                .append(this.systemContext, that.systemContext)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(this.contexts)
                .append(this.systemContext)
                .toHashCode();
    }
}
