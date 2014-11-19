/*
 * Licensed to Hewlett-Packard Development Company, L.P. under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
*/
package org.eclipse.score.samples.openstack.actions;

import com.google.common.collect.Sets;
import org.eclipse.score.api.ControlActionMetadata;
import org.eclipse.score.api.ExecutionPlan;
import org.eclipse.score.api.ExecutionStep;
import org.eclipse.score.api.TriggeringProperties;
import org.apache.commons.beanutils.BeanToPropertyValueTransformer;
import org.apache.commons.collections.CollectionUtils;
import org.eclipse.score.samples.controlactions.BranchActions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Date: 7/22/2014
 *
 * @author Bonczidai Levente
 */
public class ExecutionPlanBuilder {
	public static final String ACTION_CLASS_KEY = "className";
	public static final String ACTION_METHOD_KEY = "methodName";
	public static final String INPUT_BINDINGS_KEY = "inputBindings";
	public static final String NAVIGATION_MATCHERS_KEY = "navigationMatchers";
	public static final String BRANCH_ACTIONS_CLASS = "org.eclipse.score.samples.controlactions.BranchActions";
	public static final String SPLIT_METHOD_NAME = "splitWithContext";
	public static final String FLOW_UUID_KEY = "flowUuid";
	public static final String CONTEXT_KEY = "context";
	public static final String NAVIGATION_ACTIONS_CLASS = "org.eclipse.score.samples.controlactions.NavigationActions";
	public static final String SIMPLE_NAVIGATION_METHOD = "simpleNavigation";
	public static final String MULTI_INSTANCE_SPLIT_METHOD = "multiInstanceWithContext";
	public static final String PARALLEL_SPLIT_WITH_CONTEXT_METHOD = "parallelSplitWithContext";
	public static final String NEXT_STEP_ID_KEY = "nextStepId";
	public static final String JOIN_METHOD = "join";
	public static final String MULTI_INSTANCE_JOIN_METHOD = "joinBranches";
	public static final String OOACTION_RUNNER_CLASS = "org.eclipse.score.samples.openstack.actions.OOActionRunner";
	public static final String RUN_METHOD_NAME = "run";
	public static final String OOACTION_NAVIGATOR_CLASS = "org.eclipse.score.samples.openstack.actions.ActionNavigator";
	public static final String NAVIGATE_METHOD_NAME = "navigateWithMatchers";
	public static final String FLOW_UUID_PROPERTY = "flowUuid"; //property name in Execution Plan class


	private ExecutionPlan executionPlan;
	private Long beginStep;
	private Map<String, ? extends Serializable> initialExecutionContext;

	/**
	 * dependencies for all levels
	 */
	private Map<String, ExecutionPlan> dependencies;
	/**
	 * Holds the list of its direct subflows
	 */
	private List<ExecutionPlan> childSubflows;

	@SuppressWarnings("unused")
	public void setBeginStep(Long beginStep) {
		this.beginStep = beginStep;
	}

	public void setInitialExecutionContext(Map<String, ? extends Serializable> initialExecutionContext) {
		this.initialExecutionContext = initialExecutionContext;
	}

	public ExecutionPlanBuilder() {
		this(UUID.randomUUID().toString());
	}

	public ExecutionPlanBuilder(String flowUuid) {
		executionPlan = new ExecutionPlan();
		executionPlan.setFlowUuid(flowUuid);
		dependencies = new HashMap<>();
		initialExecutionContext = new HashMap<>();
		childSubflows = new ArrayList<>();
		beginStep = 0L;
	}

	public TriggeringProperties createTriggeringProperties() {
		TriggeringProperties triggeringProperties = TriggeringProperties.create(getExecutionPlan());

		triggeringProperties.getDependencies().putAll(dependencies);

		Map<String,Serializable> getRuntimeValues = new HashMap<>();
		triggeringProperties.setRuntimeValues(getRuntimeValues);

		triggeringProperties.setContext(initialExecutionContext);
		triggeringProperties.setStartStep(beginStep);

		return triggeringProperties;
	}

	public ExecutionPlan getExecutionPlan() {
		//prepare uuid set
		Collection uuidCollection = CollectionUtils.collect(childSubflows,
				new BeanToPropertyValueTransformer(FLOW_UUID_PROPERTY));
		Object[] uuidArray = uuidCollection.toArray();
		Set<String> uuidSet = Sets.newHashSet();

		for (Object uuid : uuidArray) {
			uuidSet.add(String.valueOf(uuid));
		}

		//set the uuid-s
		executionPlan.setSubflowsUUIDs(uuidSet);

		//set begin step
		executionPlan.setBeginStep(beginStep);

		return executionPlan;
	}

