package com.hp.oo.enginefacade.execution.log;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.Date;

/**
 * User: ronen
 * Date: 01/01/13
 * Time: 18:06
 */
public class StepErrorLog implements Serializable{
	private static final long serialVersionUID = -5568697213780706166L;

	private String message;
    private Date timeStamp;
    private ErrorType type;

    public enum ErrorType {
        OPER_ERROR,
        NAV_ERROR
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public ErrorType getType() {
        return type;
    }

    public void setType(ErrorType type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StepErrorLog)) return false;

        StepErrorLog that = (StepErrorLog) o;

        return new EqualsBuilder()
                .append(this.message, that.message)
                .append(this.timeStamp, that.timeStamp)
                .append(this.type, that.type)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(this.message)
                .append(this.timeStamp)
                .append(this.type)
                .toHashCode();
    }
}
