package org.score.samples;

import com.hp.score.api.ExecutionPlan;
import org.apache.commons.lang3.StringUtils;
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
		String identityPort;
		String computePort;
		host = readInput(reader, "Host");
		identityPort = readInput(reader, "Identity Port");
		computePort = readInput(reader, "Compute Port");
		username = readInput(reader, "Username");
		password = readInput(reader, "Password");

		if (StringUtils.isEmpty(host)){
			host = "16.59.58.200";
		}
		if (StringUtils.isEmpty(identityPort)){
			identityPort = "5000";
		}
		if (StringUtils.isEmpty(computePort)){
			computePort = "8774";
		}

		Map<String, Serializable> executionContext = new HashMap<>();
		String url = "http://" + host + ":" + identityPort + "/v2.0/tokens";
		String body = "{\"auth\": {\"tenantName\": \"demo\",\"passwordCredentials\": {\"username\": \"" + username +"\",\"password\": \"" + password + "\"}}}";
		executionContext.put("url", url);
		executionContext.put("method", "post");
		executionContext.put("body", body);
		executionContext.put("contentType", "application/json");
		executionContext.put("computePort", computePort);
		executionContext.put("host", host);

		return executionContext;
	}

	@SuppressWarnings("unused")
	public Map<String, Serializable> prepareCreateServerExecutionContext() {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		String username;
		String password;
		String host;
		String identityPort;
		String computePort;
		String serverName;
		String imageRef;

		host = readInput(reader, "Host");
		identityPort = readInput(reader, "Identity Port");
		computePort = readInput(reader, "Compute Port");
		imageRef = readInput(reader, "ImageRef");
		username = readInput(reader, "Username");
		password = readInput(reader, "Password");
		serverName = readInput(reader, "Server name");

		if (StringUtils.isEmpty(host)){
			host = "16.59.58.200";
		}
		if (StringUtils.isEmpty(identityPort)){
			identityPort = "5000";
		}
		if (StringUtils.isEmpty(computePort)){
			computePort = "8774";
		}
		if (StringUtils.isEmpty(imageRef)){
			imageRef = "56ff0279-f1fb-46e5-93dc-fe7093af0b1a";
		}

		Map<String, Serializable> executionContext = new HashMap<>();
		String url = "http://" + host + ":" + identityPort + "/v2.0/tokens";
		String body = "{\"auth\": {\"tenantName\": \"demo\",\"passwordCredentials\": {\"username\": \"" + username +"\",\"password\": \"" + password + "\"}}}";
		executionContext.put("url", url);
		executionContext.put("method", "post");
		executionContext.put("body", body);
		executionContext.put("contentType", "application/json");
		executionContext.put("serverName", serverName);
		executionContext.put("computePort", computePort);
		executionContext.put("imageRef", imageRef);
		executionContext.put("host", host);

		return executionContext;
	}
	@SuppressWarnings("unused")
	public Map<String, Serializable> prepareSendEmailExecutionContext(){
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		String from;
		String to;
		String host;
		String port;

		String subject;
		String text;

		host = readInput(reader, "Host");
		port = readInput(reader, "Port");
		from = readInput(reader, "From");
		to = readInput(reader, "To");
		subject = readInput(reader, "Subject");
		text = readInput(reader, "Text");


		if (StringUtils.isEmpty(host)){
			host = "smtp-americas.hp.com"; //todo take default values out
		}
		if (StringUtils.isEmpty(port)){
			port = "25";
		}

		Map<String, Serializable> executionContext = new HashMap<>();
		executionContext.put("host", host);
		executionContext.put("port", port);
		executionContext.put("from", from);
		executionContext.put("to", to);
		executionContext.put("subject", subject);
		executionContext.put("body", text);


		return executionContext;
	}
	@SuppressWarnings("unused")
	public ExecutionPlan prepareSendEmailExecutionPlan(){
		ExecutionPlanBuilder builder = new ExecutionPlanBuilder();


		Long sendEmailStepId = 0L;
		Long successStepId = 1L;
		Long failureStepId = 2L;

		createSendEmailStep(builder, sendEmailStepId, successStepId, failureStepId);

		createSuccessStep(builder, successStepId);

		createFailureStep(builder, failureStepId);

		return builder.getExecutionPlan();

	}

	private void createSendEmailStep(
			ExecutionPlanBuilder builder,
			Long stepId,
			Long nextStepId,
			Long defaultStepId) {
		List<NavigationMatcher<Serializable>> navigationMatchers = new ArrayList<>();

		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.EQUAL, "returnCode", "0", nextStepId));
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.DEFAULT, defaultStepId));

		builder.addOOActionStep(stepId, "org.score.samples.openstack.actions.SimpleSendEmail", "execute", null, navigationMatchers);

	}

	@SuppressWarnings("unused")
	public ExecutionPlan prepareCreateServerExecutionPlan() {
		ExecutionPlanBuilder builder = new ExecutionPlanBuilder();
		Map<String, Serializable> executionContext = new HashMap<>();

		Long tokenStepId = 0L;
		Long contextMergerStepId = 1L;
		Long createServerStepId = 2L;
		Long successStepId = 3L;
		Long failureStepId = 4L;

		createGetTokenStep(builder, tokenStepId, contextMergerStepId, failureStepId);

		createContextMergerStep(builder, contextMergerStepId, createServerStepId);

		startServerStep(builder, createServerStepId, successStepId, failureStepId);

		createSuccessStep(builder, successStepId);

		createFailureStep(builder, failureStepId);

		return builder.getExecutionPlan();
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

		host = readInput(reader, "Host");
		identityPort = readInput(reader, "Identity Port");
		computePort = readInput(reader, "Compute Port");
		username = readInput(reader, "Username");
		password = readInput(reader, "Password");
		serverName = readInput(reader, "Server name");

		if (StringUtils.isEmpty(host)){
			host = "16.59.58.200";
		}
		if (StringUtils.isEmpty(identityPort)){
			identityPort = "5000";
		}
		if (StringUtils.isEmpty(computePort)){
			computePort = "8774";
		}


		Map<String, Serializable> executionContext = new HashMap<>();
		String url = "http://" + host + ":" + identityPort + "/v2.0/tokens";
		String body = "{\"auth\": {\"tenantName\": \"demo\",\"passwordCredentials\": {\"username\": \"" + username +"\",\"password\": \"" + password + "\"}}}";
		executionContext.put("url", url);
		executionContext.put("method", "post");
		executionContext.put("body", body);
		executionContext.put("contentType", "application/json");
		executionContext.put("serverName", serverName);
		executionContext.put("computePort", computePort);

		executionContext.put("host", host);

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




		return builder.getExecutionPlan();

	}

	private void createDeleteServerStep(ExecutionPlanBuilder builder, Long stepId, Long nextStepId, Long defaultStepId) {
		List<NavigationMatcher<Serializable>> navigationMatchers = new ArrayList<>();

		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.EQUAL, "returnCode", "0", nextStepId));
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.DEFAULT, defaultStepId));

		builder.addOOActionStep(stepId, "org.score.content.httpclient.HttpClientAction", "execute", null, navigationMatchers);
	}

	private void createCreatePrepareDeleteServerStep(ExecutionPlanBuilder builder, Long stepId, Long nextStepId) {
		builder.addStep(stepId, "org.score.samples.openstack.actions.ContextMerger", "prepareDeleteServer", nextStepId);
	}


	@SuppressWarnings("unused")
	public ExecutionPlan prepareListServerExecutionPlan() {
		ExecutionPlanBuilder builder = new ExecutionPlanBuilder();

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

		return builder.getExecutionPlan();
	}

	private void createGetServerIdStep(ExecutionPlanBuilder builder, Long stepId, Long nextStepId) {
		builder.addStep(stepId, "org.score.samples.openstack.actions.ContextMerger", "getServerId", nextStepId);
	}

	private void createGetTokenStep(
			ExecutionPlanBuilder builder,
			Long stepId,
			Long successStepId,
			Long failureStepId){
		List<NavigationMatcher<Serializable>> navigationMatchers = new ArrayList<>();

		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.EQUAL, "statusCode", "200", successStepId));
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.DEFAULT, failureStepId));

		builder.addOOActionStep(stepId, "org.score.content.httpclient.HttpClientAction", "execute", null, navigationMatchers);
	}

	private void createContextMergerStep(
			ExecutionPlanBuilder builder,
			Long stepId,
			Long nextStepId) {

		builder.addStep(stepId, "org.score.samples.openstack.actions.ContextMerger", "prepareCreateServer", nextStepId);
	}

	private void startServerStep(
			ExecutionPlanBuilder builder,
			Long stepId,
			Long successStepId,
			Long failureStepId){
		List<NavigationMatcher<Serializable>> navigationMatchers = new ArrayList<>();

		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.EQUAL, "statusCode", "202", successStepId));
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.DEFAULT, failureStepId));

		builder.addOOActionStep(stepId, "org.score.content.httpclient.HttpClientAction", "execute", null, navigationMatchers);
	}

	private void createPrepareGetServersStep(
			ExecutionPlanBuilder builder,
			Long stepId,
			Long nextStepId) {
		builder.addStep(stepId, "org.score.samples.openstack.actions.ContextMerger", "prepareGetServer", nextStepId);
	}

	private void createGetServersStep(ExecutionPlanBuilder builder, Long stepId, Long successStepId, Long failureStepId) {
		List<NavigationMatcher<Serializable>>  navigationMatchers = new ArrayList<>();
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.EQUAL, "statusCode", "200", successStepId));
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.DEFAULT, failureStepId));
		builder.addOOActionStep(stepId, "org.score.content.httpclient.HttpClientAction", "execute", null, navigationMatchers);
	}

	private void createSuccessStep(ExecutionPlanBuilder builder, Long successStepId) {
		//success step
		builder.addOOActionFinalStep(successStepId, "org.score.samples.openstack.actions.FinalStepActions", "successStepAction");
	}

	private void createFailureStep(ExecutionPlanBuilder builder, Long failureStepId) {
		//failure step
		builder.addOOActionFinalStep(failureStepId, "org.score.samples.openstack.actions.FinalStepActions", "failureStepAction");
	}

	private void createDisplayStep(ExecutionPlanBuilder builder, Long stepId, Long nextStepId) {
		builder.addStep(stepId, "org.score.samples.openstack.actions.ContextMerger", "getServerNames", nextStepId);
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
