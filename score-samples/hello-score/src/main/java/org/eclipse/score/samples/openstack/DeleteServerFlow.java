/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package org.eclipse.score.samples.openstack;

import org.eclipse.score.api.TriggeringProperties;
import org.eclipse.score.samples.openstack.actions.ExecutionPlanBuilder;
import org.eclipse.score.samples.openstack.actions.InputBinding;
import org.eclipse.score.samples.openstack.actions.InputBindingFactory;
import org.eclipse.score.samples.openstack.actions.MatchType;
import org.eclipse.score.samples.openstack.actions.NavigationMatcher;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.eclipse.score.samples.openstack.OpenstackCommons.*;

/**
 * Date: 8/29/2014
 *
 * @author Bonczidai Levente
 */
public class DeleteServerFlow {
	private List<InputBinding> inputBindings;

	public DeleteServerFlow() {
		inputBindings = generateInitialInputBindings();
	}

	public TriggeringProperties deleteServerFlow(){
		ExecutionPlanBuilder builder = new ExecutionPlanBuilder();

		Long prepareGetTokenId = 0L;
		Long authenticationStepId = 1L;
		Long parseAuthenticationStepId = 2L;
		Long mergerStepId = 3L;
		Long getServersStepId = 4L;
		Long getServerIdStepId = 5L;
		Long secondMergerStepId = 6L;
		Long deleteServerStepId = 7L;
		Long successStepId = 8L;
		Long failureStepId = 9L;
		Long prepareParseAuthenticationStepId = 10L;
		Long prepareGetServerIdStepId = 11L;

		createPrepareGetAuthenticationStep(builder, prepareGetTokenId, authenticationStepId);

		createGetAuthenticationStep(builder, authenticationStepId, prepareParseAuthenticationStepId, failureStepId);

		createPrepareParseAuthenticationStep(builder, prepareParseAuthenticationStepId, parseAuthenticationStepId);

		createParseAuthenticationStep(builder, parseAuthenticationStepId, mergerStepId, failureStepId);

		createPrepareGetServersStep(builder, mergerStepId, getServersStepId);

		createGetServersStep(builder, getServersStepId, prepareGetServerIdStepId, failureStepId);

		createPrepareGetServerIdStep(builder, prepareGetServerIdStepId, getServerIdStepId);

		createGetServerIdStep(builder, getServerIdStepId, secondMergerStepId, failureStepId);

		createCreatePrepareDeleteServerStep(builder, secondMergerStepId, deleteServerStepId);

		createDeleteServerStep(builder, deleteServerStepId, successStepId, failureStepId);

		createSuccessStep(builder, successStepId);

		createFailureStep(builder, failureStepId);

		Map<String, Serializable> context = new HashMap<>();
		context.put(FLOW_DESCRIPTION, "Delete Server");
		builder.setInitialExecutionContext(context);

		builder.setBeginStep(0L);

		return builder.createTriggeringProperties();
	}

	public List<InputBinding> getInputBindings() {
		return inputBindings;
	}

	private List<InputBinding> generateInitialInputBindings() {
		List<InputBinding> bindings = new ArrayList<>(6);

		bindings.add(InputBindingFactory.createInputBinding(OPENSTACK_HOST_MESSAGE, HOST_KEY, true));
		bindings.add(InputBindingFactory.createInputBindingWithDefaultValue(IDENTITY_PORT_MESSAGE, IDENTITY_PORT_KEY, true, DEFAULT_IDENTITY_PORT));
		bindings.add(InputBindingFactory.createInputBindingWithDefaultValue(COMPUTE_PORT_MESSAGE, COMPUTE_PORT_KEY, true, DEFAULT_COMPUTE_PORT));
		bindings.add(InputBindingFactory.createInputBinding(OPENSTACK_USERNAME_MESSAGE, USERNAME_KEY, true));
		bindings.add(InputBindingFactory.createInputBinding(OPENSTACK_PASSWORD_MESSAGE, PASSWORD_KEY, true));
		bindings.add(InputBindingFactory.createInputBinding(SERVER_NAME_MESSAGE, SERVER_NAME_KEY, true));

		return bindings;
	}

	private void createDeleteServerStep(ExecutionPlanBuilder builder, Long stepId, Long nextStepId, Long defaultStepId) {
		List<NavigationMatcher<Serializable>> navigationMatchers = new ArrayList<>(2);

		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.EQUAL, RETURN_CODE, "0", nextStepId));
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.DEFAULT, defaultStepId));

		builder.addOOActionStep(stepId, HTTP_CLIENT_ACTION_CLASS, HTTP_CLIENT_ACTION_METHOD, null, navigationMatchers);
	}

	private void createParseAuthenticationStep(ExecutionPlanBuilder builder, Long stepId, Long successStepId, Long failureStepId) {
		List<NavigationMatcher<Serializable>> navigationMatchers = new ArrayList<>(2);

		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.EQUAL, RETURN_CODE, SUCCESS_CODE, successStepId));
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.DEFAULT, failureStepId));

		builder.addStep(stepId, OPENSTACK_UTILS_CLASS, PARSE_AUTHENTICATION_METHOD, navigationMatchers);
	}


	private void createCreatePrepareDeleteServerStep(ExecutionPlanBuilder builder, Long stepId, Long nextStepId) {
		List<InputBinding> inputs = new ArrayList<>(3);
		String deleteURL = "http://${" + HOST_KEY + "}:${" + COMPUTE_PORT_KEY + "}/v2/${" + TENANT_KEY + "}/servers/${" + RETURN_RESULT_KEY + "}";

		inputs.add(InputBindingFactory.createMergeInputBindingWithValue(URL_KEY, deleteURL));
		inputs.add(InputBindingFactory.createMergeInputBindingWithValue(METHOD_KEY, "delete"));
		inputs.add(InputBindingFactory.createMergeInputBindingWithValue(HEADERS_KEY, "X-AUTH-TOKEN: ${" + TOKEN_KEY + "}"));
		builder.addStep(stepId, CONTEXT_MERGER_CLASS, MERGE_METHOD, inputs, nextStepId);
	}

	private void createGetServerIdStep(ExecutionPlanBuilder builder, Long stepId, Long successStepId, Long failureStepId) {
		List<NavigationMatcher<Serializable>> navigationMatchers = new ArrayList<>(2);

		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.EQUAL, RETURN_CODE, SUCCESS_CODE, successStepId));
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.DEFAULT, failureStepId));

		builder.addStep(stepId, OPENSTACK_UTILS_CLASS, GET_SERVER_ID_METHOD, navigationMatchers);

	}


}
