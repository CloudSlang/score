package com.hp.oo.enginefacade.execution;

import com.hp.oo.internal.sdk.execution.OOContext;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A POJO containing all the data necessary to create a new branch
 */
public class StartBranchDataContainer {
    private final Long startPosition;
    private final Long executionPlanId;
    private final Map<String, OOContext> contexts;
    private final Map<String, Serializable> systemContext;

    public StartBranchDataContainer(Long startPosition, Long executionPlanId, Map<String, OOContext> contexts, Map<String, Serializable> systemContext) {
        Validate.notNull(startPosition);
        Validate.notNull(executionPlanId);
        Validate.notNull(contexts);
        Validate.notNull(systemContext);

        this.startPosition = startPosition;
        this.executionPlanId = executionPlanId;
        this.systemContext = new HashMap<>(systemContext);
        this.contexts = new HashMap<>();

        for (String name : contexts.keySet()) {
            this.contexts.put(name, new OOContext(contexts.get(name)));
        }
    }

    public Long getStartPosition() {
        return startPosition;
    }

    public Long getExecutionPlanId() {
        return executionPlanId;
    }

    public Map<String, OOContext> getContexts() {
        return Collections.unmodifiableMap(contexts);
    }

    public Map<String, Serializable> getSystemContext() {
        return Collections.unmodifiableMap(systemContext);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StartBranchDataContainer)) return false;

        StartBranchDataContainer that = (StartBranchDataContainer) o;

        return new EqualsBuilder()
                .append(this.startPosition, that.startPosition)
                .append(this.executionPlanId, that.executionPlanId)
                .append(this.contexts, that.contexts)
                .append(this.systemContext, that.systemContext)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(this.startPosition)
                .append(this.executionPlanId)
                .append(this.contexts)
                .append(this.systemContext)
                .toHashCode();
    }
}
