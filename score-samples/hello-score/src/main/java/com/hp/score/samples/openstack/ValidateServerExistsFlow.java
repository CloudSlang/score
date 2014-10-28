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

import static com.hp.score.samples.openstack.actions.FinalStepActions.RESPONSE_KEY;
import static com.hp.score.samples.openstack.actions.FinalStepActions.SUCCESS;
import static com.hp.score.samples.openstack.actions.StringOccurrenceCounter.RETURN_RESULT;

/**
 * Date: 8/29/2014
 *
 * @author Bonczidai Levente
 */
public class ValidateServerExistsFlow {
	private List<InputBinding> inputBindings;

	public ValidateServerExistsFlow() {
		inputBindings = generateInitialInputBindings();
	}

	private List<InputBinding> generateInitialInputBindings() {
		List<InputBinding> bindings = new ArrayList<>();

		bindings.addAll(new ListServersFlow().getInputBindings());
		bindings.add(InputBindingFactory.createInputBinding(OpenstackCommons.SERVER_NAME_MESSAGE, OpenstackCommons.SERVER_NAME_KEY, true));

		return bindings;
	}

	public TriggeringProperties validateServerExistsFlow() {
		ExecutionPlanBuilder builder = new ExecutionPlanBuilder("validate");
		Long splitId = 0L;
		Long joinId = 1L;
		Long successId = 2L;
		Long failureId = 3L;
		Long prepareStringOccurrencesId = 4L;
		Long stringOccurencesId = 5L;
		Long resultFormatterId = 6L;

		createGetServersSubflow(builder, splitId, joinId, failureId, prepareStringOccurrencesId);

		createPrepareStringOccurencesStep(builder, prepareStringOccurrencesId, stringOccurencesId);

		createStringOccurencesStep(builder, stringOccurencesId, successId, resultFormatterId);

		OpenstackCommons.createSuccessStep(builder, successId);

		createResultFormatterStepForFailure(builder, failureId, resultFormatterId);

		OpenstackCommons.createFailureStep(builder, failureId);

		Map<String, Serializable> context = new HashMap<>();
		context.put(OpenstackCommons.FLOW_DESCRIPTION, "Validate servers");
		builder.setInitialExecutionContext(context);

		builder.setBeginStep(0L);

		return builder.createTriggeringProperties();
	}

	public List<InputBinding> getInputBindings() {
		return inputBindings;
	}
	private void createGetServersSubflow(ExecutionPlanBuilder builder, Long splitId, Long joinId, Long failureId, Long prepareStringOccurrencesId) {
		//get servers
		List<NavigationMatcher<Serializable>> navigationMatchers = new ArrayList<>(2);
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.EQUAL, RESPONSE_KEY, SUCCESS, prepareStringOccurrencesId));
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.DEFAULT, failureId));
		ListServersFlow listServersFlow = new ListServersFlow();
		TriggeringProperties triggeringProperties = listServersFlow.listServersFlow();
		List<String> inputKeys = new ArrayList<>();
		for (InputBinding inputBinding : listServersFlow.getInputBindings()) {
			inputKeys.add(inputBinding.getSourceKey());
		}

		builder.addSubflow(splitId, joinId, triggeringProperties, inputKeys, navigationMatchers);
	}
	private void createStringOccurencesStep(ExecutionPlanBuilder builder, Long stringOccurencesId, Long successId, Long resultFormatterId) {
		List<NavigationMatcher<Serializable>> navigationMatchers;//string occurrence
		navigationMatchers = new ArrayList<>(2);
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.COMPARE_GREATER, RETURN_RESULT, "0", successId));
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.DEFAULT, resultFormatterId));
		builder.addOOActionStep(stringOccurencesId,
				OpenstackCommons.STRING_OCCURRENCE_COUNTER_CLASS,
				OpenstackCommons.EXECUTE_METHOD,
				null,
				navigationMatchers);
	}
	private void createPrepareStringOccurencesStep(ExecutionPlanBuilder builder, Long prepareStringOccurrencesId, Long stringOccurencesId) {
		//prepare string occurrences
		List<InputBinding> inputs = new ArrayList<>(3);

		inputs.add(InputBindingFactory.createMergeInputBindingWithSource("container", OpenstackCommons.RETURN_RESULT_KEY));
		inputs.add(InputBindingFactory.createMergeInputBindingWithSource("toFind", OpenstackCommons.SERVER_NAME_KEY));
		inputs.add(InputBindingFactory.createMergeInputBindingWithValue("ignoreCase", "true"));

		builder.addStep(prepareStringOccurrencesId, OpenstackCommons.CONTEXT_MERGER_CLASS, OpenstackCommons.MERGE_METHOD, inputs, stringOccurencesId);
	}
	private void createResultFormatterStepForFailure(ExecutionPlanBuilder builder, Long failureId, Long resultFormatterId) {
		//result formatter step
		builder.addStep(resultFormatterId, OpenstackCommons.CONTEXT_MERGER_CLASS, OpenstackCommons.VALIDATE_SERVER_RESULT_METHOD, failureId);
	}
}
