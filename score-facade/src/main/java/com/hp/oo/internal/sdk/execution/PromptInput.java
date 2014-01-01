package com.hp.oo.internal.sdk.execution;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.io.Serializable;
import java.util.Objects;

/**
 * User: hanael
 */
public class PromptInput implements Serializable {

    private static final long serialVersionUID = 5923239487681731108L;

    private boolean encrypted;

    private boolean required;

    private String name;

    private String type;

    private String promptMessage;

    private String promptType;

    private String errorMessage;

    private String listValues;

    private String listName;

    private String sourceDelimiter;

    private String valueDelimiter;

    private boolean multiSelect;

    private boolean promptMessageProvided = true;

	private String defaultValue;

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public boolean isEncrypted() {
        return encrypted;
    }

    public void setEncrypted(boolean encrypted) {
        this.encrypted = encrypted;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPromptType() {
        return promptType;
    }

    public void setPromptType(String promptType) {
        this.promptType = promptType;
    }

	public String getPromptMessage() {
		return promptMessage;
	}

	public void setPromptMessage(String promptMessage) {
		this.promptMessage = promptMessage;
	}

    public String getListValues() {
        return listValues;
    }

    public void setListValues(String listValues) {
        this.listValues = listValues;
    }

    public String getListName() {
        return listName;
    }

    public void setListName(String listName) {
        this.listName = listName;
    }

    public String getSourceDelimiter() {
        return sourceDelimiter;
    }

    public void setSourceDelimiter(String sourceDelimiter) {
        this.sourceDelimiter = sourceDelimiter;
    }

    public String getValueDelimiter() {
        return valueDelimiter;
    }

    public void setValueDelimiter(String valueDelimiter) {
        this.valueDelimiter = valueDelimiter;
    }

    public boolean isMultiSelect() {
        return multiSelect;
    }

    public void setMultiSelect(boolean multiSelect) {
        this.multiSelect = multiSelect;
    }

    public boolean isPromptMessageProvided() {
        return promptMessageProvided;
    }

    public void setPromptMessageProvided(boolean promptMessageProvided) {
        this.promptMessageProvided = promptMessageProvided;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("encrypted", this.encrypted)
                .append("required", this.required)
                .append("name", this.name)
                .append("type", this.type)
                .append("promptMessage", this.promptMessage)
                .append("promptType", this.promptType)
                .append("errorMessage", this.errorMessage)
                .append("listValues", this.listValues)
                .append("listName", this.listName)
                .append("sourceDelimiter", this.sourceDelimiter)
                .append("valueDelimiter", this.valueDelimiter)
                .append("multiSelect", this.multiSelect)
                .append("defaultValue", this.defaultValue)
                .toString();
    }

    @Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		PromptInput that = (PromptInput) o;
		return new EqualsBuilder()
				.append(this.encrypted, that.encrypted)
				.append(this.required, that.required)
				.append(this.name, that.name)
				.append(this.type, that.type)
				.append(this.promptMessage, that.promptMessage)
				.append(this.promptType, that.promptType)
				.append(this.errorMessage, that.errorMessage)
				.append(this.listValues, that.listValues)
				.append(this.listName, that.listName)
				.append(this.sourceDelimiter, that.sourceDelimiter)
                .append(this.valueDelimiter, that.valueDelimiter)
				.append(this.multiSelect, that.multiSelect)
				.append(this.defaultValue, that.defaultValue)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return Objects.hash(
				encrypted,
				required,
				name,
				type,
				promptMessage,
				promptType,
				errorMessage,
				listValues,
				listName,
                sourceDelimiter,
                valueDelimiter,
				multiSelect,
				defaultValue);
	}
}
