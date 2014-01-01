package com.hp.oo.broker.entities;

import com.hp.oo.internal.sdk.execution.ExecutionPlan;
import com.hp.score.engine.data.AbstractIdentifiable;
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


    public byte[] getExecutionPlanZipped() {
        return executionPlanZipped;
    }

    public void setExecutionPlanZipped(byte[] executionPlanZipped) {
        this.executionPlanZipped = executionPlanZipped;
    }

    public ExecutionPlan getExecutionPlan() {
        if(executionPlan == null){
            executionPlan = ExecutionPlanCompressUtil.getExecutionPlanFromBytes(executionPlanZipped);
        }
        return executionPlan;
    }

    public void setExecutionPlan(ExecutionPlan executionPlan) {
        this.executionPlan = executionPlan;
        executionPlanZipped = ExecutionPlanCompressUtil.getBytesFromExecutionPlan(executionPlan);
    }

    public String getFlowUUID() {
        return flowUUID;
    }

    public void setFlowUUID(String flowUUID) {
        this.flowUUID = flowUUID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RunningExecutionPlan that = (RunningExecutionPlan) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    //done for manual creation of the object
    public void setId(Long id){
        this.id = id;
    }
}
