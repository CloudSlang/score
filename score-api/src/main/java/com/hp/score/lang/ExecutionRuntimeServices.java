package com.hp.score.lang;

import com.hp.score.api.EndBranchDataContainer;
import com.hp.score.events.ScoreEvent;
import com.hp.score.api.StartBranchDataContainer;
import com.hp.score.facade.execution.ExecutionStatus;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * User: maromg
 * Date: 11/06/2014
 */
public class ExecutionRuntimeServices implements Serializable {

	private static final long serialVersionUID = 2557429503280678353L;

	protected static final String EXECUTION_PAUSED = "EXECUTION_PAUSED";
	private static final String BRANCH_DATA = "BRANCH_DATA";

	protected static final String SCORE_EVENTS_QUEUE = "SCORE_EVENTS_QUEUE";

	protected static final String NO_WORKERS_IN_GROUP = "NO_WORKERS_IN_GROUP";

    private static final String NEW_SPLIT_ID = "NEW_SPLIT_ID";

    private static final String BRANCH_ID = "BRANCH_ID";

    private static final String EXECUTION_ID_CONTEXT = "executionIdContext";

    private static final String EXECUTION_STEP_ERROR_KEY = "EXECUTION_STEP_ERROR_KEY";

    private static final String FINISHED_CHILD_BRANCHES_DATA = "FINISHED_CHILD_BRANCHES_DATA";

    private static final String RUNNING_PLANS_MAP = "RUNNING_PLANS_MAP";

    private static final String BEGIN_STEPS_MAP = "BEGIN_STEPS_MAP";

    private static final String FLOW_TERMINATION_TYPE = "FLOW_TERMINATION_TYPE";

    protected Map<String, Serializable> contextMap = new HashMap<>();

    public ExecutionRuntimeServices(){}

    public ExecutionRuntimeServices(ExecutionRuntimeServices executionRuntimeServices){
        contextMap.putAll(executionRuntimeServices.contextMap);
        contextMap.remove(NEW_SPLIT_ID);
        contextMap.remove(BRANCH_ID);
    }

    public void setFinishedChildBranchesData(ArrayList<EndBranchDataContainer> data){
        Validate.isTrue(!contextMap.containsKey(FINISHED_CHILD_BRANCHES_DATA), "not allowed to overwrite finished branches data");
        contextMap.put(FINISHED_CHILD_BRANCHES_DATA, data);
    }

    public void setSubFlowsData(Map<String, Long> runningPlansIds,Map<String, Long> beginStepsIds ) {
        contextMap.put(RUNNING_PLANS_MAP, (Serializable) runningPlansIds);
        contextMap.put(BEGIN_STEPS_MAP, (Serializable) beginStepsIds);
    }

    public Long getSubFlowRunningExecutionPlan(String subFlowUuid){
        return ((Map<String, Long>) contextMap.get(RUNNING_PLANS_MAP)).get(subFlowUuid);
    }

    public Long getSubFlowBeginStep(String subFlowUuid){
        return ((Map<String, Long>) contextMap.get(BEGIN_STEPS_MAP)).get(subFlowUuid);
    }

    public String getBranchId(){
        return getFromMap(BRANCH_ID);
    }

    public void setBranchId(String brunchId) {
        Validate.isTrue(StringUtils.isEmpty(getBranchId()), "not allowed to overwrite branch id");
        contextMap.put(BRANCH_ID, brunchId);
    }

    public ExecutionStatus getFlowTerminationType(){
        return getFromMap(FLOW_TERMINATION_TYPE);
    }

    public void setFlowTerminationType(ExecutionStatus flowTerminationType) {
        contextMap.put(FLOW_TERMINATION_TYPE, flowTerminationType);
    }

    public String getStepErrorKey(){
        return getFromMap(EXECUTION_STEP_ERROR_KEY);
    }

    public void setStepErrorKey(String stepErrorKey) {
        contextMap.put(EXECUTION_STEP_ERROR_KEY, stepErrorKey);
    }

    public boolean hasStepErrorKey(){
        return contextMap.containsKey(EXECUTION_STEP_ERROR_KEY);
    }

