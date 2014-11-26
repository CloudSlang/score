/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package org.eclipse.score.samples;

import com.hp.oo.sdk.content.annotations.Param;
import org.eclipse.score.api.ExecutionPlan;
import org.eclipse.score.api.TriggeringProperties;
import org.eclipse.score.samples.openstack.actions.ExecutionPlanBuilder;
import org.eclipse.score.samples.openstack.actions.InputBinding;
import org.eclipse.score.samples.openstack.actions.InputBindingFactory;
import org.eclipse.score.samples.openstack.actions.MatchType;
import org.eclipse.score.samples.openstack.actions.NavigationMatcher;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.eclipse.score.samples.openstack.OpenstackCommons.FINAL_STEP_ACTIONS_CLASS;
import static org.eclipse.score.samples.openstack.OpenstackCommons.SUCCESS_STEP_ACTION_METHOD;

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
		builder.addOOActionStep(0L, "org.eclipse.score.samples.DisplayMessageFlow", "displayMessage", null, navigationMatchers);

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
