package com.hp.oo.orchestrator.entities;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BranchContexts implements Serializable {
    private boolean isBranchCancelled;
    private final Map<String, Serializable> contexts;
    private final Map<String, Serializable> systemContext;

    public BranchContexts(boolean isBranchCancelled, Map<String, Serializable> contexts, Map<String, Serializable> systemContext) {
        Validate.notNull(contexts);
        Validate.notNull(systemContext);

        this.isBranchCancelled = isBranchCancelled;
        this.contexts = new HashMap<>(contexts);
        this.systemContext = new HashMap<>(systemContext);
    }

    public boolean isBranchCancelled() {
        return isBranchCancelled;
    }

    public void setBranchCancelled(boolean branchCancelled) {
        isBranchCancelled = branchCancelled;
    }

    public Map<String, Serializable> getContexts() {
        return Collections.unmodifiableMap(contexts);
    }

    public Map<String, Serializable> getSystemContext() {
        return Collections.unmodifiableMap(systemContext);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BranchContexts)) return false;

        BranchContexts that = (BranchContexts) o;

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
