/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

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

    @Column(name = "EXECUTION_ID")
    private String executionId;

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

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
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
