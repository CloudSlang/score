package com.hp.oo.internal.sdk.execution;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: Meir Wahnon
 * Date: 11/21/11
 * Time: 5:49 PM
 */
public class FlowInput implements Serializable {

    private static final long serialVersionUID = 7751059910414410919L;

	protected String uuid;

    protected boolean isMandatory;

    protected boolean isEncrypted;

    protected String userMsg;

    protected String inputName;

    protected boolean isMultiValue;

    protected String valueDelimiter;

    protected Object sourceId;

    protected Object validationId;

    protected String defaultValue;



    public FlowInput() {
    }


    public boolean isMandatory() {
        return isMandatory;
    }

    public void setMandatory(boolean mandatory) {
        isMandatory = mandatory;
    }

    public boolean isEncrypted() {
        return isEncrypted;
    }

    public void setEncrypted(boolean encrypted) {
        isEncrypted = encrypted;
    }

    public String getUserMsg() {
        return userMsg;
    }

    public void setUserMsg(String userMsg) {
        this.userMsg = userMsg;
    }

    public String getInputName() {
        return inputName;
    }

    public void setInputName(String inputName) {
        this.inputName = inputName;
    }

    public boolean isMultiValue() {
        return isMultiValue;
    }

    public void setMultiValue(boolean multiValue) {
        isMultiValue = multiValue;
    }

    public Object getValidationId() {
        return validationId;
    }

    public void setValidationId(Object validationId) {
        this.validationId = validationId;
    }

    public Object getSourceId() {
        return sourceId;
    }

    public void setSourceId(Object sourceId) {
        this.sourceId = sourceId;
    }

    public String getValueDelimiter () {
        return valueDelimiter ;
    }

    public void setValueDelimiter(String delimiter) {
        this.valueDelimiter  = delimiter;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
}
