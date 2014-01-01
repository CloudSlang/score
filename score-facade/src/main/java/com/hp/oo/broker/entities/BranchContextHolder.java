package com.hp.oo.broker.entities;

import com.hp.oo.internal.sdk.execution.OOContext;
import com.hp.score.engine.data.AbstractIdentifiable;
import org.hibernate.annotations.Type;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.Table;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: lernery
 * Date: 1/24/12
 * Time: 1:31 PM
 */
@Entity
@Table(name = "OO_BRANCH_CONTEXTS")
public class BranchContextHolder extends AbstractIdentifiable {
    private static final long serialVersionUID = -8585221621351683133L;

    @Type(type = "com.hp.oo.broker.entities.HibernateHashMapUserType")
    @Column(name = "BRANCH_CONTEXT", nullable = false)
    private Map<String, OOContext> context;

    @Column(name = "SPLIT_ID", nullable = false)
    private String splitId;

    @Column(name = "BRANCH_ID", nullable = false)
      private String branchId;

    @Column(name = "EXECUTION_ID", nullable = false)
    private String executionId;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "BRANCH_EXCEPTION")
    private String branchException;

    public Map<String, OOContext> getContext() {
        return context;
    }

    public void setContext(Map<String, OOContext> context) {
        this.context = context;
    }

    public String getSplitId() {
        return splitId;
    }

    public void setSplitId(String splitId) {
        this.splitId = splitId;
    }

    public String getBranchException() {
        return branchException;
    }

    public void setBranchException(String branchException) {
        this.branchException = branchException;
    }

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BranchContextHolder that = (BranchContextHolder) o;

        if (executionId != null ? !executionId.equals(that.executionId) : that.executionId != null) return false;
        if (branchId != null ? !branchId.equals(that.branchId) : that.branchId != null) return false;
        if (splitId != null ? !splitId.equals(that.splitId) : that.splitId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = splitId != null ? splitId.hashCode() : 0;
        result = 31 * result + (branchId != null ? branchId.hashCode() : 0);
        result = 31 * result + (executionId != null ? executionId.hashCode() : 0);
        return result;
    }
}
