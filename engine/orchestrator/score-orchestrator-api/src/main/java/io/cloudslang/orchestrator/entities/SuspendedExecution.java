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

import io.cloudslang.orchestrator.enums.SuspendedExecutionReason;
import io.cloudslang.score.facade.entities.Execution;
import io.cloudslang.engine.data.AbstractIdentifiable;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Column;
import javax.persistence.Enumerated;
import javax.persistence.Basic;
import javax.persistence.Embedded;
import javax.persistence.OneToMany;
import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import java.util.HashSet;
import java.util.Set;

import static javax.persistence.EnumType.STRING;

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

    @Enumerated(STRING)
    @Column(name = "SUSPENSION_REASON", nullable = false)
    private SuspendedExecutionReason suspensionReason;

    @Column(name = "MERGED_BRANCHES", nullable = false)
    private long mergedBranches;

    @Column(name = "LOCKED", nullable = false)
    private boolean locked;

    @Basic(fetch = FetchType.LAZY)
    @Embedded
    private ExecutionObjEntity executionObj;

    @OneToMany(cascade = {CascadeType.ALL}, orphanRemoval = true, fetch = FetchType.LAZY, mappedBy="suspendedExecution")
    private Set<FinishedBranch> finishedBranches = new HashSet<>();

    private SuspendedExecution() {
    }

    public SuspendedExecution(String executionId,
                              String splitId,
                              Integer numberOfBranches,
                              Execution executionObj,
                              SuspendedExecutionReason suspensionReason,
                              boolean locked) {
        this.executionId = executionId;
        this.splitId = splitId;
        this.numberOfBranches = numberOfBranches;
        this.executionObj = new ExecutionObjEntity(executionObj);
        this.suspensionReason = suspensionReason;
        this.locked = locked;
        mergedBranches = 0;
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

    public Set<FinishedBranch> getFinishedBranches() {
        return finishedBranches;
    }

    public void setFinishedBranches(Set<FinishedBranch> finishedBranches) {
        this.finishedBranches = finishedBranches;
    }

    public SuspendedExecutionReason getSuspensionReason() {
        return suspensionReason;
    }

    public void setSuspensionReason(SuspendedExecutionReason suspensionReason) {
        this.suspensionReason = suspensionReason;
    }

    public long getMergedBranches() {
        return mergedBranches;
    }

    public void setMergedBranches(long mergedBranches) {
        this.mergedBranches = mergedBranches;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SuspendedExecution)) return false;

        SuspendedExecution that = (SuspendedExecution) o;

        if (!executionId.equals(that.executionId)) return false;
        if (!numberOfBranches.equals(that.numberOfBranches)) return false;
        if (!splitId.equals(that.splitId)) return false;
        if (!suspensionReason.equals(that.suspensionReason)) return false;
        if (mergedBranches != that.mergedBranches) return false;
        if (locked != that.locked) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = executionId.hashCode();
        result = 31 * result + splitId.hashCode();
        result = 31 * result + numberOfBranches.hashCode();
        result = 31 * result + suspensionReason.hashCode();
        return result;
    }
}
