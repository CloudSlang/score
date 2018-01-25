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

package io.cloudslang.score.facade.entities;

import io.cloudslang.score.api.ExecutionPlan;
import io.cloudslang.engine.data.AbstractIdentifiable;
import org.apache.commons.lang.SerializationUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * Created by IntelliJ IDEA.
 * User: lernery
 * Date: 11/23/11
 * Time: 9:53 AM
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Table(name = "OO_RUNNING_EXECUTION_PLANS")
public class RunningExecutionPlan extends AbstractIdentifiable {
    private static final long serialVersionUID = 3465194293828514413L;

    @Lob
    @Column(name = "EXECUTION_PLAN_ZIPPED")
    @Basic(fetch = FetchType.LAZY)
    private byte[] executionPlanZipped;

    @Transient
    private ExecutionPlan executionPlan;

    @Column(name = "UUID", nullable = false)
    private String flowUUID;

    @Column(name = "IN_USE", nullable = true)
    private Long inUseCount;

    public byte[] getExecutionPlanZipped() {
        return executionPlanZipped;
    }

    public void setExecutionPlanZipped(byte[] executionPlanZipped) {
        this.executionPlanZipped = executionPlanZipped;
    }

    public ExecutionPlan getExecutionPlan() {
        if (executionPlan == null) {
            executionPlan = ExecutionPlanCompressUtil.getExecutionPlanFromBytes(executionPlanZipped);
        }
        return executionPlan;
    }

    public void setExecutionPlan(ExecutionPlan executionPlan) {

        this.executionPlan = (ExecutionPlan) SerializationUtils.clone(executionPlan);
        executionPlanZipped = ExecutionPlanCompressUtil.getBytesFromExecutionPlan(this.executionPlan);
    }

    public String getFlowUUID() {
        return flowUUID;
    }

    public void setFlowUUID(String flowUUID) {
        this.flowUUID = flowUUID;
    }

    public Long getInUseCount() {
        return inUseCount;
    }

    public void setInUseCount(Long inUseCount) {
        this.inUseCount = inUseCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        RunningExecutionPlan that = (RunningExecutionPlan) o;

        if (id != null ? !id.equals(that.id) : that.id != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    //done for manual creation of the object
    public void setId(Long id) {
        this.id = id;
    }
}
