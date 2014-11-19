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
 * Date: 8/28/2014
 *
 * @author Bonczidai Levente
 */
@SuppressWarnings("unused")
public class CreateServerFlow {
	private List<InputBinding> inputBindings;

	public CreateServerFlow() {
		inputBindings = generateInitialInputBindings();
	}

	private List<InputBinding> generateInitialInputBindings() {
		List<InputBinding> bindings = new ArrayList<>(7);

		bindings.add(InputBindingFactory.createInputBinding(OPENSTACK_HOST_MESSAGE, HOST_KEY, true));
		bindings.add(InputBindingFactory.createInputBindingWithDefaultValue(IDENTITY_PORT_MESSAGE, IDENTITY_PORT_KEY, true, DEFAULT_IDENTITY_PORT));
		bindings.add(InputBindingFactory.createInputBindingWithDefaultValue(COMPUTE_PORT_MESSAGE, COMPUTE_PORT_KEY, true, DEFAULT_COMPUTE_PORT));
		bindings.add(InputBindingFactory.createInputBindingWithDefaultValue(OPEN_STACK_IMAGE_REFERENCE_MESSAGE, IMAGE_REFERENCE_KEY, true, DEFAULT_IMAGE_REF));
		bindings.add(InputBindingFactory.createInputBinding(OPENSTACK_USERNAME_MESSAGE, USERNAME_KEY, true));
		bindings.add(InputBindingFactory.createInputBinding(OPENSTACK_PASSWORD_MESSAGE, PASSWORD_KEY, true));
		bindings.add(InputBindingFactory.createInputBinding(SERVER_NAME_MESSAGE, SERVER_NAME_KEY, true));

		return bindings;
	}

	public TriggeringProperties createServerFlow() {
		ExecutionPlanBuilder builder = new ExecutionPlanBuilder();

		Long prepareGetTokenId = 0L;
		Long authenticationStepId = 1L;
		Long parseAuthenticationStepId = 2L;
		Long contextMergerStepId = 3L;
		Long createServerStepId = 4L;
		Long successStepId = 5L;
		Long failureStepId = 6L;
		Long prepareParseAuthenticationStepId = 7L;

		createPrepareGetAuthenticationStep(builder, prepareGetTokenId, authenticationStepId);

		createGetAuthenticationStep(builder, authenticationStepId, prepareParseAuthenticationStepId, failureStepId);

		createPrepareParseAuthenticationStep(builder, prepareParseAuthenticationStepId, parseAuthenticationStepId);

		createParseAuthenticationStep(builder, parseAuthenticationStepId, contextMergerStepId, failureStepId);

		prepareCreateServerStep(builder, contextMergerStepId, createServerStepId);

		createServerStep(builder, createServerStepId, successStepId, failureStepId);

		createSuccessStep(builder, successStepId);

		createFailureStep(builder, failureStepId);

		Map<String, Serializable> context = new HashMap<>();
		context.put(FLOW_DESCRIPTION, "Create Server");
		builder.setInitialExecutionContext(context);

		builder.setBeginStep(0L);

		return builder.createTriggeringProperties();
	}

	private void createParseAuthenticationStep(ExecutionPlanBuilder builder, Long stepId, Long successStepId, Long failureStepId) {
		List<NavigationMatcher<Serializable>> navigationMatchers = new ArrayList<>(2);

		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.EQUAL, RETURN_CODE, SUCCESS_CODE, successStepId));
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.DEFAULT, failureStepId));

		builder.addStep(stepId, OPENSTACK_UTILS_CLASS, PARSE_AUTHENTICATION_METHOD, navigationMatchers);
	}

	private void prepareCreateServerStep(
			ExecutionPlanBuilder builder,
			Long stepId,
			Long nextStepId) {
		List<InputBinding> inputs = new ArrayList<>(3);

		String url = "http://${" + HOST_KEY + "}:${" + COMPUTE_PORT_KEY + "}/v2/${" + PARSED_TENANT_KEY + "}/servers";

		@SuppressWarnings("StringBufferReplaceableByString")
		StringBuilder body = new StringBuilder("");

		body.append("{\"server\": {\"name\": \"${" +SERVER_NAME_KEY + "}\",");
		body.append("\"imageRef\": \"${"+ IMAGE_REFERENCE_KEY +"}\",");
		body.append("\"flavorRef\": \"2\",");
		body.append("\"max_count\": 1,");
		body.append("\"min_count\": 1,");
		body.append("\"security_groups\": [{\"name\": \"default\"}]}}");

		inputs.add(InputBindingFactory.createMergeInputBindingWithValue(URL_KEY, url));
		inputs.add(InputBindingFactory.createMergeInputBindingWithValue(BODY_KEY, body.toString()));
		inputs.add(InputBindingFactory.createMergeInputBindingWithValue(HEADERS_KEY, "X-AUTH-TOKEN: ${" + PARSED_TOKEN_KEY + "}"));

		builder.addStep(stepId, CONTEXT_MERGER_CLASS, MERGE_METHOD, inputs,nextStepId);
	}

	private void createServerStep(
			ExecutionPlanBuilder builder,
			Long stepId,
			Long successStepId,
			Long failureStepId){
		List<NavigationMatcher<Serializable>> navigationMatchers = new ArrayList<>(2);

		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.EQUAL, STATUS_CODE, "202", successStepId));
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.DEFAULT, failureStepId));

		builder.addOOActionStep(stepId, HTTP_CLIENT_ACTION_CLASS, HTTP_CLIENT_ACTION_METHOD, null, navigationMatchers);
	}

	public List<InputBinding> getInputBindings() {
		return inputBindings;
	}
}
