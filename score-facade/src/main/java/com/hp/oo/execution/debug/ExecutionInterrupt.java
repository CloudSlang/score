package com.hp.oo.execution.debug;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: hajyhia
 * Date: 2/24/13
 * Time: 3:34 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ExecutionInterrupt extends Serializable, Cloneable {

    public enum InterruptType{
        BREAKPOINT,
        OVERRIDE_RESPONSES
    }

    String getUUID();

    abstract String getValue(String key);

    abstract boolean isEnabled();

    abstract void setEnabled(boolean enabled);

    public Object clone() throws CloneNotSupportedException;

}
