package org.score.samples;

import com.hp.score.api.ExecutionPlan;
import org.score.samples.openstack.actions.ExecutionPlanBuilder;
import org.score.samples.openstack.actions.InputBinding;
import org.score.samples.openstack.actions.MatchType;
import org.score.samples.openstack.actions.NavigationMatcher;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.score.samples.utility.ReadInputUtility.readStepInput;

/**
 * Date: 8/18/2014
 *
 * @author Bonczidai Levente
 */
@SuppressWarnings("unused")
public class DisplayMessageExecutionPlan {
	public static final String STATUS = "status";
	public static final String MESSAGE = "message";
	public static final String USER = "user";

	public ExecutionPlan createDisplayExecutionPlan() {
		ExecutionPlanBuilder builder = new ExecutionPlanBuilder();

		List<InputBinding> inputBindings = new ArrayList<>();
		inputBindings.add(new InputBinding("message", true));
		List<NavigationMatcher<Serializable>> navigationMatchers = new ArrayList<>();
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.DEFAULT, 1L));
		builder.addOOActionStep(0L, "org.score.samples.DisplayMessageExecutionPlan", "displayMessage", inputBindings, navigationMatchers);

		builder.addOOActionFinalStep(1L, "org.score.samples.openstack.actions.FinalStepActions", "successStepAction");

		return builder.getExecutionPlan();
	}

	public Map<String, Serializable> createDisplayExecutionContext() {
		Map<String, Serializable> executionContext = new HashMap<>();
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		executionContext.put(STATUS, readStepInput(reader, STATUS));
		executionContext.put(MESSAGE, readStepInput(reader, MESSAGE));
		executionContext.put(USER, readStepInput(reader, USER));
		return executionContext;
	}

	public Map<String, String> displayMessage(String message, String status, String user) {
		System.out.print(status + " -> ");
		System.out.println(user + " : " + message);
		return new HashMap<>();
	}
}
