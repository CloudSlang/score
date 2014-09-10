package com.hp.score.facade.entities;

import com.hp.score.lang.SystemContext;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Date: 8/1/11
 *
 * @author Dima Rassin
 */
public class Execution implements Serializable {
    private Long executionId;
    private Long runningExecutionPlanId;
    private Long position;
    private String groupName;
    private boolean mustGoToQueue;
//    private long lastEventDumpTime;

    protected Map<String, Serializable> contexts;
    protected SystemContext systemContext = new SystemContext();
    //This context is an internal action context for sharing serializable data between actions on the same execution
    //TODO  - remove - should be part of contexts!!
    protected Map<String, Serializable> serializableSessionContext;

    public Execution() {
//        this.lastEventDumpTime = 0;
        this.mustGoToQueue = false;
        this.contexts = new HashMap<>();
        this.serializableSessionContext = new HashMap<>();
    }

    public Execution(Long executionId, Long runningExecutionPlanId, Long position, Map<String, ? extends Serializable> contexts, Map<String, Serializable> systemContext) {
        this(runningExecutionPlanId, position, contexts);
//        this.contexts.putAll(contexts);
        this.systemContext.putAll(systemContext);
//        this.position = position;
//        this.runningExecutionPlanId = runningExecutionPlanId;
        this.executionId = executionId;
    }

    public Execution(Long runningExecutionPlanId, Long position, Map<String, ? extends Serializable> contexts) {
        this();
        this.position = position;
        this.runningExecutionPlanId = runningExecutionPlanId;
        if(contexts != null) {
            this.contexts.putAll(contexts);
        }
    }

    public boolean isMustGoToQueue() {
        return mustGoToQueue;
    }

    public void setMustGoToQueue(boolean mustGoToQueue) {
        this.mustGoToQueue = mustGoToQueue;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Long getExecutionId() {
        return executionId;
    }

    //todo - Meshi why we need this??
    public void setExecutionId(Long executionId) {
        this.executionId = executionId;
    }

    public Long getRunningExecutionPlanId() {
        return runningExecutionPlanId;
    }

    public Long getPosition() {
        return position;
    }

    public Execution setPosition(Long position) {
        this.position = position;
        return this;
    }

    public Map<String, Serializable> getContexts() {
        return contexts;
    }

    public SystemContext getSystemContext() {
        return systemContext;
    }

    public Map<String, Serializable> getSerializableSessionContext() {
        return serializableSessionContext;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Execution execution = (Execution) o;

        if (executionId != null ? !executionId.equals(execution.executionId) : execution.executionId != null)
            return false;
        if (position != null ? !position.equals(execution.position) : execution.position != null)
            return false;
        if (runningExecutionPlanId != null ? !runningExecutionPlanId.equals(execution.runningExecutionPlanId) : execution.runningExecutionPlanId != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = executionId != null ? executionId.hashCode() : 0;
        result = 31 * result + (runningExecutionPlanId != null ? runningExecutionPlanId.hashCode() : 0);
        result = 31 * result + (position != null ? position.hashCode() : 0);
        return result;
    }
}
