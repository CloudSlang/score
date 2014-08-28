package org.score.samples.openstack;

import com.hp.score.api.TriggeringProperties;
import org.apache.commons.lang3.StringUtils;
import org.score.samples.openstack.actions.ExecutionPlanBuilder;
import org.score.samples.openstack.actions.MatchType;
import org.score.samples.openstack.actions.NavigationMatcher;
import org.score.samples.openstack.actions.SimpleSendEmail;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.score.samples.openstack.actions.FinalStepActions.RESPONSE_KEY;
import static org.score.samples.openstack.actions.FinalStepActions.SUCCESS_KEY;
import static org.score.samples.openstack.OpenstackCommons.*;

/**
 * Date: 8/27/2014
 *
 * @author Bonczidai Levente
 */
@SuppressWarnings("unused")
public class OpenStackHealthCheck {
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
		CreateServer createServerObject = new CreateServer();
		triggeringProperties = createServerObject.createServersFlow();
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
		triggeringProperties = new ValidateServerExists().validateServerExists(false, createServerObject.getUsername(), createServerObject.getPassword(),
				createServerObject.getHost(), createServerObject.getIdentityPort(), createServerObject.getComputePort(), createServerObject.getServerName());
		builder.addSubflow(validateServerSplitId, validateServerJoinId, triggeringProperties, inputKeysFromParentContext, navigationMatchers);

		//delete server subflow
		inputKeysFromParentContext = new ArrayList<>();
		inputKeysFromParentContext.add(SERVER_NAME_KEY);
		navigationMatchers = new ArrayList<>();
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.EQUAL, RESPONSE_KEY, SUCCESS_KEY, successId));
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.DEFAULT, prepareSendEmailId));
		triggeringProperties = new DeleteServer().deleteServerFlow(createServerObject.getUsername(), createServerObject.getPassword(),
				createServerObject.getHost(), createServerObject.getIdentityPort(), createServerObject.getComputePort(), createServerObject.getServerName());
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

	public Map<String, Serializable> sendEmailInputs(){
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		Map<String, Serializable> context = new HashMap<>();

		String emailHost;
		String to;
		String emailPort;
		String from;
		emailHost = readInput(reader, "Email host");
		emailPort = readInput(reader, "Email port");
		from = readInput(reader, "Fail email sender");
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
		if(!StringUtils.isEmpty(from)) {
			context.put("from", from);
		}

		return context;
	}
}