	public Long addOOActionStep(
			Long stepId,
			String actionClassName,
			String actionMethodName,
			List<InputBinding> inputBindings,
			List<NavigationMatcher<Serializable>> navigationMatchers) {
		ExecutionStep step = new ExecutionStep(stepId);

		step.setAction(new ControlActionMetadata(OOACTION_RUNNER_CLASS, RUN_METHOD_NAME));
		Map<String, Serializable> actionData = new HashMap<>(3);
		//put the actual action class name and method name
		actionData.put(ACTION_CLASS_KEY, actionClassName);
		actionData.put(ACTION_METHOD_KEY, actionMethodName);
		actionData.put(INPUT_BINDINGS_KEY, (Serializable) inputBindings);
		step.setActionData(actionData);

		setOONavigation(step, navigationMatchers);

		step.setSplitStep(false);

		executionPlan.addStep(step);

		return step.getExecStepId();
	}

	private void setOONavigation(ExecutionStep step, List<NavigationMatcher<Serializable>> navigationMatchers) {
		step.setNavigation(new ControlActionMetadata(OOACTION_NAVIGATOR_CLASS, NAVIGATE_METHOD_NAME));
		Map<String, Object> navigationData = new HashMap<>(1);
		navigationData.put(NAVIGATION_MATCHERS_KEY, navigationMatchers);
		step.setNavigationData(navigationData);
	}

	public Long addOOActionFinalStep(Long stepId, String actionClassName, String actionMethodName) {
		return addOOActionStep(stepId, actionClassName, actionMethodName, null, null);
	}

	public Long addStep(Long stepId, String classPath, String methodName, List<InputBinding> inputs, Long nextStepId) {
		ExecutionStep step = new ExecutionStep(stepId);

		step.setAction(new ControlActionMetadata(classPath, methodName));
		Map<String, Serializable> actionData = new HashMap<>(3);

		actionData.put(ACTION_METHOD_KEY, methodName);
		actionData.put("inputs", (Serializable) inputs);
		step.setActionData(actionData);

		step.setNavigation(new ControlActionMetadata(NAVIGATION_ACTIONS_CLASS, SIMPLE_NAVIGATION_METHOD));
		Map<String, Object> navigationData = new HashMap<>(2);
		navigationData.put(NEXT_STEP_ID_KEY, nextStepId);

		step.setNavigationData(navigationData);

		step.setSplitStep(false);

		executionPlan.addStep(step);

		return step.getExecStepId();
	}

	public Long addStep(
			Long stepId, String classPath, String methodName,Long nextStepId) {
		ExecutionStep step = new ExecutionStep(stepId);

		step.setAction(new ControlActionMetadata(classPath, methodName));
		Map<String, Serializable> actionData = new HashMap<>(3);

		actionData.put(ACTION_METHOD_KEY, methodName);

		step.setActionData(actionData);

		step.setNavigation(new ControlActionMetadata(NAVIGATION_ACTIONS_CLASS, SIMPLE_NAVIGATION_METHOD));
		Map<String, Object> navigationData = new HashMap<>(2);
		navigationData.put(NEXT_STEP_ID_KEY, nextStepId);

		step.setNavigationData(navigationData);

		step.setSplitStep(false);

		executionPlan.addStep(step);

		return step.getExecStepId();
	}
	public Long addStep(
			Long stepId, String classPath, String methodName, List<NavigationMatcher<Serializable>> navigationMatchers) {
		ExecutionStep step = new ExecutionStep(stepId);

		step.setAction(new ControlActionMetadata(classPath, methodName));
		Map<String, Serializable> actionData = new HashMap<>(3);

		actionData.put(ACTION_METHOD_KEY, methodName);

		step.setActionData(actionData);

		step.setNavigation(new ControlActionMetadata(OOACTION_NAVIGATOR_CLASS, NAVIGATE_METHOD_NAME));
		Map<String, Object> navigationData = new HashMap<>(1);
		navigationData.put(NAVIGATION_MATCHERS_KEY, navigationMatchers);
		step.setNavigationData(navigationData);

		step.setSplitStep(false);

		executionPlan.addStep(step);

		return step.getExecStepId();
	}

	@SuppressWarnings("unused")
	public Long simpleNavigate(Long nextStepId){
		return nextStepId;
	}

