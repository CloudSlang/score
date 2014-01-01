package com.hp.oo.orchestrator.entities;

import com.hp.oo.internal.sdk.execution.Execution;
import com.hp.score.engine.data.AbstractIdentifiable;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kravtsov
 * Date: 10/09/13
 * Time: 09:49
 */


@Entity
@Table(name = "OO_SUSPENDED_EXECUTIONS")
public class SuspendedExecution extends AbstractIdentifiable {

    @Column(name = "EXECUTION_ID", nullable = false)
    private String executionId;

    @Column(name = "SPLIT_ID", nullable = false, unique = true)
    private String splitId;

    @Column(name= "NUMBER_OF_BRANCHES", nullable = false)
    private Integer numberOfBranches;

    @Basic(fetch = FetchType.LAZY)
    @Embedded
    private ExecutionObjEntity executionObj;

    @OneToMany(cascade = {CascadeType.ALL}, orphanRemoval = true, fetch = FetchType.LAZY, mappedBy="suspendedExecution")
    private List<FinishedBranch> finishedBranches = new ArrayList<>();

    private SuspendedExecution() {
    }

    public SuspendedExecution(String executionId, String splitId, Integer numberOfBranches, Execution executionObj) {
        this.executionId = executionId;
        this.splitId = splitId;
        this.numberOfBranches = numberOfBranches;
        this.executionObj = new ExecutionObjEntity(executionObj);
    }

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public String getSplitId() {
        return splitId;
    }

    public void setSplitId(String splitId) {
        this.splitId = splitId;
    }

    public Integer getNumberOfBranches() {
        return numberOfBranches;
    }

    public void setNumberOfBranches(Integer numberOfBranches) {
        this.numberOfBranches = numberOfBranches;
    }

    public Execution getExecutionObj() {
        if  (executionObj == null)
            return null;
        else
            return executionObj.getExecutionObj();
    }

    public void setExecutionObj(Execution executionObj) {
        this.executionObj = new ExecutionObjEntity(executionObj);
    }

    public List<FinishedBranch> getFinishedBranches() {
        return finishedBranches;
    }

    public void setFinishedBranches(List<FinishedBranch> finishedBranches) {
        this.finishedBranches = finishedBranches;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SuspendedExecution)) return false;

        SuspendedExecution that = (SuspendedExecution) o;

        if (!executionId.equals(that.executionId)) return false;
        if (!numberOfBranches.equals(that.numberOfBranches)) return false;
        if (!splitId.equals(that.splitId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = executionId.hashCode();
        result = 31 * result + splitId.hashCode();
        result = 31 * result + numberOfBranches.hashCode();
        return result;
    }
}
