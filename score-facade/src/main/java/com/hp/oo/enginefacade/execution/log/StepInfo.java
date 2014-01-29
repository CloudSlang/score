package com.hp.oo.enginefacade.execution.log;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.Date;

/**
 * User: zruya
 * Date: 25/12/12
 * Time: 17:30
 */
public class StepInfo implements Serializable{
    private String stepId;
    private String stepName;
    private String path;
    private String responseType;
    private Date startTime;
    private Date endTime;
    private boolean paused;
    private String orderNumber;
    private String flowName;
    private String flowId;
    private String type = "OTHER";

    public String getResponseType() {
        return responseType;
    }

    public void setResponseType(String responseType) {
        this.responseType = responseType;
    }

    public String getStepId() {
        return stepId;
    }

    public void setStepId(String stepId) {
        this.stepId = stepId;
    }

    public String getStepName() {
        return stepName;
    }

    public void setStepName(String stepName) {
        this.stepName = stepName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getFlowName() {
        return flowName;
    }

    public void setFlowName(String flowName) {
        this.flowName = flowName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    public String getFlowId() {
        return flowId;
    }

    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StepInfo)) return false;

        StepInfo that = (StepInfo) o;

        return new EqualsBuilder()
                .append(this.stepName, that.stepName)
                .append(this.path, that.path)
                .append(this.responseType, that.responseType)
                .append(this.startTime, that.startTime)
                .append(this.endTime, that.endTime)
                .append(this.stepId, that.stepId)
                .append(this.paused, that.paused)
                .append(this.orderNumber, that.orderNumber)
                .append(this.flowName, that.flowName)
                .append(this.flowId, that.flowId)
                .append(this.type, that.type)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(this.path)
                .append(this.stepId)
                .append(this.startTime)
                .append(this.endTime)
                .append(this.responseType)
                .append(this.stepName)
                .append(this.paused)
                .append(this.orderNumber)
                .append(this.flowName)
                .append(this.flowId)
                .append(this.type)
                .toHashCode();
    }
}
