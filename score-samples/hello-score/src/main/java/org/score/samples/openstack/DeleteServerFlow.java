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
import static org.score.samples.openstack.actions.InputBinding.createInputBinding;
import static org.score.samples.openstack.actions.InputBinding.createInputBindingWithDefaultValue;

/**
 * Date: 8/29/2014
 *
 * @author Bonczidai Levente
 */
public class DeleteServerFlow {
	private List<InputBinding> inputBindings;

	public DeleteServerFlow() {
		inputBindings = generateInitialInputBindings();
	}

	public TriggeringProperties deleteServerFlow(){
		ExecutionPlanBuilder builder = new ExecutionPlanBuilder();

		Long prepareGetTokenId = 0L;
		Long tokenStepId = 1L;
		Long mergerStepId = 2L;
		Long getServersStepId = 3L;
		Long getServerIdStepId = 4L;
		Long secondMergerStepId = 5L;
		Long deleteServerStepId = 6L;
		Long successStepId = 7L;
		Long failureStepId = 8L;

		createPrepareGetTokenStep(builder, prepareGetTokenId, tokenStepId);

		createGetTokenStep(builder, tokenStepId, mergerStepId, failureStepId);

		createPrepareGetServersStep(builder, mergerStepId, getServersStepId);

		createGetServersStep(builder, getServersStepId, getServerIdStepId, failureStepId);

		createGetServerIdStep(builder, getServerIdStepId, secondMergerStepId);

		createCreatePrepareDeleteServerStep(builder, secondMergerStepId, deleteServerStepId);

		createDeleteServerStep(builder, deleteServerStepId, successStepId, failureStepId);

		createSuccessStep(builder, successStepId);

		createFailureStep(builder, failureStepId);

		Map<String, Serializable> context = new HashMap<>();
		context.put(FLOW_DESCRIPTION, "Delete Server");
		builder.setInitialExecutionContext(context);

		builder.setBeginStep(0L);

		return builder.createTriggeringProperties();
	}

	public List<InputBinding> getInputBindings() {
		return inputBindings;
	}

	private List<InputBinding> generateInitialInputBindings() {
		List<InputBinding> bindings = new ArrayList<>();

		bindings.add(createInputBinding(OPENSTACK_HOST_MESSAGE, HOST_KEY, true));
		bindings.add(createInputBindingWithDefaultValue(IDENTITY_PORT_MESSAGE, IDENTITY_PORT_KEY, true, DEFUALT_IDENTITY_PORT));
		bindings.add(createInputBindingWithDefaultValue(COMPUTE_PORT_MESSAGE, COMPUTE_PORT_KEY, true, DEFAULT_COMPUTE_PORT));
		bindings.add(createInputBinding(OPENSTACK_USERNAME_MESSAGE, USERNAME_KEY, true));
		bindings.add(createInputBinding(OPENSTACK_PASSWORD_MESSAGE, PASSWORD_KEY, true));
		bindings.add(createInputBinding(SERVER_NAME_MESSAGE, SERVER_NAME_KEY, true));

		return bindings;
	}

	private void createDeleteServerStep(ExecutionPlanBuilder builder, Long stepId, Long nextStepId, Long defaultStepId) {
		List<NavigationMatcher<Serializable>> navigationMatchers = new ArrayList<>();

		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.EQUAL, "returnCode", "0", nextStepId));
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.DEFAULT, defaultStepId));

		builder.addOOActionStep(stepId, HTTP_CLIENT_ACTION_CLASS, HTTP_CLIENT_ACTION_METHOD, null, navigationMatchers);
	}

	private void createCreatePrepareDeleteServerStep(ExecutionPlanBuilder builder, Long stepId, Long nextStepId) {
		builder.addStep(stepId, CONTEXT_MERGER_CLASS, PREPARE_DELETE_SERVER_METHOD, nextStepId);
	}

	private void createGetServerIdStep(ExecutionPlanBuilder builder, Long stepId, Long nextStepId) {
		builder.addStep(stepId, CONTEXT_MERGER_CLASS, GET_SERVER_ID_METHOD, nextStepId);
	}
}
