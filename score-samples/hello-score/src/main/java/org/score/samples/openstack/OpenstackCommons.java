package org.score.samples.openstack;

import org.apache.commons.collections.list.SetUniqueList;
import org.score.samples.openstack.actions.ExecutionPlanBuilder;
import org.score.samples.openstack.actions.InputBinding;

import org.score.samples.openstack.actions.InputBindingFactory;
import org.score.samples.openstack.actions.MatchType;
import org.score.samples.openstack.actions.NavigationMatcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Date: 8/27/2014
 *
 * @author Bonczidai Levente
 */
public class OpenstackCommons {
	

	public static final String SERVER_NAME_KEY = "serverName";
	public static final String FROM_KEY = "from";
	public static final String EMAIL_PORT_KEY = "emailPort";
	public static final String EMAIL_HOST_KEY = "emailHost";
	public static final String BODY_KEY = "body";
	public static final String URL_KEY = "url";
	public static final String TO_KEY = "to";
	public static final String CONTENT_TYPE_KEY = "contentType";
	public static final String METHOD_KEY = "method";
	public static final String FINAL_STEP_ACTIONS_CLASS = "org.score.samples.openstack.actions.FinalStepActions";
	public static final String CONTEXT_MERGER_CLASS = "org.score.samples.openstack.actions.ContextMerger";
	public static final String OPENSTACK_UTILS_CLASS = "org.score.samples.openstack.actions.OpenstackUtils";
	public static final String SEND_EMAIL_CLASS = "org.score.samples.openstack.actions.SimpleSendEmail";
	public static final String HTTP_CLIENT_ACTION_CLASS = "com.hp.score.content.httpclient.HttpClientAction";
	public static final String STRING_OCCURRENCE_COUNTER_CLASS = "org.score.samples.openstack.actions.StringOccurrenceCounter";
	public static final String SUCCESS_STEP_ACTION_METHOD = "successStepAction";
	public static final String FAILURE_STEP_ACTION_METHOD = "failureStepAction";
	public static final String HTTP_CLIENT_ACTION_METHOD = "execute";
	public static final String PARSE_AUTHENTICATION_METHOD = "parseAuthentication";
	public static final String MERGE_METHOD = "merge";
	public static final String VALIDATE_SERVER_RESULT_METHOD = "validateServerResult";
	public static final String EXECUTE_METHOD = "execute";
	public static final String SEND_EMAIL_METHOD = "execute";
	public static final String OPENSTACK_HOST_MESSAGE = "OpenStack Host";
	public static final String OPENSTACK_USERNAME_MESSAGE = "OpenStack Username";
	public static final String OPENSTACK_PASSWORD_MESSAGE = "OpenStack Password";
	public static final String FLOW_DESCRIPTION = "flowDescription";
	public static final String POST_METHOD_TYPE = "post";
	public static final String JSON_CONTENT_TYPE = "application/json";
	public static final String COMPUTE_PORT_KEY = "computePort";
	public static final String IDENTITY_PORT_KEY = "identityPort";
	public static final String SUBJECT_KEY = "subject";
	public static final String HOST_KEY = "host";
	public static final String DEFAULT_IDENTITY_PORT = "5000";
	public static final String DEFAULT_COMPUTE_PORT = "8774";
	public static final String DEFAULT_IMAGE_REF = "56ff0279-f1fb-46e5-93dc-fe7093af0b1a";
	public static final String USERNAME_KEY = "username";
	public static final String PASSWORD_KEY = "password";
	public static final String PARSED_TOKEN_KEY = "parsedToken";
	public static final String TOKEN_KEY = "token";
	public static final String TENANT_KEY = "tenant";
	public static final String HEADERS_KEY = "headers";
	public static final String PORT_KEY = "port";
	public static final String RETURN_RESULT_KEY = "returnResult";
	public static final String PARSED_TENANT_KEY = "parsedTenant";
	public static final String IMAGE_REFERENCE_KEY = "imgRef";
	public static final String IDENTITY_PORT_MESSAGE = "Identity port";
	public static final String COMPUTE_PORT_MESSAGE = "Compute port";
	public static final String OPEN_STACK_IMAGE_REFERENCE_MESSAGE = "OpenStack image reference";
	public static final String SERVER_NAME_MESSAGE = "Server name";
	public static final String STATUS_CODE = "statusCode";
	public static final String RETURN_CODE = "returnCode";
	public static final String GET_SERVER_NAMES_METHOD = "getServerNames";
	public static final String GET_SERVER_ID_METHOD = "getServerId";
	public static final String OPEN_STACK_HEALTH_CHECK_SERVER_NAME = "health_check_server";
	public static final String JSON_AUTHENTICATION_RESPONSE_KEY = "jsonAuthenticationResponse";
	public static final String GET_SERVERS_RESPONSE_KEY = "getServersResponse";
	public static final String SUCCESS = "0";


	public static void createPrepareGetServerIdStep(ExecutionPlanBuilder builder, Long stepId, Long nextStepId){
		List<InputBinding> inputs  = new ArrayList<>(1);

		inputs.add(InputBindingFactory.createMergeInputBindingWithSource(GET_SERVERS_RESPONSE_KEY, RETURN_RESULT_KEY));

		builder.addStep(stepId, CONTEXT_MERGER_CLASS, MERGE_METHOD, inputs, nextStepId);
	}

