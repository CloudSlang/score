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
public class ListServers {
	public TriggeringProperties listServersFlow(
			String username,
			String password,
			String host,
			String identityPort,
			String computePort,
			String serverName) {
		TriggeringProperties triggeringProperties = TriggeringProperties.create(prepareListServerExecutionPlan());
		triggeringProperties.setContext(prepareListServerContext(username, password, host, identityPort, computePort, serverName));

		triggeringProperties.setStartStep(0L);
		return triggeringProperties;
	}

	public TriggeringProperties listServersFlowStandAlone() {
		TriggeringProperties triggeringProperties = TriggeringProperties.create(prepareListServerExecutionPlan());
		triggeringProperties.setContext(prepareListServerExecutionContext());
		triggeringProperties.setStartStep(0L);
		return triggeringProperties;
	}

	public ExecutionPlan prepareListServerExecutionPlan() {
		ExecutionPlanBuilder builder = new ExecutionPlanBuilder("list servers");

		Long tokenStepId = 0L;
		Long mergerStepId = 1L;
		Long getServersStepId = 2L;
		Long displayStepId = 3L;
		Long successStepId = 4L;
		Long failureStepId = 5L;

		createGetTokenStep(builder, tokenStepId, mergerStepId, failureStepId);

		createPrepareGetServersStep(builder, mergerStepId, getServersStepId);

		createGetServersStep(builder, getServersStepId, displayStepId, failureStepId);

		createDisplayStep(builder, displayStepId, successStepId);

		createSuccessStep(builder, successStepId);

		createFailureStep(builder, failureStepId);

		return builder.createTriggeringProperties().getExecutionPlan();
	}

	public Map<String, Serializable> prepareListServerContext(
			String username,
			String password,
			String host,
			String identityPort,
			String computePort,
			String serverName) {
		Map<String, Serializable> executionContext = new HashMap<>();
		String url = "http://" + host + ":" + identityPort + "/v2.0/tokens";
		String body = "{\"auth\": {\"tenantName\": \"demo\",\"passwordCredentials\": {\"username\": \"" + username +"\",\"password\": \"" + password + "\"}}}";
		executionContext.put(URL_KEY, url);
		executionContext.put(METHOD_KEY, "post");
		executionContext.put(BODY_KEY, body);
		executionContext.put(CONTENT_TYPE_KEY, "application/json");
		executionContext.put(COMPUTE_PORT_KEY, computePort);
		executionContext.put(HOST_KEY, host);

		return executionContext;
	}

	public Map<String, Serializable> prepareListServerExecutionContext() {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		String username;
		String password;
		String host;
		String identityPort;
		String computePort;


		String defualtIdentityPort =  "5000";
		String defaultComputePort = "8774";

		host = readInput(reader, OPENSTACK_HOST_MESSAGE);
		identityPort = readPredefinedInput(reader, IDENTITY_PORT_MESSAGE, defualtIdentityPort);
		computePort = readPredefinedInput(reader, COMPUTE_PORT_MESSAGE, defaultComputePort);
		username = readInput(reader, OPENSTACK_USERNAME_MESSAGE);
		password = readInput(reader, OPENSTACK_PASSWORD_MESSAGE);

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
		executionContext.put(COMPUTE_PORT_KEY, computePort);
		executionContext.put(HOST_KEY, host);

		return executionContext;
	}

	private void createDisplayStep(ExecutionPlanBuilder builder, Long stepId, Long nextStepId) {
		List<NavigationMatcher<Serializable>>  navigationMatchers = new ArrayList<>();
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.DEFAULT, nextStepId));
		builder.addOOActionStep(stepId, CONTEXT_MERGER_CLASS, "getServerNames", null, navigationMatchers);
	}
}
