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

package io.cloudslang.score.facade.execution;

import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.Date;

public class ExecutionSummary implements Serializable {

    public static final String EMPTY_BRANCH = "EMPTY";

    private String executionId;
    private String branchId;
    private Date startTime;
    private Date endTime;
    private ExecutionStatus status;
    private String resultStatusType;
    private String resultStatusName;
    private PauseReason pauseReason;
    private CancelReason cancelReason;
    private String owner;
    private String triggeredBy;
    private String flowUuid;
    private String flowPath;
    private String executionName;
    private String triggeringSource;
    @SuppressWarnings("UnusedDeclaration")
    @Deprecated private long branchesCount; //not active since 10.02!! we don't set the value, but must leave it for backward compatible of the POJO in Careml.
    private Double roi;
    private boolean reserveRobot;

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
        // In the DB and ExecutionSummaryEntity we use "EMPTY" (EMPTY_BRANCH) for empty branchId.
        // But all the usages still use null. So, here we convert it from "EMPTY" to null, for the outside world.
        if (StringUtils.equalsIgnoreCase(branchId, EMPTY_BRANCH)) {
            this.branchId = null;
        } else {
            this.branchId = branchId;
        }
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public ExecutionStatus getStatus() {
        return status;
    }

    public void setStatus(ExecutionStatus status) {
        this.status = status;
    }

    public String getResultStatusType() {
        return resultStatusType;
    }

    public void setResultStatusType(String resultStatusType) {
        this.resultStatusType = resultStatusType;
    }

    public String getResultStatusName() {
        return resultStatusName;
    }

    public void setResultStatusName(String resultStatusName) {
        this.resultStatusName = resultStatusName;
    }

    public PauseReason getPauseReason() {
        return pauseReason;
    }

    @SuppressWarnings("UnusedDeclaration")
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

    public void setTriggeredBy(String triggeredBy) {
        this.triggeredBy = triggeredBy;
    }

    public String getTriggeringSource() {
        return triggeringSource;
    }

    public void setTriggeringSource(String triggeringSource) {
        this.triggeringSource = triggeringSource;
    }

    public String getFlowUuid() {
        return flowUuid;
    }

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

    public void setExecutionName(String executionName) {
        this.executionName = executionName;
    }

    @SuppressWarnings("UnusedDeclaration")
    @Deprecated
    //not active since 10.02!! we don't set the value, but must leave it for backward compatible of the POJO in Careml.
    public long getBranchesCount() {
        //noinspection deprecation
        return branchesCount;
    }

    @SuppressWarnings("UnusedDeclaration")
    public Double getRoi() {
        return roi;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setRoi(Double roi) {
        this.roi = roi;
    }

    public CancelReason getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(CancelReason cancelReason) {
        this.cancelReason = cancelReason;
    }

    public boolean isReserveRobot() {
        return reserveRobot;
    }

    public void setReserveRobot(boolean reserveRobot) {
        this.reserveRobot = reserveRobot;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExecutionSummary)) return false;

        ExecutionSummary that = (ExecutionSummary) o;

        if (branchId != null ? !branchId.equals(that.branchId) : that.branchId != null) return false;
        if (endTime != null ? !endTime.equals(that.endTime) : that.endTime != null) return false;
        if (executionId != null ? !executionId.equals(that.executionId) : that.executionId != null) return false;
        if (executionName != null ? !executionName.equals(that.executionName) : that.executionName != null)
            return false;
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
        if (roi != that.roi) return false;
        if (triggeredBy != null ? !triggeredBy.equals(that.triggeredBy) : that.triggeredBy != null) return false;
        if (triggeringSource != null ? !triggeringSource.equals(that.triggeringSource) : that.triggeringSource != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = executionId != null ? executionId.hashCode() : 0;
        result = 31 * result + (roi != null ? roi.hashCode() : 0);
        result = 31 * result + (branchId != null ? branchId.hashCode() : 0);
        result = 31 * result + (startTime != null ? startTime.hashCode() : 0);
        result = 31 * result + (endTime != null ? endTime.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (resultStatusType != null ? resultStatusType.hashCode() : 0);
        result = 31 * result + (resultStatusName != null ? resultStatusName.hashCode() : 0);
        result = 31 * result + (pauseReason != null ? pauseReason.hashCode() : 0);
        result = 31 * result + (owner != null ? owner.hashCode() : 0);
        result = 31 * result + (triggeredBy != null ? triggeredBy.hashCode() : 0);
        result = 31 * result + (triggeringSource != null ? triggeringSource.hashCode() : 0);
        result = 31 * result + (flowUuid != null ? flowUuid.hashCode() : 0);
        result = 31 * result + (flowPath != null ? flowPath.hashCode() : 0);
        result = 31 * result + (executionName != null ? executionName.hashCode() : 0);
        return result;
    }


}
