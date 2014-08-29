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
 * Date: 8/28/2014
 *
 * @author Bonczidai Levente
 */
@SuppressWarnings("unused")
public class CreateServerFlow {
	private List<InputBinding> inputBindings;

	public CreateServerFlow() {
		inputBindings = generateInitialInputBindings();
	}

	private List<InputBinding> generateInitialInputBindings() {
		List<InputBinding> bindings = new ArrayList<>();

		bindings.add(createInputBinding(OPENSTACK_HOST_MESSAGE, HOST_KEY, true));
		bindings.add(createInputBindingWithDefaultValue(IDENTITY_PORT_MESSAGE, IDENTITY_PORT_KEY, true, DEFUALT_IDENTITY_PORT));
		bindings.add(createInputBindingWithDefaultValue(COMPUTE_PORT_MESSAGE, COMPUTE_PORT_KEY, true, DEFAULT_COMPUTE_PORT));
		bindings.add(createInputBindingWithDefaultValue(OPEN_STACK_IMAGE_REFERENCE_MESSAGE, IMAGE_REFERENCE_KEY, true, DEFAULT_IMAGE_REF));
		bindings.add(createInputBinding(OPENSTACK_USERNAME_MESSAGE, USERNAME_KEY, true));
		bindings.add(createInputBinding(OPENSTACK_PASSWORD_MESSAGE, PASSWORD_KEY, true));
		bindings.add(createInputBinding(SERVER_NAME_MESSAGE, SERVER_NAME_KEY, true));

		return bindings;
	}

	public TriggeringProperties createServerFlow() {
		ExecutionPlanBuilder builder = new ExecutionPlanBuilder();

		Long prepareGetTokenId = 0L;
		Long tokenStepId = 1L;
		Long contextMergerStepId = 2L;
		Long createServerStepId = 3L;
		Long successStepId = 4L;
		Long failureStepId = 5L;

		createPrepareGetTokenStep(builder, prepareGetTokenId, tokenStepId);

		createGetTokenStep(builder, tokenStepId, contextMergerStepId, failureStepId);

		prepareCreateServerStep(builder, contextMergerStepId, createServerStepId);

		createServerStep(builder, createServerStepId, successStepId, failureStepId);

		createSuccessStep(builder, successStepId);

		createFailureStep(builder, failureStepId);

		Map<String, Serializable> context = new HashMap<>();
		context.put(FLOW_DESCRIPTION, "Create Server");
		builder.setInitialExecutionContext(context);

		builder.setBeginStep(0L);

		return builder.createTriggeringProperties();
	}

	private void prepareCreateServerStep(
			ExecutionPlanBuilder builder,
			Long stepId,
			Long nextStepId) {

		builder.addStep(stepId, CONTEXT_MERGER_CLASS, "prepareCreateServer", nextStepId);
	}

	private void createServerStep(
			ExecutionPlanBuilder builder,
			Long stepId,
			Long successStepId,
			Long failureStepId){
		List<NavigationMatcher<Serializable>> navigationMatchers = new ArrayList<>();

		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.EQUAL, "statusCode", "202", successStepId));
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.DEFAULT, failureStepId));

		builder.addOOActionStep(stepId, HTTP_CLIENT_ACTION_CLASS, HTTP_CLIENT_ACTION_METHOD, null, navigationMatchers);
	}

	public List<InputBinding> getInputBindings() {
		return inputBindings;
	}
}
