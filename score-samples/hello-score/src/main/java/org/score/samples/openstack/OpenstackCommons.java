package org.score.samples.openstack;

import org.apache.commons.collections.list.SetUniqueList;
import org.score.samples.openstack.actions.ExecutionPlanBuilder;
import org.score.samples.openstack.actions.InputBinding;
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
	public static final String FINAL_STEP_ACTIONS_CLASS = "org.score.samples.openstack.actions.FinalStepActions";
	public static final String SUCCESS_STEP_ACTION = "successStepAction";
	public static final String FAILURE_STEP_ACTION = "failureStepAction";
	public static final String HTTP_CLIENT_ACTION_CLASS = "org.score.content.httpclient.HttpClientAction";
	public static final String HTTP_CLIENT_ACTION_METHOD = "execute";
	public static final String SERVER_NAME_KEY = "serverName";
	public static final String CONTEXT_MERGER_CLASS = "org.score.samples.openstack.actions.ContextMerger";
	public static final String SEND_EMAIL_CLASS = "org.score.samples.openstack.actions.SimpleSendEmail";
	public static final String SEND_EMAIL_METHOD = "execute";
	public static final String OPENSTACK_HOST_MESSAGE = "OpenStack Host";
	public static final String OPENSTACK_USERNAME_MESSAGE = "OpenStack Username";
	public static final String OPENSTACK_PASSWORD_MESSAGE = "OpenStack Password";
	public static final String FLOW_DESCRIPTION = "flowDescription";
	public static final String COMPUTE_PORT_KEY = "computePort";
	public static final String IDENTITY_PORT_KEY = "identityPort";
	public static final String HOST_KEY = "host";
	public static final String DEFUALT_IDENTITY_PORT = "5000";
	public static final String DEFAULT_COMPUTE_PORT = "8774";
	public static final String DEFAULT_IMAGE_REF = "56ff0279-f1fb-46e5-93dc-fe7093af0b1a";
	public static final String USERNAME_KEY = "username";
	public static final String PASSWORD_KEY = "password";
	public static final String IMAGE_REFERENCE_KEY = "imgRef";
	public static final String IDENTITY_PORT_MESSAGE = "Identity port";
	public static final String COMPUTE_PORT_MESSAGE = "Compute port";
	public static final String OPEN_STACK_IMAGE_REFERENCE_MESSAGE = "OpenStack image reference";
	public static final String SERVER_NAME_MESSAGE = "Server name";
	public static final String PREPARE_GET_TOKEN_METHOD = "prepareGetToken";
	public static final String GET_SERVER_NAMES_METHOD = "getServerNames";
	public static final String PREPARE_DELETE_SERVER_METHOD = "prepareDeleteServer";
	public static final String GET_SERVER_ID_METHOD = "getServerId";
	public static final String PREPARE_SEND_EMAIL_METHOD = "prepareSendEmail";

	public static void createGetTokenStep(
			ExecutionPlanBuilder builder,
			Long stepId,
			Long successStepId,
			Long failureStepId
	) {
		List<NavigationMatcher<Serializable>> navigationMatchers = new ArrayList<>();

		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.EQUAL, "statusCode", "200", successStepId));
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.DEFAULT, failureStepId));

		builder.addOOActionStep(stepId, "org.score.content.httpclient.HttpClientAction", "execute", null, navigationMatchers);
	}

	public static void createSuccessStep(ExecutionPlanBuilder builder, Long successStepId) {
		//success step
		builder.addOOActionFinalStep(successStepId, FINAL_STEP_ACTIONS_CLASS, SUCCESS_STEP_ACTION);
	}

	public static void createFailureStep(ExecutionPlanBuilder builder, Long failureStepId) {
		//failure step
		builder.addOOActionFinalStep(failureStepId, FINAL_STEP_ACTIONS_CLASS, FAILURE_STEP_ACTION);
	}

	public static void createPrepareGetServersStep(
			ExecutionPlanBuilder builder,
			Long stepId,
			Long nextStepId
	) {
		builder.addStep(stepId, CONTEXT_MERGER_CLASS, "prepareGetServer", nextStepId);
	}

	public static void createGetServersStep(ExecutionPlanBuilder builder, Long stepId, Long successStepId, Long failureStepId) {
		List<NavigationMatcher<Serializable>> navigationMatchers = new ArrayList<>();
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.EQUAL, "statusCode", "200", successStepId));
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.DEFAULT, failureStepId));
		builder.addOOActionStep(stepId, HTTP_CLIENT_ACTION_CLASS, HTTP_CLIENT_ACTION_METHOD, null, navigationMatchers);
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
		} catch (IOException ioe) {
			System.out.println("IO error trying to read command");
			System.exit(1);
		}
		return line;
	}

	public static Map<String, Serializable> prepareExecutionContext(List<InputBinding> inputBindings) {
		Map<String, Serializable> executionContext = new HashMap<>();

		for (InputBinding inputBinding : inputBindings) {
			executionContext.put(inputBinding.getInputKey(), inputBinding.getValue());
		}

		return executionContext;
	}

	public static void createPrepareGetTokenStep(ExecutionPlanBuilder builder, Long stepId, Long nextStepId) {
		builder.addStep(stepId, CONTEXT_MERGER_CLASS, PREPARE_GET_TOKEN_METHOD, nextStepId);
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
