package com.hp.oo.enginefacade.execution.log;

import com.hp.oo.enginefacade.execution.ExecutionEnums;
import com.hp.oo.internal.sdk.execution.RecordBoundInput;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

/**
 * User: zruya
 * Date: 25/12/12
 * Time: 17:30
 */
public class StepLog implements Serializable {


    private static final long serialVersionUID = -3195339762640416295L;

    private StepInfo stepInfo;
    private StepTransitionLog stepTransitionLog;
    private String description;
    private String stepPrimaryResult;
    private String operationGroup;
    private List<StepErrorLog> errorList;
    private List<RecordBoundInput> stepInputs;
    private HashMap<String, String> stepResult;
    private HashMap<String, String> extraData;

    private String executionId;
    private ExecutionEnums.StepStatus status;
    private String workerId;
    private String user;
    private ExecutionEnums.StepLogCategory stepLogCategory;


    public List<StepErrorLog> getErrorList() {
        return errorList;
    }

    public void setErrorList(List<StepErrorLog> errorList) {
        this.errorList = errorList;
    }

    public StepInfo getStepInfo() {
        return stepInfo;
    }

    public void setStepInfo(StepInfo stepInfo) {
        this.stepInfo = stepInfo;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public HashMap<String, String> getExtraData() {
        return extraData;
    }

    public void setExtraData(HashMap<String, String> extraData) {
        this.extraData = extraData;
    }

    public List<RecordBoundInput> getStepInputs() {
        return stepInputs;
    }

    public void setStepInputs(List<RecordBoundInput> stepInputs) {
        this.stepInputs = stepInputs;
    }

    public HashMap<String, String> getStepResult() {
        return stepResult;
    }

    public void setStepResult(HashMap<String, String> stepResult) {
        this.stepResult = stepResult;
    }

    public String getStepPrimaryResult() {
        return stepPrimaryResult;
    }

    public void setStepPrimaryResult(String stepPrimaryResult) {
        this.stepPrimaryResult = stepPrimaryResult;
    }

    public String getOperationGroup() {
        return operationGroup;
    }

    public void setOperationGroup(String operationGroup) {
        this.operationGroup = operationGroup;
    }


    public StepTransitionLog getStepTransitionLog() {
        return stepTransitionLog;
    }

    public void setStepTransitionLog(StepTransitionLog stepTransitionLog) {
        this.stepTransitionLog = stepTransitionLog;
    }

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public String getWorkerId() {
        return workerId;
    }

    public void setWorkerId(String workerId) {
        this.workerId = workerId;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public ExecutionEnums.StepStatus getStatus() {
        return status;
    }

    public void setStatus(ExecutionEnums.StepStatus status) {
        this.status = status;
    }

    public ExecutionEnums.StepLogCategory getStepLogCategory() {
        return stepLogCategory;
    }

    public void setStepLogCategory(ExecutionEnums.StepLogCategory stepLogCategory) {
        this.stepLogCategory = stepLogCategory;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StepInfo)) return false;

        StepLog that = (StepLog) o;

        return new EqualsBuilder()
                .append(this.stepInfo, that.stepInfo)
                .append(this.stepInputs, that.stepInputs)
                .append(this.stepResult, that.stepResult)
                .append(this.description, that.description)
                .append(this.stepTransitionLog, that.stepTransitionLog)
                .append(this.extraData, that.extraData)
                .append(this.stepPrimaryResult, that.stepPrimaryResult)
                .append(this.errorList, that.errorList)
                .append(this.user, that.user)
                .append(this.workerId, that.workerId)
                .append(this.executionId, that.executionId)
                .append(this.status, that.status)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(this.stepInfo)
                .append(this.stepInputs)
                .append(this.description)
                .append(this.stepTransitionLog)
                .append(this.extraData)
                .append(this.stepResult)
                .append(this.stepPrimaryResult)
                .append(this.errorList)
                .append(this.user)
                .append(this.workerId)
                .append(this.executionId)
                .append(this.status)
                .toHashCode();
    }
}
