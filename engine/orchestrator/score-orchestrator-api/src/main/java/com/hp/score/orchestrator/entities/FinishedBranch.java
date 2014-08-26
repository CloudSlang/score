package com.hp.score.orchestrator.entities;

import com.hp.score.engine.data.AbstractIdentifiable;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.annotations.Immutable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

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
    private BranchContexts branchContexts;

    @ManyToOne
    @JoinColumn(name="SUSPENDED_EXECUTION_ID", nullable = false, updatable = false)
    private SuspendedExecution suspendedExecution;

    public FinishedBranch() {}

    public FinishedBranch(String executionId, String branchId, String splitId, String branchException, BranchContexts branchContexts) {
        this.executionId = executionId;
        this.branchId = branchId;
        this.splitId = splitId;
        this.branchException = branchException;
        this.branchContexts = branchContexts;
    }

    public void connectToSuspendedExecution(SuspendedExecution suspendedExecution) {
        this.suspendedExecution = suspendedExecution;
        suspendedExecution.getFinishedBranches().add(this); //bi directional connection
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
