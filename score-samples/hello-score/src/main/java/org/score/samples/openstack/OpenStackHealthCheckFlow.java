package org.score.samples.openstack;

import com.hp.score.api.TriggeringProperties;
import org.score.samples.openstack.actions.ExecutionPlanBuilder;
import org.score.samples.openstack.actions.InputBinding;
import org.score.samples.openstack.actions.MatchType;
import org.score.samples.openstack.actions.NavigationMatcher;
import org.score.samples.openstack.actions.SimpleSendEmail;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static org.score.samples.openstack.OpenstackCommons.CONTEXT_MERGER_CLASS;
import static org.score.samples.openstack.OpenstackCommons.FAILURE_STEP_ACTION;
import static org.score.samples.openstack.OpenstackCommons.FINAL_STEP_ACTIONS_CLASS;
import static org.score.samples.openstack.OpenstackCommons.PREPARE_SEND_EMAIL_METHOD;
import static org.score.samples.openstack.OpenstackCommons.SEND_EMAIL_CLASS;
import static org.score.samples.openstack.OpenstackCommons.SEND_EMAIL_METHOD;
import static org.score.samples.openstack.OpenstackCommons.SUCCESS_STEP_ACTION;
import static org.score.samples.openstack.OpenstackCommons.mergeInputsWithoutDuplicates;
import static org.score.samples.openstack.actions.FinalStepActions.RESPONSE_KEY;
import static org.score.samples.openstack.actions.FinalStepActions.SUCCESS_KEY;
import static org.score.samples.openstack.actions.InputBinding.createInputBinding;

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

		bindings.add(createInputBinding("Email host", "emailHost", true));
		bindings.add(createInputBinding("Email port", "emailPort", true));
		bindings.add(createInputBinding("Fail email recipient", "to", true));
		bindings.add(createInputBinding("Fail email sender", "from", true));

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

		//create server subflow
		List<NavigationMatcher<Serializable>> navigationMatchers = new ArrayList<>();
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.EQUAL, RESPONSE_KEY, SUCCESS_KEY, validateServerSplitId));
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.DEFAULT, prepareSendEmailId));
		CreateServerFlow createServerFlow = new CreateServerFlow();
		TriggeringProperties triggeringProperties = createServerFlow.createServerFlow();
		List<String> inputKeys = new ArrayList<>();
		for (InputBinding inputBinding : createServerFlow.getInputBindings()) {
			inputKeys.add(inputBinding.getInputKey());
		}
		builder.addSubflow(createServerSplitId, createServerJoinId, triggeringProperties, inputKeys, navigationMatchers);

		//validate server subflow
		ValidateServerExistsFlow validateServerExistsFlow = new ValidateServerExistsFlow();
		triggeringProperties = validateServerExistsFlow.validateServerExistsFlow();
		inputKeys = new ArrayList<>();
		for (InputBinding inputBinding : validateServerExistsFlow.getInputBindings()) {
			inputKeys.add(inputBinding.getInputKey());
		}
		navigationMatchers = new ArrayList<>();
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.EQUAL, RESPONSE_KEY, SUCCESS_KEY, deleteServerSplitId));
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.DEFAULT, prepareSendEmailId));
		builder.addSubflow(validateServerSplitId, validateServerJoinId, triggeringProperties, inputKeys, navigationMatchers);

		//delete server subflow
		DeleteServerFlow deleteServerFlow = new DeleteServerFlow();
		triggeringProperties = deleteServerFlow.deleteServerFlow();
		inputKeys = new ArrayList<>();
		for (InputBinding inputBinding : deleteServerFlow.getInputBindings()) {
			inputKeys.add(inputBinding.getInputKey());
		}
		navigationMatchers = new ArrayList<>();
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.EQUAL, RESPONSE_KEY, SUCCESS_KEY, successId));
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.DEFAULT, prepareSendEmailId));
		builder.addSubflow(deleteServerSplitId, deleteServerJoinId, triggeringProperties, inputKeys, navigationMatchers);

		//prepare send email
		builder.addStep(prepareSendEmailId, CONTEXT_MERGER_CLASS, PREPARE_SEND_EMAIL_METHOD, sendEmailId);

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

	public List<InputBinding> getInputBindings() {
		return inputBindings;
	}
}