	public Long addSubflow(
			Long splitStepId,
			Long joinStepId,
			TriggeringProperties subflowProperties,
			List<String> inputKeysFromParentContext,
			List<NavigationMatcher<Serializable>> navigationMatchers) {
		//split step
		ExecutionStep executionSplitStep = new ExecutionStep(splitStepId);
		executionSplitStep.setSplitStep(true);

		executionSplitStep.setAction(new ControlActionMetadata(BRANCH_ACTIONS_CLASS, SPLIT_METHOD_NAME));
		Map<String, Serializable> actionData = new HashMap<>();
		actionData.put(FLOW_UUID_KEY, subflowProperties.getExecutionPlan().getFlowUuid());
		actionData.put(CONTEXT_KEY, (Serializable) subflowProperties.getContext());
		actionData.put("inputKeysFromParentContext", (Serializable) inputKeysFromParentContext);
		executionSplitStep.setActionData(actionData);

		executionSplitStep.setNavigation(new ControlActionMetadata(NAVIGATION_ACTIONS_CLASS, SIMPLE_NAVIGATION_METHOD));
		Map<String, Serializable> navigationData = new HashMap<>();
		navigationData.put(NEXT_STEP_ID_KEY, joinStepId);
		executionSplitStep.setNavigationData(navigationData);

		executionPlan.addStep(executionSplitStep);

		// join step
		ExecutionStep executionJoinStep = new ExecutionStep(joinStepId);

		executionJoinStep.setAction(new ControlActionMetadata(BRANCH_ACTIONS_CLASS, JOIN_METHOD));
		actionData = new HashMap<>();
		executionJoinStep.setActionData(actionData);

		setOONavigation(executionJoinStep, navigationMatchers);

		executionPlan.addStep(executionJoinStep);

		//register subFlow
		childSubflows.add(subflowProperties.getExecutionPlan());
		//register dependencies
		dependencies.put(subflowProperties.getExecutionPlan().getFlowUuid(), subflowProperties.getExecutionPlan());
		dependencies.putAll(subflowProperties.getDependencies());

		return executionSplitStep.getExecStepId();
	}
	@SuppressWarnings("unused")
	public Long addMultiInstance(Long splitStepId, Long joinStepId, String flowUuid, List<NavigationMatcher<Serializable>> navigationMatchers){
		Map<String, Serializable> actionData = new HashMap<>();
		actionData.put(BranchActions.STEP_POSITION, 0L);
		actionData.put(BranchActions.EXECUTION_PLAN_ID, flowUuid);

		ExecutionStep executionSplitStep = new ExecutionStep(splitStepId);
		executionSplitStep.setAction(new ControlActionMetadata(BRANCH_ACTIONS_CLASS, MULTI_INSTANCE_SPLIT_METHOD));
		executionSplitStep.setActionData(actionData);
		executionSplitStep.setSplitStep(true);

		executionSplitStep.setNavigation(new ControlActionMetadata(NAVIGATION_ACTIONS_CLASS, SIMPLE_NAVIGATION_METHOD));
		Map<String, Serializable> navigationData = new HashMap<>();
		navigationData.put(NEXT_STEP_ID_KEY, joinStepId);
		executionSplitStep.setNavigationData(navigationData);
		executionPlan.addStep(executionSplitStep);

		ExecutionStep executionJoinStep = new ExecutionStep(joinStepId);
		executionJoinStep.setAction(new ControlActionMetadata(BRANCH_ACTIONS_CLASS, MULTI_INSTANCE_JOIN_METHOD));

		actionData = new HashMap<>();
		executionJoinStep.setActionData(actionData);

		setOONavigation(executionJoinStep, navigationMatchers);

		executionPlan.addStep(executionJoinStep);

		return executionSplitStep.getExecStepId();
	}
	@SuppressWarnings("unused")
	public Long addParallel(Long splitStepId, Long joinStepId, List<String> flowUuids, List<NavigationMatcher<Serializable>> navigationMatchers){
		Map<String, Serializable> actionData = new HashMap<>();
		actionData.put(BranchActions.STEP_POSITION, 0L);
		actionData.put(BranchActions.PARALLEL_EXECUTION_PLAN_IDS, (Serializable) flowUuids);

		ExecutionStep executionSplitStep = new ExecutionStep(splitStepId);
		executionSplitStep.setAction(new ControlActionMetadata(BRANCH_ACTIONS_CLASS, PARALLEL_SPLIT_WITH_CONTEXT_METHOD));
		executionSplitStep.setActionData(actionData);
		executionSplitStep.setSplitStep(true);

		executionSplitStep.setNavigation(new ControlActionMetadata(NAVIGATION_ACTIONS_CLASS, SIMPLE_NAVIGATION_METHOD));
		Map<String, Serializable> navigationData = new HashMap<>();
		navigationData.put(NEXT_STEP_ID_KEY, joinStepId);
		executionSplitStep.setNavigationData(navigationData);
		executionPlan.addStep(executionSplitStep);

		ExecutionStep executionJoinStep = new ExecutionStep(joinStepId);
		executionJoinStep.setAction(new ControlActionMetadata(BRANCH_ACTIONS_CLASS, MULTI_INSTANCE_JOIN_METHOD));

		actionData = new HashMap<>();
		executionJoinStep.setActionData(actionData);

		setOONavigation(executionJoinStep, navigationMatchers);

		executionPlan.addStep(executionJoinStep);

		return executionSplitStep.getExecStepId();
	}
}
