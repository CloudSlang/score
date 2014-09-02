package com.hp.score.facade.entities;

import com.hp.oo.internal.sdk.execution.ExecutionConstants;
import com.hp.score.api.EndBranchDataContainer;
import com.hp.score.lang.SystemContext;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    private long lastEventDumpTime;

    protected Map<String, Serializable> contexts;
    protected SystemContext systemContext = new SystemContext();
    //This context is an internal action context for sharing serializable data between actions on the same execution
    protected Map<String, Serializable> serializableSessionContext;

    public Execution() {
        this.lastEventDumpTime = 0;
        this.mustGoToQueue = false;
        this.contexts = new HashMap<>();
        this.serializableSessionContext = new HashMap<>();
    }

    public Execution(Long executionId, Long runningExecutionPlanId, Long position, Map<String, ? extends Serializable> contexts, Map<String, Serializable> systemContext) {
        this();
        this.contexts.putAll(contexts);
        this.systemContext.putAll(systemContext);
        this.position = position;
        this.runningExecutionPlanId = runningExecutionPlanId;
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

    public void setContexts(Map<String, Serializable> contexts) {
        this.contexts = contexts;
    }

    public Map<String, Serializable> getSerializableSessionContext() {
        return serializableSessionContext;
    }

    public long getLastEventDumpTime() {
        return lastEventDumpTime;
    }

    public void setLastEventDumpTime(long lastEventDumpTime) {
        this.lastEventDumpTime = lastEventDumpTime;
    }

    /*  TODO :
        System Context Wrapper APIs
        ----------------------------
        The following methods are here to provide a cleaner API above the SystemContext,
        this is a temporary solution to the "map hell", instead of having 'execution.getSystemContext.get(ExecutionConstants.BLAH)'
        scattered across our code and having different places manipulate the system context.
        there should be only one place which has knowledge about the internal structure of the system context.

        It would be better to have the SystemContext object as a data structure and have it contain the following methods,
        this requires a bigger refactor, so in the meantime as a "better then nothing" solution i'm adding the new APIs below

        - Matan
    */
    public boolean isBranch() {
        return !StringUtils.isEmpty(systemContext.getBranchId());
    }

    public boolean isNewBranchMechanism() {
        return systemContext.containsKey(ExecutionConstants.NEW_BRANCH_MECHANISM);
    }

    public String getBranchId() {
        return getSystemContext().getBranchId();
    }

    public void putBranchId(String branchId) {
        Validate.isTrue(StringUtils.isEmpty(getSystemContext().getBranchId()), "not allowed to overwrite branch id");
        getSystemContext().setBranchId(branchId);
    }

    public String getSplitId() {
        return getSystemContext().getSplitId();
    }

    public void putSplitId(String splitId) {
        Validate.isTrue(StringUtils.isEmpty(getSystemContext().getSplitId()), "not allowed to overwrite split id");
        getSystemContext().setSplitId(splitId);
    }

    public String getError() {
        return (String) getSystemContext().get(ExecutionConstants.EXECUTION_STEP_ERROR_KEY);
    }

    /**
     * @param contexts a list of Maps of OO Contexts, each map holds the maps of a finished branch, the type is ArrayList because
     *                 it has to be serializable
     */
    public void putFinishedChildBranchesData(ArrayList<EndBranchDataContainer> contexts) {
        Validate.isTrue(!getSystemContext().containsKey(ExecutionConstants.FINISHED_CHILD_BRANCHES_DATA), "not allowed to overwrite finished branches data");
        systemContext.put(ExecutionConstants.FINISHED_CHILD_BRANCHES_DATA, contexts);
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
