package com.hp.oo.execution.debug;

import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * User: hajyhia
 * Date: 2/24/13
 * Time: 3:34 PM
 */
public class AbstractExecutionInterrupt implements ExecutionInterrupt {
    private static final long serialVersionUID = -1171920843844504469L;


    private final String uuid;

    Map<String, String> interruptData;

    private boolean enabled;


    public AbstractExecutionInterrupt() {
        this(UUID.randomUUID().toString(), null);
    }
    public AbstractExecutionInterrupt(Map<String, String> interruptData) {
        this(UUID.randomUUID().toString(), interruptData);
    }

    protected AbstractExecutionInterrupt(String uuid, Map<String, String> interruptData) {
        if(uuid == null || uuid.isEmpty()){
            throw new RuntimeException("empty uuid");
        }
        this.uuid = uuid;
        this.enabled = true;
        this.interruptData = interruptData ;
        if(this.interruptData == null){
            this.interruptData = new HashMap<>();
            this.interruptData.put("*","*");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUUID() {
        return uuid;
    }

    @Override
    public String getValue(String key) {
        return interruptData.get(key);
    }

    public void setEnabled(boolean enabled) {
        synchronized (this) {
            this.enabled = enabled;
        }
    }

    public synchronized boolean isEnabled() {
        return enabled;
    }


    /**
     * this interrupt matches another interrupt if it's for the same flow and step
     */
    protected boolean matchInterrupt(ExecutionInterrupt other) {

        if(other == null)
            return true;
        boolean match = true;
        for (Map.Entry<String,String> entry : interruptData.entrySet()){
              if(!entry.getValue().equals(other.getValue(entry.getKey()))){
                  match =  false;
              }
        }

        return match && uuid.equals(other.getUUID());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractExecutionInterrupt that = (AbstractExecutionInterrupt) o;

        if (!uuid.equals(that.uuid)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = uuid.hashCode();
        return result;
    }

    @Override
    public String toString() {
        ToStringBuilder stringBuilder =  new ToStringBuilder(this).append("UUID", getUUID());
        for (String key : interruptData.keySet()){
            stringBuilder.append(key, interruptData.get(key));
        }
        stringBuilder.append("enabled", isEnabled()).toString();

        return stringBuilder.toString();
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        AbstractExecutionInterrupt clone = (AbstractExecutionInterrupt) super.clone();
        return clone;
    }

    public Map<String, String> getInterruptData() {
        return interruptData;
    }

    public void setInterruptData(Map<String, String> interruptData) {
        this.interruptData = interruptData;
    }
}
