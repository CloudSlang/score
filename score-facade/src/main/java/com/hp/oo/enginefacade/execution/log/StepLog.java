package com.hp.oo.enginefacade.execution.log;

import com.hp.oo.internal.sdk.execution.RecordBoundInput;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.HashMap;
import java.util.List;

/**
 * User: zruya
 * Date: 25/12/12
 * Time: 17:30
 */
public class StepLog {
    private StepInfo stepInfo;
    private StepTransitionLog stepTransitionLog;
    private String description;
    private String stepPrimaryResult;
    private String operationGroup;
    private List<StepErrorLog> errorList;
    private List<RecordBoundInput> stepInputs;
    private HashMap<String, String> stepResult;
    private HashMap<String, String> extraData;

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
                .toHashCode();
    }
}
