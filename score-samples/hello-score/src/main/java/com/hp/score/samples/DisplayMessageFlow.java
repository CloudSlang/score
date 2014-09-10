package com.hp.score.samples;

import com.hp.oo.sdk.content.annotations.Param;
import com.hp.score.api.ExecutionPlan;
import com.hp.score.api.TriggeringProperties;
import com.hp.score.samples.openstack.actions.ExecutionPlanBuilder;
import com.hp.score.samples.openstack.actions.InputBinding;
import com.hp.score.samples.openstack.actions.InputBindingFactory;
import com.hp.score.samples.openstack.actions.MatchType;
import com.hp.score.samples.openstack.actions.NavigationMatcher;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.hp.score.samples.openstack.OpenstackCommons.FINAL_STEP_ACTIONS_CLASS;
import static com.hp.score.samples.openstack.OpenstackCommons.SUCCESS_STEP_ACTION_METHOD;

/**
 * Date: 8/18/2014
 *
 * @author Bonczidai Levente
 */
@SuppressWarnings("unused")
public class DisplayMessageFlow {
	public static final String STATUS = "status";
	public static final String MESSAGE = "message";
	public static final String USER = "user";
	private List<InputBinding> inputBindings;

	public DisplayMessageFlow() {
		inputBindings = generateInitialInputBindings();
	}

	private List<InputBinding> generateInitialInputBindings() {
		List<InputBinding> bindings = new ArrayList<>();

		bindings.add(InputBindingFactory.createInputBinding("status", "status", true));
		bindings.add(InputBindingFactory.createInputBinding("message", "message", true));
		bindings.add(InputBindingFactory.createInputBinding("user", "user", true));

		return bindings;
	}

	public TriggeringProperties displayMessageFlow() {
		TriggeringProperties triggeringProperties = TriggeringProperties.create(createDisplayExecutionPlan());
		Map<String, Serializable> context = new HashMap<>();
		triggeringProperties.setContext(context);
		triggeringProperties.setStartStep(0L);
		return triggeringProperties;
	}

	public ExecutionPlan createDisplayExecutionPlan() {
		ExecutionPlanBuilder builder = new ExecutionPlanBuilder();

		List<NavigationMatcher<Serializable>> navigationMatchers = new ArrayList<>();
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.DEFAULT, 1L));
		builder.addOOActionStep(0L, "com.hp.score.samples.DisplayMessageFlow", "displayMessage", null, navigationMatchers);

		builder.addOOActionFinalStep(1L, FINAL_STEP_ACTIONS_CLASS, SUCCESS_STEP_ACTION_METHOD);

		return builder.createTriggeringProperties().getExecutionPlan();
	}

	public Map<String, String> displayMessage(@Param("message") String message,
                                              @Param("status") String status,
                                              @Param("user") String user) {
		System.out.println(status + " -> " + user + " : " + message);
		return new HashMap<>();
	}

	public List<InputBinding> getInputBindings() {
		return inputBindings;
	}
}
