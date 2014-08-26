package org.score.samples;

import com.hp.score.api.ExecutionPlan;
import com.hp.score.api.TriggeringProperties;
import org.apache.commons.lang3.StringUtils;
import org.score.samples.openstack.actions.ExecutionPlanBuilder;
import org.score.samples.openstack.actions.MatchType;
import org.score.samples.openstack.actions.NavigationMatcher;
import org.score.samples.openstack.actions.SimpleSendEmail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.score.samples.openstack.actions.FinalStepActions.RESPONSE_KEY;
import static org.score.samples.openstack.actions.FinalStepActions.SUCCESS_KEY;
import static org.score.samples.openstack.actions.StringOccurrenceCounter.RETURN_RESULT;

/**
 * Date: 7/28/2014
 *
 * @author Bonczidai Levente
 */
@SuppressWarnings("unused")
public class OpenstackApplications {
	public static final String FINAL_STEP_ACTIONS_CLASS = "org.score.samples.openstack.actions.FinalStepActions";
	public static final String SUCCESS_STEP_ACTION = "successStepAction";
	public static final String FAILURE_STEP_ACTION = "failureStepAction";
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

	private String username;
	private String password;
	private String host;
	private String identityPort;
	private String computePort;
	private String serverName;

	public TriggeringProperties openStackHealthCheck() {
		ExecutionPlanBuilder builder = new ExecutionPlanBuilder("health check");

		Long createServerSplitId = 0L;
		Long createServerJoinId = 1L;
		Long validateServerSplitId = 2L;
		Long validateServerJoinId = 3L;
		Long deleteServerSplitId = 4L;
		Long deleteServerJoinId = 5L;
		Long successId = 6L;
		Long sendEmailId = 7L;
		Long failureId = 8L;
		Long prepareSendEmailId = 9L;

		//create server subflow
		List<NavigationMatcher<Serializable>> navigationMatchers = new ArrayList<>();
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.EQUAL, RESPONSE_KEY, SUCCESS_KEY, validateServerSplitId));
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.DEFAULT, prepareSendEmailId));
		TriggeringProperties triggeringProperties;
		triggeringProperties = createServersFlow();
		builder.addSubflow(createServerSplitId, createServerJoinId, triggeringProperties, null, navigationMatchers);

		//set initial parent context
		Map<String, Serializable> sendEmailContext = sendEmailInputs();
		sendEmailContext.putAll(triggeringProperties.getContext());
		builder.setInitialExecutionContext(sendEmailContext);

		//validate server subflow
		List<String> inputKeysFromParentContext;
		inputKeysFromParentContext = new ArrayList<>();
		inputKeysFromParentContext.add(SERVER_NAME_KEY);
		navigationMatchers = new ArrayList<>();
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.EQUAL, RESPONSE_KEY, SUCCESS_KEY, deleteServerSplitId));
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.DEFAULT, prepareSendEmailId));
		triggeringProperties = validateServerExists(false);
		builder.addSubflow(validateServerSplitId, validateServerJoinId, triggeringProperties, inputKeysFromParentContext, navigationMatchers);

		//delete server subflow
		inputKeysFromParentContext = new ArrayList<>();
		inputKeysFromParentContext.add(SERVER_NAME_KEY);
		navigationMatchers = new ArrayList<>();
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.EQUAL, RESPONSE_KEY, SUCCESS_KEY, successId));
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.DEFAULT, prepareSendEmailId));
		triggeringProperties = deleteServerFlow();
		builder.addSubflow(deleteServerSplitId, deleteServerJoinId, triggeringProperties, inputKeysFromParentContext, navigationMatchers);

		//prepare send email
		builder.addStep(prepareSendEmailId, CONTEXT_MERGER_CLASS, "prepareSendEmail", sendEmailId);

		//send email step
		navigationMatchers = new ArrayList<>();
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.EQUAL, SimpleSendEmail.RETURN_CODE, SimpleSendEmail.SUCCESS, failureId));
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.DEFAULT, failureId));
		builder.addOOActionStep(sendEmailId, SEND_EMAIL_CLASS, SEND_EMAIL_METHOD, null, navigationMatchers);

		//success step
		builder.addOOActionFinalStep(successId, FINAL_STEP_ACTIONS_CLASS, SUCCESS_STEP_ACTION);

		//failure step
		builder.addOOActionFinalStep(failureId, FINAL_STEP_ACTIONS_CLASS, FAILURE_STEP_ACTION);

		return builder.createTriggeringProperties();
	}

	public TriggeringProperties validateServerExists(boolean standAloneFlow) {
		ExecutionPlanBuilder builder = new ExecutionPlanBuilder("validate");
		Long splitId = 0L;
		Long joinId = 1L;
		Long successId = 2L;
		Long failureId = 3L;
		Long prepareStringOccurrencesId = 4L;
		Long stringOccurencesId = 5L;
		Long resultFormatterId = 6L;

		//get servers
		List<NavigationMatcher<Serializable>> navigationMatchers = new ArrayList<>();
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.EQUAL, RESPONSE_KEY, SUCCESS_KEY, prepareStringOccurrencesId));
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.DEFAULT, failureId));
		TriggeringProperties triggeringProperties = listServersFlow(standAloneFlow);
		builder.addSubflow(splitId, joinId, triggeringProperties, navigationMatchers);

		//prepare string occurrences
		navigationMatchers = new ArrayList<>();
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.DEFAULT, stringOccurencesId));
		builder.addOOActionStep(prepareStringOccurrencesId, CONTEXT_MERGER_CLASS, "prepareStringOccurrences", null, navigationMatchers);

		//string occurrence
		navigationMatchers = new ArrayList<>();
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.EQUAL, RETURN_RESULT, "1", successId));
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.DEFAULT, resultFormatterId));
		builder.addOOActionStep(stringOccurencesId,
				"org.score.samples.openstack.actions.StringOccurrenceCounter",
				"execute",
				null,
				navigationMatchers);

		//result formatter step
		builder.addStep(resultFormatterId, CONTEXT_MERGER_CLASS, "validateServerResult", failureId);

		//success step
		builder.addOOActionFinalStep(successId, FINAL_STEP_ACTIONS_CLASS, SUCCESS_STEP_ACTION);

		//failure step
		builder.addOOActionFinalStep(failureId, FINAL_STEP_ACTIONS_CLASS, FAILURE_STEP_ACTION);

		triggeringProperties = builder.createTriggeringProperties();
		@SuppressWarnings("unchecked")
		Map<String, Serializable> context = (Map<String, Serializable>) triggeringProperties.getContext();
		context.put(FLOW_DESCRIPTION, "Validate servers");
		triggeringProperties.setContext(context);
		return triggeringProperties;
	}

	public TriggeringProperties validateServerExistsStandAlone() {
		TriggeringProperties triggeringProperties = validateServerExists(true);

		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		String serverName = readInput(reader, "Server name");
		Map<String, Serializable> executionContext = new HashMap<>();
		executionContext.putAll(triggeringProperties.getContext());
		executionContext.put(SERVER_NAME_KEY, serverName);
		triggeringProperties.setContext(executionContext);

		triggeringProperties.setStartStep(0L);
		return triggeringProperties;
	}

	public TriggeringProperties listServersFlow(boolean standAloneFlow) {
		TriggeringProperties triggeringProperties = TriggeringProperties.create(prepareListServerExecutionPlan());
		if (standAloneFlow) {
			triggeringProperties.setContext(prepareListServerExecutionContext());
		} else {
			triggeringProperties.setContext(prepareListServerContext());
		}

		triggeringProperties.setStartStep(0L);
		return triggeringProperties;
	}

	public TriggeringProperties listServersFlowStandAlone() {
		return listServersFlow(true);
	}

	public TriggeringProperties createServersFlow() {
		TriggeringProperties triggeringProperties = TriggeringProperties.create(prepareCreateServerExecutionPlan());
		Map<String, Serializable> context = prepareCreateServerExecutionContext();
		context.put(FLOW_DESCRIPTION, "Create Server");
		triggeringProperties.setContext(context);
		triggeringProperties.setStartStep(0L);
		return triggeringProperties;
	}

	public Map<String, Serializable> prepareListServerContext() {
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

	public Map<String, Serializable> prepareListServerExecutionContext() {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		String username;
		String password;
		String host;
		String identityPort;
		String computePort;

		String defaultHost = "16.59.58.200";//todo remove near hardcoded strings
		String defualtIdentityPort =  "5000";
		String defaultComputePort = "8774";

		host = readPredefinedInput(reader, OPENSTACK_HOST_MESSAGE, defaultHost);
		identityPort = readPredefinedInput(reader, IDENTITY_PORT_MESSAGE, defualtIdentityPort);
		computePort = readPredefinedInput(reader, COMPUTE_PORT_MESSAGE, defaultComputePort);
		username = readInput(reader, OPENSTACK_USERNAME_MESSAGE);
		password = readInput(reader, OPENSTACK_PASSWORD_MESSAGE);

		if (StringUtils.isEmpty(host)){
			host = defaultHost;
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
		executionContext.put("url", url);
		executionContext.put("method", "post");
		executionContext.put("body", body);
		executionContext.put("contentType", "application/json");
		executionContext.put("computePort", computePort);
		executionContext.put("host", host);

		return executionContext;
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

		String defaultHost = "16.59.58.200"; //todo remove near hardcoded strings
		String defualtIdentityPort =  "5000";
		String defaultComputePort = "8774";
		String defaultImageRef = "56ff0279-f1fb-46e5-93dc-fe7093af0b1a";

		host = readPredefinedInput(reader, OPENSTACK_HOST_MESSAGE, defaultHost);
		identityPort = readPredefinedInput(reader, IDENTITY_PORT_MESSAGE, defualtIdentityPort);
		computePort = readPredefinedInput(reader, COMPUTE_PORT_MESSAGE, defaultComputePort);
		imageRef = readPredefinedInput(reader, "ImageRef", defaultImageRef);
		username = readInput(reader, OPENSTACK_USERNAME_MESSAGE);
		password = readInput(reader, OPENSTACK_PASSWORD_MESSAGE);
		serverName = readInput(reader, "Server name");

		if (StringUtils.isEmpty(host)){
			host = defaultHost;
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
		executionContext.put("url", url);
		executionContext.put("method", "post");
		executionContext.put("body", body);
		executionContext.put("contentType", "application/json");
		executionContext.put(SERVER_NAME_KEY, serverName);
		executionContext.put("computePort", computePort);
		executionContext.put("imageRef", imageRef);
		executionContext.put("host", host);

		return executionContext;
	}

	public Map<String, Serializable> sendEmailInputs(){
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		Map<String, Serializable> context = new HashMap<>();

		String emailHost;
		String to;
		String emailPort;
		emailHost = readPredefinedInput(reader, "Email host", "smtp-americas.hp.com"); //todo remove near hardcoded strings
		emailPort = readPredefinedInput(reader, "Email port", "25");
		to = readInput(reader, "Fail email recipient");

		if(!StringUtils.isEmpty(emailHost)) {
			context.put("emailHost", emailHost);
		}
		if(!StringUtils.isEmpty(emailPort)){
			context.put("emailPort", emailPort);
		}
		if(!StringUtils.isEmpty(to)) {
			context.put("to", to);
		}

		return context;
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
			host = "smtp-americas.hp.com"; //todo remove near hardcoded strings
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

		return builder.createTriggeringProperties().getExecutionPlan();

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

		return builder.createTriggeringProperties().getExecutionPlan();
	}

	public TriggeringProperties deleteServerFlowStandAlone() {
		TriggeringProperties triggeringProperties = TriggeringProperties.create(prepareDeleteServerExecutionPlan());
		triggeringProperties.setContext(prepareDeleteServerExecutionContext());
		triggeringProperties.setStartStep(0L);
		return triggeringProperties;
	}

	public TriggeringProperties deleteServerFlow() {
		TriggeringProperties triggeringProperties = TriggeringProperties.create(prepareDeleteServerExecutionPlan());
		Map<String, Serializable> context = prepareDeleteServerContext();
		context.put(FLOW_DESCRIPTION, "Delete Server");
		triggeringProperties.setContext(context);
		triggeringProperties.setStartStep(0L);
		return triggeringProperties;
	}

	public Map<String, Serializable> prepareDeleteServerContext(){
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
	public Map<String, Serializable> prepareDeleteServerExecutionContext(){
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		String username;
		String password;
		String host;
		String identityPort;
		String computePort;
		String serverName;
		String imageRef;

		String defaultHost = "16.59.58.200"; //todo remove near hardcoded strings
		String defualtIdentityPort =  "5000";
		String defaultComputePort = "8774";

		host = readPredefinedInput(reader, "Host", defaultHost);
		identityPort = readPredefinedInput(reader, "Identity Port", defualtIdentityPort);
		computePort = readPredefinedInput(reader, "Compute Port", defaultComputePort);
		username = readInput(reader, "Username");
		password = readInput(reader, "Password");
		serverName = readInput(reader, "Server name");

		if (StringUtils.isEmpty(host)){
			host = defaultHost; //todo remove near hardcoded strings
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

		return builder.createTriggeringProperties().getExecutionPlan();
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
		List<NavigationMatcher<Serializable>>  navigationMatchers = new ArrayList<>();
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.DEFAULT, nextStepId));
		builder.addOOActionStep(stepId, "org.score.samples.openstack.actions.ContextMerger", "getServerNames", null, navigationMatchers);
	}

	private String readInput(BufferedReader reader, String inputName) {
		System.out.print(inputName + ": ");
		return readLine(reader);
	}

	private String readPredefinedInput(BufferedReader reader, String inputName, String defaultValue) {
		System.out.print(inputName + " (default value = " + defaultValue +" --hit Enter): ");
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
