package org.score.samples.openstack;

import com.hp.score.api.TriggeringProperties;
import org.score.samples.openstack.actions.ExecutionPlanBuilder;
import org.score.samples.openstack.actions.InputBinding;
import org.score.samples.openstack.actions.InputBindingFactory;
import org.score.samples.openstack.actions.MatchType;
import org.score.samples.openstack.actions.NavigationMatcher;
import org.score.samples.openstack.actions.SimpleSendEmail;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


import static org.score.samples.openstack.OpenstackCommons.*;
import static org.score.samples.openstack.actions.FinalStepActions.RESPONSE_KEY;
import static org.score.samples.openstack.actions.FinalStepActions.SUCCESS_KEY;


/**
 * Date: 8/29/2014
 *
 * @author Bonczidai Levente
 */
@SuppressWarnings("unused")
public class OpenStackHealthCheckFlow {

	private List<InputBinding> inputBindings;

	public OpenStackHealthCheckFlow() {
		inputBindings = generateInitialInputBindings();
	}

	@SuppressWarnings("unchecked")
	private List<InputBinding> generateInitialInputBindings() {
		List<InputBinding> bindings = mergeInputsWithoutDuplicates(
				new CreateServerFlow().getInputBindings(),
				new ValidateServerExistsFlow().getInputBindings(),
				new DeleteServerFlow().getInputBindings());

		bindings.remove(InputBindingFactory.createInputBinding(SERVER_NAME_MESSAGE, SERVER_NAME_KEY, true));
		bindings.add(InputBindingFactory.createInputBindingWithDefaultValue(SERVER_NAME_MESSAGE, SERVER_NAME_KEY, true, OPEN_STACK_HEALTH_CHECK_SERVER_NAME));
		bindings.add(InputBindingFactory.createInputBinding("Email host", EMAIL_HOST_KEY, true));
		bindings.add(InputBindingFactory.createInputBinding("Email port", EMAIL_PORT_KEY, true));
		bindings.add(InputBindingFactory.createInputBinding("Fail email recipient", TO_KEY, true));
		bindings.add(InputBindingFactory.createInputBinding("Fail email sender", FROM_KEY, true));

		return bindings;
	}

	public TriggeringProperties openStackHealthCheckFlow() {
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

		createCreateServerSubflow(builder, createServerSplitId, createServerJoinId, validateServerSplitId, prepareSendEmailId);

		createValidateServerSubflow(builder, validateServerSplitId, validateServerJoinId, deleteServerSplitId, prepareSendEmailId);

		createDeleteServerSubflow(builder, deleteServerSplitId, deleteServerJoinId, successId, prepareSendEmailId);

		createPrepareSendEmailStep(builder, sendEmailId, prepareSendEmailId);

		createSendEmailStep(builder, sendEmailId, failureId);

		createSuccessStep(builder, successId);

		createFailureStep(builder, failureId);

		return builder.createTriggeringProperties();
	}

	public List<InputBinding> getInputBindings() {
		return inputBindings;
	}
	private void createCreateServerSubflow(ExecutionPlanBuilder builder, Long createServerSplitId, Long createServerJoinId, Long validateServerSplitId, Long prepareSendEmailId) {
		//create server subflow
		List<NavigationMatcher<Serializable>> navigationMatchers = new ArrayList<>(2);
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.EQUAL, RESPONSE_KEY, SUCCESS_KEY, validateServerSplitId));
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.DEFAULT, prepareSendEmailId));
		CreateServerFlow createServerFlow = new CreateServerFlow();
		TriggeringProperties triggeringProperties = createServerFlow.createServerFlow();
		List<String> inputKeys = new ArrayList<>();
		for (InputBinding inputBinding : createServerFlow.getInputBindings()) {
			inputKeys.add(inputBinding.getSourceKey());
		}
		builder.addSubflow(createServerSplitId, createServerJoinId, triggeringProperties, inputKeys, navigationMatchers);
	}

	private void createValidateServerSubflow(ExecutionPlanBuilder builder, Long validateServerSplitId, Long validateServerJoinId, Long deleteServerSplitId, Long prepareSendEmailId) {
		TriggeringProperties triggeringProperties;
		List<String> inputKeys;
		List<NavigationMatcher<Serializable>> navigationMatchers;//validate server subflow
		ValidateServerExistsFlow validateServerExistsFlow = new ValidateServerExistsFlow();
		triggeringProperties = validateServerExistsFlow.validateServerExistsFlow();
		inputKeys = new ArrayList<>();
		for (InputBinding inputBinding : validateServerExistsFlow.getInputBindings()) {
			inputKeys.add(inputBinding.getSourceKey());
		}
		navigationMatchers = new ArrayList<>(2);
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.EQUAL, RESPONSE_KEY, SUCCESS_KEY, deleteServerSplitId));
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.DEFAULT, prepareSendEmailId));
		builder.addSubflow(validateServerSplitId, validateServerJoinId, triggeringProperties, inputKeys, navigationMatchers);

	}

	private void createDeleteServerSubflow(ExecutionPlanBuilder builder, Long deleteServerSplitId, Long deleteServerJoinId, Long successId, Long prepareSendEmailId) {
		TriggeringProperties triggeringProperties;
		List<String> inputKeys;
		List<NavigationMatcher<Serializable>> navigationMatchers;//delete server subflow
		DeleteServerFlow deleteServerFlow = new DeleteServerFlow();
		triggeringProperties = deleteServerFlow.deleteServerFlow();
		inputKeys = new ArrayList<>();
		for (InputBinding inputBinding : deleteServerFlow.getInputBindings()) {
			inputKeys.add(inputBinding.getSourceKey());
		}
		navigationMatchers = new ArrayList<>(2);
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.EQUAL, RESPONSE_KEY, SUCCESS_KEY, successId));
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.DEFAULT, prepareSendEmailId));
		builder.addSubflow(deleteServerSplitId, deleteServerJoinId, triggeringProperties, inputKeys, navigationMatchers);
	}

	private void createPrepareSendEmailStep(ExecutionPlanBuilder builder, Long sendEmailId, Long prepareSendEmailId) {
		//prepare send email
		List<InputBinding> inputs = new ArrayList<>(2);

		inputs.add(InputBindingFactory.createMergeInputBindingWithSource(HOST_KEY, EMAIL_HOST_KEY));
		inputs.add(InputBindingFactory.createMergeInputBindingWithSource(PORT_KEY, EMAIL_PORT_KEY));


		String failureFrom = "Failure from step \"${"+ FLOW_DESCRIPTION + "}\"";
		failureFrom += ":\n${" + RETURN_RESULT_KEY + "}";


		inputs.add(InputBindingFactory.createMergeInputBindingWithValue(BODY_KEY, failureFrom));
		inputs.add(InputBindingFactory.createMergeInputBindingWithValue(SUBJECT_KEY, "OpenStack failure"));


		builder.addStep(prepareSendEmailId, CONTEXT_MERGER_CLASS, MERGE_METHOD, inputs, sendEmailId);
	}

	private void createSendEmailStep(ExecutionPlanBuilder builder, Long sendEmailId, Long failureId) {
		List<NavigationMatcher<Serializable>> navigationMatchers;//send email step
		navigationMatchers = new ArrayList<>();
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.EQUAL, SimpleSendEmail.RETURN_CODE, SimpleSendEmail.SUCCESS, failureId));
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.DEFAULT, failureId));
		builder.addOOActionStep(sendEmailId, SEND_EMAIL_CLASS, SEND_EMAIL_METHOD, null, navigationMatchers);
	}

}
