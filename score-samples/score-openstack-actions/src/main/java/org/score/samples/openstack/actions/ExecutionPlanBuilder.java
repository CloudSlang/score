package org.score.samples.openstack.actions;

import com.hp.score.api.ControlActionMetadata;
import com.hp.score.api.ExecutionPlan;
import com.hp.score.api.ExecutionStep;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Date: 7/22/2014
 *
 * @author Bonczidai Levente
 */
public class ExecutionPlanBuilder {
	public static final String ACTION_CLASS_KEY = "className";
	public static final String ACTION_METHOD_KEY = "methodName";

	private ExecutionPlan executionPlan;

	public ExecutionPlanBuilder() {
		executionPlan = new ExecutionPlan();
		executionPlan.setFlowUuid(UUID.randomUUID().toString());
		executionPlan.setBeginStep(0L);
	}

	public ExecutionPlan getExecutionPlan() {
		return executionPlan;
	}

	public Long addStep(
			Long stepId,
			String actionClassName,
			String actionMethodName,
			List<NavigationMatcher> navigationMatchers) {
		ExecutionStep step = new ExecutionStep(stepId);

		step.setAction(new ControlActionMetadata("org.score.samples.openstack.actions.OOActionRunner", "run"));
		Map<String, String> actionData = new HashMap<>(2);
		//put the actual action class name and method name
		actionData.put(ACTION_CLASS_KEY, actionClassName);
		actionData.put(ACTION_METHOD_KEY, actionMethodName);
		step.setActionData(actionData);

		step.setNavigation(new ControlActionMetadata("org.score.samples.openstack.actions.OOActionNavigator", "navigate"));
		Map<String, Object> navigationData = new HashMap<>(2);
		navigationData.put("navigationMatchers", navigationMatchers);

		step.setNavigationData(navigationData);

		step.setSplitStep(false);

		executionPlan.addStep(step);

		return step.getExecStepId();
	}

	public Long addFinalStep(Long stepId, String actionClassName, String actionMethodName) {
		return addStep(stepId, actionClassName, actionMethodName, null);
	}

	@SuppressWarnings("unused")
	public void setBeginStep(Long beginStepId)
	{
		executionPlan.setBeginStep(beginStepId);
	}
}
