package com.hp.score.lang;

import com.hp.oo.enginefacade.execution.EndBranchDataContainer;
import com.hp.score.events.ScoreEvent;
import com.hp.score.api.StartBranchDataContainer;
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

    private static final String FINISHED_CHILD_BRANCHES_DATA = "FINISHED_CHILD_BRANCHES_DATA";

    public static final String RUNNING_PLANS_MAP = "RUNNING_PLANS_MAP";

	protected Map<String, Serializable> myMap = new HashMap<>();

    public ExecutionRuntimeServices(){}

    public ExecutionRuntimeServices(ExecutionRuntimeServices executionRuntimeServices){
        myMap.putAll(executionRuntimeServices.myMap);
    }

	public void pause() {
		myMap.put(EXECUTION_PAUSED, Boolean.TRUE);
	}

	public boolean isPaused() {
		return myMap.containsKey(EXECUTION_PAUSED) && myMap.get(EXECUTION_PAUSED).equals(Boolean.TRUE);
	}

	public void addEvent(String eventType, Serializable eventData) {
		@SuppressWarnings("unchecked")
		Queue<ScoreEvent> eventsQueue = getFromMap(SCORE_EVENTS_QUEUE);
		if (eventsQueue == null) {
			eventsQueue = new ArrayDeque<>();
			myMap.put(SCORE_EVENTS_QUEUE, (ArrayDeque) eventsQueue);
		}
		eventsQueue.add(new ScoreEvent(eventType, eventData));
	}

	public ArrayDeque<ScoreEvent> getEvents() {
		return getFromMap(SCORE_EVENTS_QUEUE);
	}

	public void setNoWorkerInGroup(String groupName) {
		myMap.put(NO_WORKERS_IN_GROUP, groupName);
	}

	public String getNoWorkerInGroupName() {
		return getFromMap(NO_WORKERS_IN_GROUP);
	}

	protected <T> T getFromMap(String key) {
		if (myMap.containsKey(key)) {
			Serializable value = myMap.get(key);
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
		if (!myMap.containsKey(BRANCH_DATA)) {
			myMap.put(BRANCH_DATA, new ArrayList<StartBranchDataContainer>());
		}
		List<StartBranchDataContainer> branchesData = getFromMap(BRANCH_DATA);
		branchesData.add(new StartBranchDataContainer(startPosition, executionPlanId, context, new SystemContext(executionRuntimeServices.myMap)));//TODO :why SystemContext object here? remove this, need to be ExecutioneRuntimeServices instead..
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
        return (List<EndBranchDataContainer>) getFromMap(FINISHED_CHILD_BRANCHES_DATA);
    }

	private <T> T removeFromMap(String key) {
		if (myMap.containsKey(key)) {
			Serializable value = myMap.remove(key);
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
				.append(this.myMap, that.myMap)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.append(this.myMap)
				.toHashCode();
	}
}
