package org.score.samples.openstack;

import com.hp.score.api.TriggeringProperties;
import org.score.samples.openstack.actions.ExecutionPlanBuilder;
import org.score.samples.openstack.actions.InputBinding;
import org.score.samples.openstack.actions.MatchType;
import org.score.samples.openstack.actions.NavigationMatcher;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.score.samples.openstack.OpenstackCommons.*;
import static org.score.samples.openstack.actions.InputBinding.*;

/**
 * Date: 8/29/2014
 *
 * @author Bonczidai Levente
 */
public class ListServersFlow {
	private List<InputBinding> inputBindings;

	public ListServersFlow() {
		inputBindings = generateInitialInputBindings();
	}

	private List<InputBinding> generateInitialInputBindings() {
		List<InputBinding> bindings = new ArrayList<>();

		bindings.add(createInputBinding(OPENSTACK_HOST_MESSAGE, HOST_KEY, true));
		bindings.add(createInputBindingWithDefaultValue(IDENTITY_PORT_MESSAGE, IDENTITY_PORT_KEY, true, DEFUALT_IDENTITY_PORT));
		bindings.add(createInputBindingWithDefaultValue(COMPUTE_PORT_MESSAGE, COMPUTE_PORT_KEY, true, DEFAULT_COMPUTE_PORT));
		bindings.add(createInputBinding(OPENSTACK_USERNAME_MESSAGE, USERNAME_KEY, true));
		bindings.add(createInputBinding(OPENSTACK_PASSWORD_MESSAGE, PASSWORD_KEY, true));

		return bindings;
	}

	public TriggeringProperties listServersFlow() {
		ExecutionPlanBuilder builder = new ExecutionPlanBuilder("list servers");

		Long prepareGetTokenId = 0L;
		Long tokenStepId = 1L;
		Long mergerStepId = 2L;
		Long getServersStepId = 3L;
		Long displayStepId = 4L;
		Long successStepId = 5L;
		Long failureStepId = 6L;

		createPrepareGetTokenStep(builder, prepareGetTokenId, tokenStepId);

		createGetTokenStep(builder, tokenStepId, mergerStepId, failureStepId);

		createPrepareGetServersStep(builder, mergerStepId, getServersStepId);

		createGetServersStep(builder, getServersStepId, displayStepId, failureStepId);

		createDisplayStep(builder, displayStepId, successStepId);

		createSuccessStep(builder, successStepId);

		createFailureStep(builder, failureStepId);

		Map<String, Serializable> context = new HashMap<>();
		builder.setInitialExecutionContext(context);

		builder.setBeginStep(0L);

		return builder.createTriggeringProperties();
	}

	public List<InputBinding> getInputBindings() {
		return inputBindings;
	}

	private void createDisplayStep(ExecutionPlanBuilder builder, Long stepId, Long nextStepId) {
		List<NavigationMatcher<Serializable>>  navigationMatchers = new ArrayList<>();
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.DEFAULT, nextStepId));
		builder.addOOActionStep(stepId, CONTEXT_MERGER_CLASS, GET_SERVER_NAMES_METHOD, null, navigationMatchers);
	}
}
