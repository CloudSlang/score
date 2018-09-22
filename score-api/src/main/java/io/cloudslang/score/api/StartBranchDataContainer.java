/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package io.cloudslang.score.api;

import io.cloudslang.score.lang.SystemContext;
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
public class StartBranchDataContainer implements Serializable{
    private final Long startPosition;
    private final Long executionPlanId;
    private final Map<String, Serializable> contexts;
    private final SystemContext systemContext;

    public StartBranchDataContainer(Long startPosition, Long executionPlanId, Map<String, Serializable> localContexts, SystemContext systemContext) {
        Validate.notNull(startPosition);
        Validate.notNull(executionPlanId);
        Validate.notNull(localContexts);
        Validate.notNull(systemContext);

        this.startPosition = startPosition;
        this.executionPlanId = executionPlanId;
        this.systemContext = systemContext;
        this.contexts = new HashMap<>(localContexts);
    }

    public Long getStartPosition() {
        return startPosition;
    }

    public Long getExecutionPlanId() {
        return executionPlanId;
    }

    public Map<String, Serializable> getContexts() {
        return Collections.unmodifiableMap(contexts);
    }

    public SystemContext getSystemContext() {
        return systemContext;
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
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(this.startPosition)
                .append(this.executionPlanId)
                .toHashCode();
    }
}
