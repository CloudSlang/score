package org.score.samples.openstack;

import org.score.samples.openstack.actions.ExecutionPlanBuilder;
import org.score.samples.openstack.actions.MatchType;
import org.score.samples.openstack.actions.NavigationMatcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
	public static final String IDENTITY_PORT_MESSAGE = "Identity Port";
	public static final String COMPUTE_PORT_MESSAGE = "Compute Port";
	public static final String OPENSTACK_USERNAME_MESSAGE = "OpenStack Username";
	public static final String OPENSTACK_PASSWORD_MESSAGE = "OpenStack Password";
	public static final String FLOW_DESCRIPTION = "flowDescription";
	public static final String BODY_KEY = "body";
	public static final String URL_KEY = "url";
	public static final String CONTENT_TYPE_KEY = "contentType";
	public static final String METHOD_KEY = "method";
	public static final String COMPUTE_PORT_KEY = "computePort";
	public static final String HOST_KEY = "host";

	public static void createGetTokenStep(
			ExecutionPlanBuilder builder,
			Long stepId,
			Long successStepId,
			Long failureStepId){
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
			Long nextStepId) {
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
		System.out.print(inputName + " (default value = " + defaultValue +" --hit Enter): ");
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
}
