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

//    private static final String REQUESTED_EXECUTION_PLAN_ID = "REQUESTED_EXECUTION_PLAN_ID";

    protected Map<String, Serializable> contextMap = new HashMap<>();

    public ExecutionRuntimeServices(){}

    /**
     * copy constructor that clean the NEW_SPLIT_ID & BRANCH_ID keys
     * @param executionRuntimeServices
     */
    public ExecutionRuntimeServices(ExecutionRuntimeServices executionRuntimeServices){
        contextMap.putAll(executionRuntimeServices.contextMap);
        contextMap.remove(NEW_SPLIT_ID);
        contextMap.remove(BRANCH_ID);
    }

    /**
     *  setter for the finished child brunches data
     * @param data  - list of EndBranchDataContainer
     */
    public void setFinishedChildBranchesData(ArrayList<EndBranchDataContainer> data){
        Validate.isTrue(!contextMap.containsKey(FINISHED_CHILD_BRANCHES_DATA), "not allowed to overwrite finished branches data");
        contextMap.put(FINISHED_CHILD_BRANCHES_DATA, data);
    }

    /**
     * put all the data relevant for sub flows: map of runningPlanIds and list of BeginStepIds
     * @param runningPlansIds  - map of flowUUID to runningPlanId
     * @param beginStepsIds   -  map of flowUUID to beginStepId
     */
    public void setSubFlowsData(Map<String, Long> runningPlansIds,Map<String, Long> beginStepsIds ) {
        contextMap.put(RUNNING_PLANS_MAP, (Serializable) runningPlansIds);
        contextMap.put(BEGIN_STEPS_MAP, (Serializable) beginStepsIds);
    }

    /**
     *
     * @param subFlowUuid - the required sub flow UUID
     * @return  the id of the runningPlan of the given flow
     */
    public Long getSubFlowRunningExecutionPlan(String subFlowUuid){
        return ((Map<String, Long>) contextMap.get(RUNNING_PLANS_MAP)).get(subFlowUuid);
    }

    /**
     *
     * @param subFlowUuid - the required sub flow UUID
     * @return the begin step of the given flow
     */
    public Long getSubFlowBeginStep(String subFlowUuid){
        return ((Map<String, Long>) contextMap.get(BEGIN_STEPS_MAP)).get(subFlowUuid);
    }

    /**
     *
     * @return the brunchId of the current execution
     */
    public String getBranchId(){
        return getFromMap(BRANCH_ID);
    }

    /**
     * setter for the brunch id of the current Execution
     * @param brunchId
     */
    public void setBranchId(String brunchId) {
        Validate.isTrue(StringUtils.isEmpty(getBranchId()), "not allowed to overwrite branch id");
        contextMap.put(BRANCH_ID, brunchId);
    }

    /**
     *
     * @return the flow termination type : one of ExecutionStatus values
     */
    public ExecutionStatus getFlowTerminationType(){
        return getFromMap(FLOW_TERMINATION_TYPE);
    }

    /**
     * set the flow termination type
     * @param flowTerminationType - from ExecutionStatus
     */
    public void setFlowTerminationType(ExecutionStatus flowTerminationType) {
        contextMap.put(FLOW_TERMINATION_TYPE, flowTerminationType);
    }

//    public void requestToChangeExecutionPlan(Long runningExecutionPlanId) {
//        contextMap.put(REQUESTED_EXECUTION_PLAN_ID, runningExecutionPlanId);
//    }
//
//    public Long handleRequestForChangingExecutionPlan(){
//        Long requestedExecutionPlanId = getFromMap(REQUESTED_EXECUTION_PLAN_ID);
//        contextMap.put(REQUESTED_EXECUTION_PLAN_ID, null);
//        return requestedExecutionPlanId;
//    }

    /**
     *
     * @return the error key of the step
     */
    public String getStepErrorKey(){
        return getFromMap(EXECUTION_STEP_ERROR_KEY);
    }

    /**
     * set the step error key
     * @param stepErrorKey
     */
    public void setStepErrorKey(String stepErrorKey) {
        contextMap.put(EXECUTION_STEP_ERROR_KEY, stepErrorKey);
    }

    /**
     *
     * @return  true if there is step error
     */
    public boolean hasStepErrorKey(){
        return contextMap.containsKey(EXECUTION_STEP_ERROR_KEY);
    }

    /**
     * clean step error key
     * @return the values cleaned
     */
    public String removeStepErrorKey(){
        return (String)removeFromMap(EXECUTION_STEP_ERROR_KEY);
    }

    /**
     *
     * @return the execution id
     */
    public Long getExecutionId(){
        return getFromMap(EXECUTION_ID_CONTEXT);
    }

    /**
     * set the execution id - should be called only once in score triggering!
     * @param executionId
     */
    public void setExecutionId(Long executionId) {
        contextMap.put(EXECUTION_ID_CONTEXT, executionId);
    }

    /**
     *
     * @return the split id
     */
    public String getSplitId(){
        return getFromMap(NEW_SPLIT_ID);
    }

    /**
     * set teh split id
     * @param splitId
     */
    public void setSplitId(String splitId) {
        Validate.isTrue(StringUtils.isEmpty(getSplitId()), "not allowed to overwrite split id");
        contextMap.put(NEW_SPLIT_ID, splitId);
    }

    /**
     * used for asking score to pause your run
     */
    public void pause() {
		contextMap.put(EXECUTION_PAUSED, Boolean.TRUE);
	}

    /**
     *
     * @return  true if the execution should be paused
     */
	public boolean isPaused() {
		return contextMap.containsKey(EXECUTION_PAUSED) && contextMap.get(EXECUTION_PAUSED).equals(Boolean.TRUE);
	}

    /**
     *  add event - for score to fire
     * @param eventType  - string which is the key you can listen to
     * @param eventData  - the event data
     */
	public void addEvent(String eventType, Serializable eventData) {
		@SuppressWarnings("unchecked")
		Queue<ScoreEvent> eventsQueue = getFromMap(SCORE_EVENTS_QUEUE);
		if (eventsQueue == null) {
			eventsQueue = new ArrayDeque<>();
			contextMap.put(SCORE_EVENTS_QUEUE, (ArrayDeque) eventsQueue);
		}
		eventsQueue.add(new ScoreEvent(eventType, eventData));
	}

    /**
     *
     * @return all the added events
     */
	public ArrayDeque<ScoreEvent> getEvents() {
		return getFromMap(SCORE_EVENTS_QUEUE);
	}

    /**
     *  means we dont have worker with the required group
     * @param groupName - the name of the missing group
     */
	public void setNoWorkerInGroup(String groupName) {
		contextMap.put(NO_WORKERS_IN_GROUP, groupName);
	}

    /**
     *
     * @return the missing group name
     */
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

    /**
     * add brunch - means you want to split your execution
     * @param startPosition  - the position in the execution plan the new brunch will point to
     * @param flowUuid - the flow uuid
     * @param context - the context of the created brunch
     */
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
