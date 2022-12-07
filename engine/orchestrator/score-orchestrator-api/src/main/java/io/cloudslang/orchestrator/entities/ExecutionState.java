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
import io.cloudslang.score.facade.execution.ExecutionStatus;
import io.cloudslang.score.facade.execution.ExecutionSummary;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.hibernate.annotations.Type;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.Arrays;

/**
 * User:
 * Date: 12/05/2014
 */
@Entity
@Table(name = "OO_EXECUTION_STATE",
        uniqueConstraints = {@UniqueConstraint(name = "OO_EXECUTION_STATE_UC", columnNames = {"EXECUTION_ID", "BRANCH_ID"})})
public class ExecutionState extends AbstractIdentifiable {

    public static final String EMPTY_BRANCH = "EMPTY";

    @Column(name = "EXECUTION_ID", nullable = false)
    private Long executionId;

    @Column(name = "BRANCH_ID", nullable = false)
    private String branchId = ExecutionSummary.EMPTY_BRANCH;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "STATUS", nullable = false)
    private ExecutionStatus status;

    @Column(name = "EXECUTION_OBJECT")
    @Lob
    @Type(type = "org.hibernate.type.ImageType")
    @Basic(fetch = FetchType.LAZY)
    private byte[] executionObject;

    @Column(name = "UPDATE_TIME")
    private Long updateTime;

    public Long getExecutionId() {
        return executionId;
    }

    public void setExecutionId(Long executionId) {
        this.executionId = executionId;
    }

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    public ExecutionStatus getStatus() {
        return status;
    }

    public void setStatus(ExecutionStatus status) {
        this.status = status;
    }

    public byte[] getExecutionObject() {
        return executionObject;
    }

    public void setExecutionObject(byte[] executionObj) {
        this.executionObject = executionObj;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ExecutionState)) return false;

        ExecutionState that = (ExecutionState) obj;
        EqualsBuilder equalsBuilder = new EqualsBuilder();
        equalsBuilder.append(branchId, that.branchId);
        equalsBuilder.append(executionId, that.executionId);
        equalsBuilder.append(executionObject, that.executionObject);
        equalsBuilder.append(status, that.status);
        return equalsBuilder.isEquals();
    }

    @Override
    public int hashCode() {
        int result = executionId != null ? executionId.hashCode() : 0;
        result = 31 * result + (branchId != null ? branchId.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (executionObject != null ? Arrays.hashCode(executionObject) : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ExecutionState {" +
                "executionId='" + executionId + '\'' +
                ", branchId='" + branchId + '\'' +
                '}';
    }
}