	public static void createPrepareParseAuthenticationStep(ExecutionPlanBuilder builder, Long stepId,
															Long nextStepId){
		List<InputBinding> inputs  = new ArrayList<>(1);

		inputs.add(InputBindingFactory.createMergeInputBindingWithSource(JSON_AUTHENTICATION_RESPONSE_KEY, RETURN_RESULT_KEY));

		builder.addStep(stepId, CONTEXT_MERGER_CLASS, MERGE_METHOD, inputs, nextStepId);
	}


	public static void createGetAuthenticationStep(
			ExecutionPlanBuilder builder,
			Long stepId,
			Long successStepId,
			Long failureStepId
	) {
		List<NavigationMatcher<Serializable>> navigationMatchers = new ArrayList<>(2);

		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.EQUAL, STATUS_CODE, "200", successStepId));
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.DEFAULT, failureStepId));

		builder.addOOActionStep(stepId, HTTP_CLIENT_ACTION_CLASS, EXECUTE_METHOD, null, navigationMatchers);
	}

	public static void createSuccessStep(ExecutionPlanBuilder builder, Long successStepId) {
		//success step
		builder.addOOActionFinalStep(successStepId, FINAL_STEP_ACTIONS_CLASS, SUCCESS_STEP_ACTION_METHOD);
	}

	public static void createFailureStep(ExecutionPlanBuilder builder, Long failureStepId) {
		//failure step
		builder.addOOActionFinalStep(failureStepId, FINAL_STEP_ACTIONS_CLASS, FAILURE_STEP_ACTION_METHOD);
	}

	public static void createPrepareGetServersStep(
			ExecutionPlanBuilder builder,
			Long stepId,
			Long nextStepId) {
		List<InputBinding> inputs  = new ArrayList<>(5);

		String url = "http://${" + HOST_KEY + "}:${" + COMPUTE_PORT_KEY + "}/v2/${" + PARSED_TENANT_KEY + "}/servers";

		inputs.add(InputBindingFactory.createMergeInputBindingWithValue(URL_KEY, url));
		inputs.add(InputBindingFactory.createMergeInputBindingWithValue(HEADERS_KEY, "X-AUTH-TOKEN: ${" + PARSED_TOKEN_KEY + "}"));
		inputs.add(InputBindingFactory.createMergeInputBindingWithValue(METHOD_KEY, "get"));
		inputs.add(InputBindingFactory.createMergeInputBindingWithSource(TOKEN_KEY, PARSED_TOKEN_KEY));
		inputs.add(InputBindingFactory.createMergeInputBindingWithSource(TENANT_KEY, PARSED_TENANT_KEY));

		builder.addStep(stepId, CONTEXT_MERGER_CLASS, MERGE_METHOD, inputs, nextStepId);
	}

	public static void createGetServersStep(ExecutionPlanBuilder builder, Long stepId, Long successStepId, Long failureStepId) {
		List<NavigationMatcher<Serializable>> navigationMatchers = new ArrayList<>(2);
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.EQUAL, STATUS_CODE, "200", successStepId));
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.DEFAULT, failureStepId));
		builder.addOOActionStep(stepId, HTTP_CLIENT_ACTION_CLASS, EXECUTE_METHOD, null, navigationMatchers);
	}

	public static String readInput(BufferedReader reader, String inputName) {
		System.out.print(inputName + ": ");
		return readLine(reader);
	}

	public static String readPredefinedInput(BufferedReader reader, String inputName, String defaultValue) {
		System.out.print(inputName + " (default value = " + defaultValue + " --hit Enter): ");
		return readLine(reader);
	}

	private static String readLine(BufferedReader reader) {
		String line = null;
		try {
			line = reader.readLine();
		} catch (IOException ignore) {
			System.out.println("IO error trying to read command");
			System.exit(1);
		}
		return line;
	}

	public static Map<String, Serializable> prepareExecutionContext(List<InputBinding> inputBindings) {
		Map<String, Serializable> executionContext = new HashMap<>();

		for (InputBinding inputBinding : inputBindings) {
			executionContext.put(inputBinding.getSourceKey(), inputBinding.getValue());
		}

		return executionContext;
	}

	public static void createPrepareGetAuthenticationStep(ExecutionPlanBuilder builder, Long stepId, Long nextStepId) {
		List<InputBinding> inputs = new ArrayList<>();

		String url = "http://${" + HOST_KEY + "}:${" + IDENTITY_PORT_KEY + "}/v2.0/tokens";
		String body = "{\"auth\": {\"tenantName\": \"demo\",\"passwordCredentials\": {\"username\": \"${" + USERNAME_KEY +"}\",\"password\": \"${" + PASSWORD_KEY + "}\"}}}";
		inputs.add(InputBindingFactory.createMergeInputBindingWithValue(URL_KEY, url));
		inputs.add(InputBindingFactory.createMergeInputBindingWithValue(BODY_KEY, body));
		inputs.add(InputBindingFactory.createMergeInputBindingWithValue(METHOD_KEY, POST_METHOD_TYPE));
		inputs.add(InputBindingFactory.createMergeInputBindingWithValue(CONTENT_TYPE_KEY, JSON_CONTENT_TYPE));

		builder.addStep(stepId, CONTEXT_MERGER_CLASS, MERGE_METHOD, inputs, nextStepId);

	}

	@SuppressWarnings("unchecked")
	public static List<InputBinding> mergeInputsWithoutDuplicates(List<InputBinding>... bindingLists) {
		SetUniqueList uniqueList = SetUniqueList.decorate(new ArrayList<>());
		for (List<InputBinding> bindingList : bindingLists) {
			uniqueList.addAll(bindingList);
		}
		return uniqueList.subList(0, uniqueList.size());
	}
}