    public String removeStepErrorKey(){
        return (String)removeFromMap(EXECUTION_STEP_ERROR_KEY);
    }

    public Long getExecutionId(){
        return getFromMap(EXECUTION_ID_CONTEXT);
    }

    public void setExecutionId(Long executionId) {
        contextMap.put(EXECUTION_ID_CONTEXT, executionId);
    }

    public String getSplitId(){
        return getFromMap(NEW_SPLIT_ID);
    }

    public void setSplitId(String splitId) {
        Validate.isTrue(StringUtils.isEmpty(getSplitId()), "not allowed to overwrite split id");
        contextMap.put(NEW_SPLIT_ID, splitId);
    }


    public void pause() {
		contextMap.put(EXECUTION_PAUSED, Boolean.TRUE);
	}

	public boolean isPaused() {
		return contextMap.containsKey(EXECUTION_PAUSED) && contextMap.get(EXECUTION_PAUSED).equals(Boolean.TRUE);
	}

	public void addEvent(String eventType, Serializable eventData) {
		@SuppressWarnings("unchecked")
		Queue<ScoreEvent> eventsQueue = getFromMap(SCORE_EVENTS_QUEUE);
		if (eventsQueue == null) {
			eventsQueue = new ArrayDeque<>();
			contextMap.put(SCORE_EVENTS_QUEUE, (ArrayDeque) eventsQueue);
		}
		eventsQueue.add(new ScoreEvent(eventType, eventData));
	}

	public ArrayDeque<ScoreEvent> getEvents() {
		return getFromMap(SCORE_EVENTS_QUEUE);
	}

	public void setNoWorkerInGroup(String groupName) {
		contextMap.put(NO_WORKERS_IN_GROUP, groupName);
	}

	public String getNoWorkerInGroupName() {
		return getFromMap(NO_WORKERS_IN_GROUP);
	}

	protected <T> T getFromMap(String key) {
		if (contextMap.containsKey(key)) {
			Serializable value = contextMap.get(key);
			if (value != null) {
				@SuppressWarnings("unchecked")
				T retVal = (T) value;
				return retVal;
			}
		}
		return null;
	}

	public void addBranch(Long startPosition, Long executionPlanId, Map<String, Serializable> context) { //TODO : delete this method , use instead the method below
		addBranch(startPosition, executionPlanId, context, this);
	}

    public void addBranch(Long startPosition, String flowUuid, Map<String, Serializable> context){
        Map<String, Long> runningPlansIds = getFromMap(RUNNING_PLANS_MAP);
        Long runningPlanId = runningPlansIds.get(flowUuid);
        addBranch(startPosition, runningPlanId, context, new ExecutionRuntimeServices(this));
    }

	protected void addBranch(Long startPosition, Long executionPlanId, Map<String, Serializable> context, ExecutionRuntimeServices executionRuntimeServices) {
		if (!contextMap.containsKey(BRANCH_DATA)) {
			contextMap.put(BRANCH_DATA, new ArrayList<StartBranchDataContainer>());
		}
		List<StartBranchDataContainer> branchesData = getFromMap(BRANCH_DATA);
		branchesData.add(new StartBranchDataContainer(startPosition, executionPlanId, context, new SystemContext(executionRuntimeServices.contextMap)));
	}

	/**
	 * Removes the branches data and returns it
	 */
	public List<StartBranchDataContainer> removeBranchesData() {
		return removeFromMap(BRANCH_DATA);
	}

    /**
     * @return a list of all branches ended.
     */
    public List<EndBranchDataContainer> getFinishedChildBranchesData() {
        return (List<EndBranchDataContainer>) removeFromMap(FINISHED_CHILD_BRANCHES_DATA);
    }

	private <T> T removeFromMap(String key) {
		if (contextMap.containsKey(key)) {
			Serializable value = contextMap.remove(key);
			if (value != null) {
				@SuppressWarnings("unchecked")
				T retVal = (T) value;
				return retVal;
			}
		}
		return null;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		ExecutionRuntimeServices that = (ExecutionRuntimeServices) o;

		return new EqualsBuilder()
				.append(this.contextMap, that.contextMap)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.append(this.contextMap)
				.toHashCode();
	}
}
