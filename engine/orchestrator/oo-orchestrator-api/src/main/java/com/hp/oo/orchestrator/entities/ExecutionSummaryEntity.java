package com.hp.oo.orchestrator.entities;

import com.hp.oo.enginefacade.execution.ExecutionEnums;
import com.hp.oo.enginefacade.execution.ExecutionSummary;
import com.hp.oo.enginefacade.execution.PauseReason;
import com.hp.score.engine.data.AbstractIdentifiable;
import org.hibernate.annotations.Filter;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Arrays;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: kravtsov
 * Date: 20/12/12
 * Time: 11:43
 */

@Entity
@Table(name = "OO_EXECUTION_SUMMARY")
@Filter(name = "viewFilter", condition = "(FLOW_UUID IN (SELECT E.RELATED_ENTITY_UUID FROM OO_ENTITLEMENTS E INNER JOIN OO_ENTITLEMENTS_PRIVILEGES PRIV ON (E.ID = PRIV.ENTITLEMENT_ID) WHERE PRIV.PRIVILEGE_NAME = :viewPrivilege AND E.ROLE_ID IN (:roleIds)))")
public class ExecutionSummaryEntity extends AbstractIdentifiable {

    @Column(name = "EXECUTION_ID", nullable = false)
    private String executionId;

    @Column(name = "BRANCH_ID", nullable = false)
    private String branchId = ExecutionSummary.EMPTY_BRANCH;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "STATUS", nullable = false)
    private ExecutionEnums.ExecutionStatus status;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "START_TIME", nullable = false)
    private Date startTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "END_TIME")
    private Date endTime;

    @Column(name = "FLOW_UUID", nullable = false)
    private String flowUuid;

    @Column(name = "FLOW_PATH", nullable = false)
    private String flowPath;

    @Column(name = "RESULT_STATUS_TYPE")
    private String resultStatusType;

    @Column(name = "RESULT_STATUS_NAME")
    private String resultStatusName;

    @Column(name = "ROI")
    private Double roi;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "PAUSE_REASON")
    private PauseReason pauseReason;

    @Column(name = "OWNER", nullable = false)
    private String owner;

    @Column(name = "TRIGGERED_BY", nullable = false)
    private String triggeredBy;

    @Column(name = "EXECUTION_OBJECT")
    @Lob
    @Basic(fetch = FetchType.LAZY)
    private byte[] executionObj;

    @Column(name = "EXECUTION_NAME")
    private String executionName;

    @Column(name = "DURATION")
    private Long duration;

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    public Date getStartTime() {
        return startTime;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public ExecutionEnums.ExecutionStatus getStatus() {
        return status;
    }

    public void setStatus(ExecutionEnums.ExecutionStatus status) {
        this.status = status;
    }

    public byte[] getExecutionObj() {
        return executionObj;
    }

    public void setExecutionObj(byte[] executionObj) {
        this.executionObj = executionObj;
    }

    public String getResultStatusType() {
        return resultStatusType;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setResultStatusType(String resultStatusType) {
        this.resultStatusType = resultStatusType;
    }

    public String getResultStatusName() {
        return resultStatusName;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setResultStatusName(String resultStatusName) {
        this.resultStatusName = resultStatusName;
    }

    public PauseReason getPauseReason() {
        return pauseReason;
    }

    public void setPauseReason(PauseReason pauseReason) {
        this.pauseReason = pauseReason;
    }

    public String getOwner() {
        return owner;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getTriggeredBy() {
        return triggeredBy;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setTriggeredBy(String triggeredBy) {
        this.triggeredBy = triggeredBy;
    }

    public String getFlowUuid() {
        return flowUuid;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setFlowUuid(String flowUuid) {
        this.flowUuid = flowUuid;
    }

    public String getFlowPath() {
        return flowPath;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setFlowPath(String flowPath) {
        this.flowPath = flowPath;
    }

    public String getExecutionName() {
        return executionName;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setExecutionName(String executionName) {
        this.executionName = executionName;
    }

    @SuppressWarnings("UnusedDeclaration")
    public Double getRoi() {
        return roi;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setRoi(Double roi) {
        this.roi = roi;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExecutionSummaryEntity)) return false;

        ExecutionSummaryEntity that = (ExecutionSummaryEntity) o;

        if (branchId != null ? !branchId.equals(that.branchId) : that.branchId != null) return false;
        if (endTime != null ? !endTime.equals(that.endTime) : that.endTime != null) return false;
        if (executionId != null ? !executionId.equals(that.executionId) : that.executionId != null) return false;
        if (!Arrays.equals(executionObj, that.executionObj)) return false;
        if (flowPath != null ? !flowPath.equals(that.flowPath) : that.flowPath != null) return false;
        if (flowUuid != null ? !flowUuid.equals(that.flowUuid) : that.flowUuid != null) return false;
        if (owner != null ? !owner.equals(that.owner) : that.owner != null) return false;
        if (pauseReason != that.pauseReason) return false;
        if (resultStatusName != null ? !resultStatusName.equals(that.resultStatusName) : that.resultStatusName != null)
            return false;
        if (resultStatusType != null ? !resultStatusType.equals(that.resultStatusType) : that.resultStatusType != null)
            return false;
        if (startTime != null ? !startTime.equals(that.startTime) : that.startTime != null) return false;
        if (status != that.status) return false;
        if (executionName != null ? !executionName.equals(that.executionName) : that.executionName != null) return false;
        //noinspection RedundantIfStatement
        if (triggeredBy != null ? !triggeredBy.equals(that.triggeredBy) : that.triggeredBy != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = executionId != null ? executionId.hashCode() : 0;
        result = 31 * result + (branchId != null ? branchId.hashCode() : 0);
        result = 31 * result + (startTime != null ? startTime.hashCode() : 0);
        result = 31 * result + (endTime != null ? endTime.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (pauseReason != null ? pauseReason.hashCode() : 0);
        result = 31 * result + (executionObj != null ? Arrays.hashCode(executionObj) : 0);
        result = 31 * result + (resultStatusType != null ? resultStatusType.hashCode() : 0);
        result = 31 * result + (resultStatusName != null ? resultStatusName.hashCode() : 0);
        result = 31 * result + (owner != null ? owner.hashCode() : 0);
        result = 31 * result + (triggeredBy != null ? triggeredBy.hashCode() : 0);
        result = 31 * result + (flowUuid != null ? flowUuid.hashCode() : 0);
        result = 31 * result + (flowPath != null ? flowPath.hashCode() : 0);
        return result;
    }
}
