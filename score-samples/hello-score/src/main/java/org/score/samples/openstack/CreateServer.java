package org.score.samples.openstack;

import com.hp.score.api.ExecutionPlan;
import com.hp.score.api.TriggeringProperties;
import org.apache.commons.lang3.StringUtils;
import org.score.samples.openstack.actions.ExecutionPlanBuilder;
import org.score.samples.openstack.actions.MatchType;
import org.score.samples.openstack.actions.NavigationMatcher;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.score.samples.openstack.OpenstackCommons.*;

/**
 * Date: 8/27/2014
 *
 * @author Bonczidai Levente
 */
@SuppressWarnings("unused")
public class CreateServer {
	private String username;
	private String password;
	private String host;
	private String identityPort;
	private String computePort;
	private String serverName;

	public TriggeringProperties createServersFlow() {
		TriggeringProperties triggeringProperties = TriggeringProperties.create(prepareCreateServerExecutionPlan());
		Map<String, Serializable> context = prepareCreateServerExecutionContext();
		context.put(FLOW_DESCRIPTION, "Create Server");
		triggeringProperties.setContext(context);
		triggeringProperties.setStartStep(0L);
		return triggeringProperties;
	}

	public ExecutionPlan prepareCreateServerExecutionPlan() {
		ExecutionPlanBuilder builder = new ExecutionPlanBuilder();

		Long tokenStepId = 0L;
		Long contextMergerStepId = 1L;
		Long createServerStepId = 2L;
		Long successStepId = 3L;
		Long failureStepId = 4L;

		createGetTokenStep(builder, tokenStepId, contextMergerStepId, failureStepId);

		prepareCreateServerStep(builder, contextMergerStepId, createServerStepId);

		createServerStep(builder, createServerStepId, successStepId, failureStepId);

		createSuccessStep(builder, successStepId);

		createFailureStep(builder, failureStepId);

		return builder.createTriggeringProperties().getExecutionPlan();
	}

	public Map<String, Serializable> prepareCreateServerExecutionContext() {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		String username;
		String password;
		String host;
		String identityPort;
		String computePort;
		String serverName;
		String imageRef;


		String defualtIdentityPort =  "5000";
		String defaultComputePort = "8774";
		String defaultImageRef = "56ff0279-f1fb-46e5-93dc-fe7093af0b1a";

		host = readInput(reader, OPENSTACK_HOST_MESSAGE);
		identityPort = readPredefinedInput(reader, IDENTITY_PORT_MESSAGE, defualtIdentityPort);
		computePort = readPredefinedInput(reader, COMPUTE_PORT_MESSAGE, defaultComputePort);
		imageRef = readPredefinedInput(reader, "ImageRef", defaultImageRef);
		username = readInput(reader, OPENSTACK_USERNAME_MESSAGE);
		password = readInput(reader, OPENSTACK_PASSWORD_MESSAGE);
		serverName = readInput(reader, "Server name");

		if (StringUtils.isEmpty(host)){
			host = "";
		}
		if (StringUtils.isEmpty(identityPort)){
			identityPort = defualtIdentityPort;
		}
		if (StringUtils.isEmpty(computePort)){
			computePort = defaultComputePort;
		}
		if (StringUtils.isEmpty(imageRef)){
			imageRef = defaultImageRef;
		}

		this.username = username;
		this.password = password;
		this.host = host;
		this.identityPort = identityPort;
		this.computePort = computePort;
		this.serverName = serverName;

		Map<String, Serializable> executionContext = new HashMap<>();
		String url = "http://" + host + ":" + identityPort + "/v2.0/tokens";
		String body = "{\"auth\": {\"tenantName\": \"demo\",\"passwordCredentials\": {\"username\": \"" + username +"\",\"password\": \"" + password + "\"}}}";
		executionContext.put(URL_KEY, url);
		executionContext.put(METHOD_KEY, "post");
		executionContext.put(BODY_KEY, body);
		executionContext.put(CONTENT_TYPE_KEY, "application/json");
		executionContext.put(SERVER_NAME_KEY, serverName);
		executionContext.put(COMPUTE_PORT_KEY, computePort);
		executionContext.put("imageRef", imageRef);
		executionContext.put(HOST_KEY, host);

		return executionContext;
	}

	private void prepareCreateServerStep(
			ExecutionPlanBuilder builder,
			Long stepId,
			Long nextStepId) {

		builder.addStep(stepId, CONTEXT_MERGER_CLASS, "prepareCreateServer", nextStepId);
	}

	private void createServerStep(
			ExecutionPlanBuilder builder,
			Long stepId,
			Long successStepId,
			Long failureStepId){
		List<NavigationMatcher<Serializable>> navigationMatchers = new ArrayList<>();

		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.EQUAL, "statusCode", "202", successStepId));
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.DEFAULT, failureStepId));

		builder.addOOActionStep(stepId, HTTP_CLIENT_ACTION_CLASS, HTTP_CLIENT_ACTION_METHOD, null, navigationMatchers);
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getHost() {
		return host;
	}

	public String getIdentityPort() {
		return identityPort;
	}

	public String getComputePort() {
		return computePort;
	}

	public String getServerName() {
		return serverName;
	}
}
