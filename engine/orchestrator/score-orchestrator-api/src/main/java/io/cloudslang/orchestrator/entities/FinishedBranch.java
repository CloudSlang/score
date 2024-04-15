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

import io.cloudslang.engine.data.AbstractIdentifiable;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Type;

/**
 * Created with IntelliJ IDEA.
 * User: kravtsov
 * Date: 10/09/13
 * Time: 09:49
 */
@Entity
@Immutable
@Table(name = "OO_FINISHED_BRANCHES")
public class FinishedBranch extends AbstractIdentifiable {

    @Column(name = "EXECUTION_ID", nullable = false, updatable = false)
    private String executionId;

    @Column(name = "BRANCH_ID", nullable = false, updatable = false)
    private String branchId;

    @Column(name = "SPLIT_ID", nullable = false, updatable = false)
    private String splitId;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "BRANCH_EXCEPTION", updatable = false)
    private String branchException;

    @Column(name = "BRANCH_CONTEXT", nullable = false, updatable = false)
    @Lob
    @Type(value = io.cloudslang.orchestrator.entities.BranchContextByteaTypeDescriptor.class)
    private BranchContexts branchContexts;

    @ManyToOne
    @JoinColumn(name = "SUSPENDED_EXECUTION_ID", nullable = false, updatable = false)
    private SuspendedExecution suspendedExecution;

    public FinishedBranch() {
    }

    public FinishedBranch(String executionId, String branchId, String splitId, String branchException, BranchContexts branchContexts) {
        this.executionId = executionId;
        this.branchId = branchId;
        this.splitId = splitId;
        this.branchException = branchException;
        this.branchContexts = branchContexts;
    }

    public boolean connectToSuspendedExecution(SuspendedExecution suspendedExecution) {
        this.suspendedExecution = suspendedExecution;
        return suspendedExecution.getFinishedBranches().add(this); //bi directional connection
    }

    public SuspendedExecution getSuspendedExecution() {
        return suspendedExecution;
    }

    public String getExecutionId() {
        return executionId;
    }

    public String getBranchId() {
        return branchId;
    }

    public String getSplitId() {
        return splitId;
    }

    public String getBranchException() {
        return branchException;
    }

    public BranchContexts getBranchContexts() {
        return this.branchContexts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FinishedBranch that = (FinishedBranch) o;

        return new EqualsBuilder()
                .append(this.branchId, that.branchId)
                .append(this.executionId, that.executionId)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(this.branchId)
                .append(this.executionId)
                .toHashCode();
    }
}
