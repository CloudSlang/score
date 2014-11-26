/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package org.eclipse.score.api;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.io.Serializable;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A POJO which serves as an holder for the contexts and exception (if exists) of a finished branch
 */
public class EndBranchDataContainer implements Serializable {
    private final Map<String, Serializable> contexts;
    private final Map<String, Serializable> systemContext;
    private final String exception;

    public EndBranchDataContainer(Map<String, Serializable> contexts, Map<String, Serializable> systemContext, String exception) {
        Validate.notNull(contexts);
        Validate.notNull(systemContext);

        this.contexts = new HashMap<>(contexts);
        this.systemContext = new HashMap<>(systemContext);
        this.exception = exception;
    }

    public Map<String, Serializable> getContexts() {
        return Collections.unmodifiableMap(contexts);
    }

    public Map<String, Serializable> getSystemContext() {
        return Collections.unmodifiableMap(systemContext);
    }

    public String getException() {
        return exception;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof EndBranchDataContainer))
            return false;

        EndBranchDataContainer that = (EndBranchDataContainer) o;

        return new EqualsBuilder()
                .append(this.contexts, that.contexts)
                .append(this.systemContext, that.systemContext)
                .append(this.exception, that.exception)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(this.contexts)
                .append(this.systemContext)
                .append(this.exception)
                .toHashCode();
    }
}
