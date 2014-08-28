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
public class DeleteServer {
	public TriggeringProperties deleteServerFlowStandAlone() {
		TriggeringProperties triggeringProperties = TriggeringProperties.create(prepareDeleteServerExecutionPlan());
		triggeringProperties.setContext(prepareDeleteServerExecutionContext());
		triggeringProperties.setStartStep(0L);
		return triggeringProperties;
	}

	public TriggeringProperties deleteServerFlow(
			String username,
			String password,
			String host,
			String identityPort,
			String computePort,
			String serverName) {
		TriggeringProperties triggeringProperties = TriggeringProperties.create(prepareDeleteServerExecutionPlan());
		Map<String, Serializable> context = prepareDeleteServerContext(username, password, host, identityPort, computePort, serverName);
		context.put(FLOW_DESCRIPTION, "Delete Server");
		triggeringProperties.setContext(context);
		triggeringProperties.setStartStep(0L);
		return triggeringProperties;
	}

	public Map<String, Serializable> prepareDeleteServerContext(
			String username,
			String password,
			String host,
			String identityPort,
			String computePort,
			String serverName){
		Map<String, Serializable> executionContext = new HashMap<>();
		String url = "http://" + host + ":" + identityPort + "/v2.0/tokens";
		String body = "{\"auth\": {\"tenantName\": \"demo\",\"passwordCredentials\": {\"username\": \"" + username +"\",\"password\": \"" + password + "\"}}}";
		executionContext.put(URL_KEY, url);
		executionContext.put(METHOD_KEY, "post");
		executionContext.put(BODY_KEY, body);
		executionContext.put(CONTENT_TYPE_KEY, "application/json");
		executionContext.put(SERVER_NAME_KEY, serverName);
		executionContext.put(COMPUTE_PORT_KEY, computePort);

		executionContext.put(HOST_KEY, host);

		return executionContext;
	}

	@SuppressWarnings("unused")
	public Map<String, Serializable> prepareDeleteServerExecutionContext(){
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

		host = readInput(reader, "Host");
		identityPort = readPredefinedInput(reader, "Identity Port", defualtIdentityPort);
		computePort = readPredefinedInput(reader, "Compute Port", defaultComputePort);
		username = readInput(reader, "Username");
		password = readInput(reader, "Password");
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


		Map<String, Serializable> executionContext = new HashMap<>();
		String url = "http://" + host + ":" + identityPort + "/v2.0/tokens";
		String body = "{\"auth\": {\"tenantName\": \"demo\",\"passwordCredentials\": {\"username\": \"" + username +"\",\"password\": \"" + password + "\"}}}";
		executionContext.put(URL_KEY, url);
		executionContext.put(METHOD_KEY, "post");
		executionContext.put(BODY_KEY, body);
		executionContext.put(CONTENT_TYPE_KEY, "application/json");
		executionContext.put(SERVER_NAME_KEY, serverName);
		executionContext.put(COMPUTE_PORT_KEY, computePort);

		executionContext.put(HOST_KEY, host);

		return executionContext;
	}

	@SuppressWarnings("unused")
	public ExecutionPlan prepareDeleteServerExecutionPlan(){
		ExecutionPlanBuilder builder = new ExecutionPlanBuilder();

		Long tokenStepId = 0L;
		Long mergerStepId = 1L;
		Long getServersStepId = 2L;
		Long getServerIdStepId = 3L;
		Long secondMergerStepId = 4L;
		Long deleteServerStepId = 5L;
		Long successStepId = 6L;
		Long failureStepId = 7L;

		createGetTokenStep(builder, tokenStepId, mergerStepId, failureStepId);

		createPrepareGetServersStep(builder, mergerStepId, getServersStepId);

		createGetServersStep(builder, getServersStepId, getServerIdStepId, failureStepId);

		createGetServerIdStep(builder, getServerIdStepId, secondMergerStepId);

		createCreatePrepareDeleteServerStep(builder, secondMergerStepId, deleteServerStepId);

		createDeleteServerStep(builder, deleteServerStepId, successStepId, failureStepId);

		createSuccessStep(builder, successStepId);

		createFailureStep(builder, failureStepId);

		return builder.createTriggeringProperties().getExecutionPlan();
	}

	private void createDeleteServerStep(ExecutionPlanBuilder builder, Long stepId, Long nextStepId, Long defaultStepId) {
		List<NavigationMatcher<Serializable>> navigationMatchers = new ArrayList<>();

		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.EQUAL, "returnCode", "0", nextStepId));
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.DEFAULT, defaultStepId));

		builder.addOOActionStep(stepId, HTTP_CLIENT_ACTION_CLASS, HTTP_CLIENT_ACTION_METHOD, null, navigationMatchers);
	}

	private void createCreatePrepareDeleteServerStep(ExecutionPlanBuilder builder, Long stepId, Long nextStepId) {
		builder.addStep(stepId, CONTEXT_MERGER_CLASS, "prepareDeleteServer", nextStepId);
	}

	private void createGetServerIdStep(ExecutionPlanBuilder builder, Long stepId, Long nextStepId) {
		builder.addStep(stepId, CONTEXT_MERGER_CLASS, "getServerId", nextStepId);
	}
}
