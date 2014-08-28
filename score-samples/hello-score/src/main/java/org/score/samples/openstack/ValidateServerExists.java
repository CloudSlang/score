package org.score.samples.openstack;

import com.hp.score.api.TriggeringProperties;
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

import static org.score.samples.openstack.actions.FinalStepActions.RESPONSE_KEY;
import static org.score.samples.openstack.actions.FinalStepActions.SUCCESS_KEY;
import static org.score.samples.openstack.actions.StringOccurrenceCounter.RETURN_RESULT;
import static org.score.samples.openstack.OpenstackCommons.*;

/**
 * Date: 8/27/2014
 *
 * @author Bonczidai Levente
 */
@SuppressWarnings("unused")
public class ValidateServerExists {
	public TriggeringProperties validateServerExistsStandAlone() {
		TriggeringProperties triggeringProperties = validateServerExists(true, "", "", "", "", "", "");

		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		String serverName = readInput(reader, "Server name");
		Map<String, Serializable> executionContext = new HashMap<>();
		executionContext.putAll(triggeringProperties.getContext());
		executionContext.put(SERVER_NAME_KEY, serverName);
		triggeringProperties.setContext(executionContext);

		triggeringProperties.setStartStep(0L);
		return triggeringProperties;
	}

	public TriggeringProperties validateServerExists(
			boolean standAloneFlow,
			String username,
			String password,
			String host,
			String identityPort,
			String computePort,
			String serverName) {
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
		ListServers listServers = new ListServers();
		TriggeringProperties triggeringProperties;
		if (standAloneFlow) {
			triggeringProperties = listServers.listServersFlowStandAlone();
		} else {
			triggeringProperties = listServers.listServersFlow(username, password, host, identityPort, computePort, serverName);
		}
		builder.addSubflow(splitId, joinId, triggeringProperties, null, navigationMatchers);

		//prepare string occurrences
		navigationMatchers = new ArrayList<>();
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.DEFAULT, stringOccurencesId));
		builder.addOOActionStep(prepareStringOccurrencesId, CONTEXT_MERGER_CLASS, "prepareStringOccurrences", null, navigationMatchers);

		//string occurrence
		navigationMatchers = new ArrayList<>();
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.COMPARE_GREATER, RETURN_RESULT, "0", successId));
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
}
