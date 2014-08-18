package org.score.samples;

import com.hp.score.api.ExecutionPlan;
import org.score.samples.openstack.actions.ExecutionPlanBuilder;
import org.score.samples.openstack.actions.MatchType;
import org.score.samples.openstack.actions.NavigationMatcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Date: 7/28/2014
 *
 * @author Bonczidai Levente
 */
public class OpenstackApplications {
	@SuppressWarnings("unused")
	public Map<String, Serializable> prepareListServerExecutionContext() {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		String username;
		String password;
		String host;
		String port;
		host = readInput(reader, "Host");
		port = readInput(reader, "Port");
		username = readInput(reader, "Username");
		password = readInput(reader, "Password");

		Map<String, Serializable> executionContext = new HashMap<>();
		String url = "http://" + host + ":" + port + "/v2.0/tokens";
		String body = "{\"auth\": {\"tenantName\": \"demo\",\"passwordCredentials\": {\"username\": \"" + username +"\",\"password\": \"" + password + "\"}}}";
		executionContext.put("url", url);
		executionContext.put("method", "post");
		executionContext.put("body", body);
		executionContext.put("contentType", "application/json");

		return executionContext;
	}

	@SuppressWarnings("unused")
	public Map<String, Serializable> prepareCreateServerExecutionContext() {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		String username;
		String password;
		String host;
		String port;
		String serverName;
		host = readInput(reader, "Host");
		port = readInput(reader, "Port");
		username = readInput(reader, "Username");
		password = readInput(reader, "Password");
		serverName = readInput(reader, "Server name");

		Map<String, Serializable> executionContext = new HashMap<>();
		String url = "http://" + host + ":" + port + "/v2.0/tokens";
		String body = "{\"auth\": {\"tenantName\": \"demo\",\"passwordCredentials\": {\"username\": \"" + username +"\",\"password\": \"" + password + "\"}}}";
		executionContext.put("url", url);
		executionContext.put("method", "post");
		executionContext.put("body", body);
		executionContext.put("contentType", "application/json");
		executionContext.put("serverName", serverName);

		return executionContext;
	}

	@SuppressWarnings("unused")
	public ExecutionPlan prepareCreateServerExecutionPlan() {
		ExecutionPlanBuilder builder = new ExecutionPlanBuilder();
		Map<String, Serializable> executionContext = new HashMap<>();

		Long tokenStepId = 0L;
		Long contextMergerStepId = 1L;
		Long createServerStepId = 2L;
		Long successStepId = 3L;

		createGetTokenStep(builder, tokenStepId, contextMergerStepId, successStepId);

		createContextMergerStep(builder, contextMergerStepId, createServerStepId, successStepId);

		startServerStep(builder, createServerStepId, successStepId, successStepId);

		createSuccessStep(builder, successStepId);

		return builder.getExecutionPlan();
	}

	@SuppressWarnings("unused")
	public ExecutionPlan prepareListServerExecutionPlan() {
		ExecutionPlanBuilder builder = new ExecutionPlanBuilder();

		Long tokenStepId = 0L;
		Long mergerStepId = 1L;
		Long getServersStepId = 2L;
		Long displayStepId = 3L;
		Long successStepId = 4L;

		createGetTokenStep(builder, tokenStepId, mergerStepId, successStepId);

		createPrepareGetServersStep(builder, mergerStepId, getServersStepId);

		createGetServersStep(builder, getServersStepId, displayStepId);

		createDisplayStep(builder, displayStepId, successStepId);

		createSuccessStep(builder, successStepId);

		return builder.getExecutionPlan();
	}

	private void createGetTokenStep(
			ExecutionPlanBuilder builder,
			Long stepId,
			Long nextStepId,
			Long defaultStepId){
		List<NavigationMatcher<Serializable>> navigationMatchers = new ArrayList<>();

		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.EQUAL, "returnCode", "0", nextStepId));
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.DEFAULT, defaultStepId));

		builder.addOOActionStep(stepId, "org.score.content.httpclient.HttpClientAction", "execute", null, navigationMatchers);
	}

	private void createContextMergerStep(
			ExecutionPlanBuilder builder,
			Long stepId,
			Long nextStepId,
			Long defaultStepId) {
		List<NavigationMatcher<Serializable>> navigationMatchers = new ArrayList<>();

		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.EQUAL, "result", "0", nextStepId));
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.DEFAULT, defaultStepId));

		builder.addOOActionStep(stepId, "org.score.samples.openstack.actions.ContextMerger", "prepareCreateServer", null, navigationMatchers);
	}

	private void startServerStep(
			ExecutionPlanBuilder builder,
			Long stepId,
			Long nextStepId,
			Long defaultStepId){
		List<NavigationMatcher<Serializable>> navigationMatchers = new ArrayList<>();

		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.EQUAL, "returnCode", "0", nextStepId));
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.DEFAULT, defaultStepId));

		builder.addOOActionStep(stepId, "org.score.content.httpclient.HttpClientAction", "execute", null, navigationMatchers);
	}

	private void createPrepareGetServersStep(
			ExecutionPlanBuilder builder,
			Long stepId,
			Long defaultStepId) {
		//prepare context for get servers
		List<NavigationMatcher<Serializable>>  navigationMatchers = new ArrayList<>();
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.DEFAULT, defaultStepId));
		builder.addOOActionStep(stepId, "org.score.samples.openstack.actions.ContextMerger", "prepareGetServer", null, navigationMatchers);
	}

	private void createGetServersStep(ExecutionPlanBuilder builder, Long stepId, Long defaultStepId) {
		List<NavigationMatcher<Serializable>>  navigationMatchers = new ArrayList<>();
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.EQUAL, "statusCode", "200", defaultStepId));
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.EQUAL, "statusCode", "203", defaultStepId));
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.DEFAULT, defaultStepId));
		builder.addOOActionStep(stepId, "org.score.content.httpclient.HttpClientAction", "execute", null, navigationMatchers);
	}

	private void createSuccessStep(ExecutionPlanBuilder builder, Long successStepId) {
		//success step
		builder.addOOActionFinalStep(successStepId, "org.score.samples.openstack.actions.FinalStepActions", "successStepAction");
	}

	private void createDisplayStep(ExecutionPlanBuilder builder, Long stepId, Long defaultStepId) {
		List<NavigationMatcher<Serializable>> navigationMatchers;//display step
		navigationMatchers = new ArrayList<>();
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.DEFAULT, defaultStepId));
		builder.addOOActionStep(stepId, "org.score.samples.openstack.actions.ContextMerger", "getServerNames", null, navigationMatchers);
	}

	private String readInput(BufferedReader reader, String inputName) {
		System.out.print(inputName + ": ");
		return readLine(reader);
	}

	private String readLine(BufferedReader reader) {
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
