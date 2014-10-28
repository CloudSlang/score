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
package com.hp.score.samples.openstack;

import com.google.common.collect.Sets;
import com.hp.score.api.ExecutionPlan;
import com.hp.score.api.TriggeringProperties;
import com.hp.score.samples.openstack.actions.ExecutionPlanBuilder;
import com.hp.score.samples.openstack.actions.InputBinding;
import com.hp.score.samples.openstack.actions.InputBindingFactory;
import com.hp.score.samples.openstack.actions.MatchType;
import com.hp.score.samples.openstack.actions.NavigationMatcher;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.hp.score.samples.openstack.OpenstackCommons.OPENSTACK_UTILS_CLASS;
import static com.hp.score.samples.openstack.OpenstackCommons.GET_MULTI_INSTANCE_RESPONSE_METHOD;
import static com.hp.score.samples.openstack.OpenstackCommons.SERVER_NAMES_LIST_MESSAGE;
import static com.hp.score.samples.openstack.OpenstackCommons.SERVER_NAMES_LIST_KEY;

import static com.hp.score.samples.openstack.OpenstackCommons.SPLIT_SERVERS_INTO_BRANCH_CONTEXTS_METHOD;
import static com.hp.score.samples.openstack.OpenstackCommons.createFailureStep;
import static com.hp.score.samples.openstack.OpenstackCommons.createSuccessStep;
import static com.hp.score.samples.openstack.OpenstackCommons.mergeInputsWithoutDuplicates;
import static com.hp.score.samples.openstack.OpenstackCommons.SUCCESS_RESPONSE;
import static com.hp.score.samples.openstack.OpenstackCommons.RESPONSE_KEY;


/**
 * Date: 8/29/2014
 *
 * @author lesant
 */
@SuppressWarnings("unused")
public class CreateMultiInstanceServersFlow {
	private List<InputBinding> inputBindings;

	@SuppressWarnings("unused")
	public CreateMultiInstanceServersFlow() {
		inputBindings = generateInitialInputBindings();
	}
	@SuppressWarnings("unused")
	public List<InputBinding> getInputBindings() {
		return inputBindings;
	}

	private List<InputBinding> generateInitialInputBindings() {
		@SuppressWarnings("unchecked") List<InputBinding> bindings = mergeInputsWithoutDuplicates(
				new CreateServerFlow().getInputBindings());

		bindings.remove(InputBindingFactory.createInputBinding(OpenstackCommons.SERVER_NAME_MESSAGE, OpenstackCommons.SERVER_NAME_KEY, true));
		bindings.add(InputBindingFactory.createInputBinding(SERVER_NAMES_LIST_MESSAGE, SERVER_NAMES_LIST_KEY, true));

		return bindings;
	}

	@SuppressWarnings("unused")
	public TriggeringProperties createMultiInstanceServersFlow(){
		ExecutionPlanBuilder builder = new ExecutionPlanBuilder();

		Long splitContextsStepId = 0L;
		Long createServerJoinId = 1L;
		Long createServerSplitId = 2L;
		Long getMultiInstanceResponseStepId = 3L;
		Long successStepId = 4L;
		Long failureStepId = 5L;

		CreateServerFlow createServer = new CreateServerFlow();
		ExecutionPlan createServerExecutionPlan = createServer.createServerFlow().getExecutionPlan();
		String createServerFlowUuid = createServerExecutionPlan.getFlowUuid();

		List<NavigationMatcher<Serializable>> navigationMatchers = new ArrayList<>();

		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.DEFAULT, getMultiInstanceResponseStepId));
		builder.addStep(splitContextsStepId, OPENSTACK_UTILS_CLASS, SPLIT_SERVERS_INTO_BRANCH_CONTEXTS_METHOD, createServerSplitId);
		builder.addMultiInstance(createServerSplitId, createServerJoinId, createServerFlowUuid, navigationMatchers);

		navigationMatchers = new ArrayList<>();
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.EQUAL, RESPONSE_KEY, SUCCESS_RESPONSE, successStepId));
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.DEFAULT, failureStepId));

		builder.addStep(getMultiInstanceResponseStepId, OPENSTACK_UTILS_CLASS, GET_MULTI_INSTANCE_RESPONSE_METHOD, navigationMatchers);

		createSuccessStep(builder, successStepId);
		createFailureStep(builder, failureStepId);

		ExecutionPlan multiInstanceFlow = builder.getExecutionPlan();
		multiInstanceFlow.setSubflowsUUIDs(Sets.newHashSet(createServerFlowUuid));
		Map<String, ExecutionPlan> dependencies = new HashMap<>();
		dependencies.put(createServerFlowUuid, createServerExecutionPlan);
		Map<String, Serializable> getRuntimeValues = new HashMap<>();

		return TriggeringProperties.create(multiInstanceFlow).
				setDependencies(dependencies).setRuntimeValues(getRuntimeValues).setStartStep(0L);
	}




}
