package com.hp.oo.enginefacade.execution.log;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.io.Serializable;

/**
 * User: zruya
 * Date: 03/01/13
 * Time: 10:41
 */
public class StepTransitionLog implements Serializable{
	private static final long serialVersionUID = -145923078501934483L;

	private String transitionName;
    private String transitionDescription;
    private String responseName;
    private String responseType;
    private String transitionValue;

    public String getTransitionValue() {
        return transitionValue;
    }

    public void setTransitionValue(String transitionValue) {
        this.transitionValue = transitionValue;
    }

    public String getResponseType() {
        return responseType;
    }

    public void setResponseType(String responseType) {
        this.responseType = responseType;
    }

    public String getResponseName() {
        return responseName;
    }

    public void setResponseName(String responseName) {
        this.responseName = responseName;
    }

    public String getTransitionDescription() {
        return transitionDescription;
    }

    public void setTransitionDescription(String transitionDescription) {
        this.transitionDescription = transitionDescription;
    }

    public String getTransitionName() {
        return transitionName;
    }

    public void setTransitionName(String transitionName) {
        this.transitionName = transitionName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StepTransitionLog)) return false;

        StepTransitionLog that = (StepTransitionLog) o;

        return new EqualsBuilder()
                .append(this.transitionName, that.transitionName)
                .append(this.transitionDescription, that.transitionDescription)
                .append(this.responseName, that.responseName)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(this.transitionName)
                .append(this.transitionDescription)
                .append(this.responseName)
                .toHashCode();
    }
}


