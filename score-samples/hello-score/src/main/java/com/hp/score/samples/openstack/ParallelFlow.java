package com.hp.score.samples.openstack;

import com.google.common.collect.Sets;
import com.hp.score.api.ExecutionPlan;
import com.hp.score.api.TriggeringProperties;
import com.hp.score.samples.openstack.actions.ExecutionPlanBuilder;
import com.hp.score.samples.openstack.actions.InputBinding;
import com.hp.score.samples.openstack.actions.InputBindingFactory;
import com.hp.score.samples.openstack.actions.MatchType;
import com.hp.score.samples.openstack.actions.NavigationMatcher;
import com.hp.score.samples.openstack.actions.SimpleSendEmail;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.hp.score.samples.openstack.OpenstackCommons.GET_MULTI_INSTANCE_RESPONSE_METHOD;
import static com.hp.score.samples.openstack.OpenstackCommons.OPENSTACK_UTILS_CLASS;
import static com.hp.score.samples.openstack.OpenstackCommons.RESPONSE_KEY;
import static com.hp.score.samples.openstack.OpenstackCommons.SERVER_NAMES_LIST_KEY;
import static com.hp.score.samples.openstack.OpenstackCommons.SERVER_NAMES_LIST_MESSAGE;
import static com.hp.score.samples.openstack.OpenstackCommons.SERVER_NAME_KEY;
import static com.hp.score.samples.openstack.OpenstackCommons.SERVER_NAME_MESSAGE;
import static com.hp.score.samples.openstack.OpenstackCommons.SPLIT_SERVERS_INTO_BRANCH_CONTEXTS_METHOD;
import static com.hp.score.samples.openstack.OpenstackCommons.SUCCESS_RESPONSE;
import static com.hp.score.samples.openstack.OpenstackCommons.createFailureStep;
import static com.hp.score.samples.openstack.OpenstackCommons.createSuccessStep;
import static com.hp.score.samples.openstack.OpenstackCommons.mergeInputsWithoutDuplicates;

/**
 * Date: 9/11/2014
 *
 * @author lesant
 */

public class ParallelFlow {
	public static final String OPENSTACK_FLOWS_PACKAGE = "com.hp.score.samples.openstack.ParallelFlow";
	private static final String PARALLEL_SPLIT_CONTEXTS_METHOD = "parallelSplitContexts";
	private List<InputBinding> inputBindings;

	@SuppressWarnings("unused")
	public ParallelFlow() {
		inputBindings = generateInitialInputBindings();
	}

	@SuppressWarnings("unused")
	public List<InputBinding> getInputBindings() {
		return inputBindings;
	}

	private List<InputBinding> generateInitialInputBindings() {
		@SuppressWarnings("unchecked") List<InputBinding> bindings = mergeInputsWithoutDuplicates(
				new CreateServerFlow().getInputBindings());

		bindings.add(InputBindingFactory.createInputBinding("Email host", OpenstackCommons.EMAIL_HOST_KEY, true));
		bindings.add(InputBindingFactory.createInputBinding("Email port", OpenstackCommons.EMAIL_PORT_KEY, true));
		bindings.add(InputBindingFactory.createInputBinding("Email recipient", OpenstackCommons.TO_KEY, true));
		bindings.add(InputBindingFactory.createInputBinding("Email sender", OpenstackCommons.FROM_KEY, true));

		return bindings;
	}

	public TriggeringProperties createParallelFlow() {
		ExecutionPlanBuilder builder = new ExecutionPlanBuilder();

		Long splitContextsStepId = 0L;
		Long parallelSplitId = 1L;
		Long parallelJoinId = 2L;
		Long getResponseStepId = 3L;
		Long successStepId = 4L;
		Long failureStepId = 5L;

		ExecutionPlanBuilder sendEmailBuilder = new ExecutionPlanBuilder();
		createPrepareSendEmailStep(sendEmailBuilder, 0L, 1L);
		createSendEmailStep(sendEmailBuilder, 1L, successStepId, failureStepId);
		createSuccessStep(sendEmailBuilder, successStepId);
		createFailureStep(sendEmailBuilder, failureStepId);
		sendEmailBuilder.setBeginStep(0L);

		ExecutionPlan sendEmailExecutionPlan = sendEmailBuilder.getExecutionPlan();
		String sendEmailFlowUuid = sendEmailExecutionPlan.getFlowUuid();

		CreateServerFlow createServer = new CreateServerFlow();
		ExecutionPlan createServerExecutionPlan = createServer.createServerFlow().getExecutionPlan();
		String createServerFlowUuid = createServerExecutionPlan.getFlowUuid();

		builder.addStep(splitContextsStepId, OPENSTACK_FLOWS_PACKAGE, PARALLEL_SPLIT_CONTEXTS_METHOD, parallelSplitId);

		List<NavigationMatcher<Serializable>> navigationMatchers = new ArrayList<>();

		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.DEFAULT, getResponseStepId));
		List<String> executionPlanIds = new ArrayList<>();
		executionPlanIds.add(sendEmailFlowUuid);
		executionPlanIds.add(createServerFlowUuid);

		builder.addParallel(parallelSplitId, parallelJoinId, executionPlanIds, navigationMatchers);

		navigationMatchers = new ArrayList<>();
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.EQUAL, RESPONSE_KEY, SUCCESS_RESPONSE, successStepId));
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.DEFAULT, failureStepId));

		builder.addStep(getResponseStepId, OPENSTACK_UTILS_CLASS, GET_MULTI_INSTANCE_RESPONSE_METHOD, navigationMatchers);

		createSuccessStep(builder, successStepId);
		createFailureStep(builder, failureStepId);

		ExecutionPlan parallelFlow = builder.getExecutionPlan();
		parallelFlow.setSubflowsUUIDs(Sets.newHashSet(sendEmailFlowUuid,createServerFlowUuid));
		Map<String, ExecutionPlan> dependencies = new HashMap<>();
		dependencies.put(createServerFlowUuid, createServerExecutionPlan);
		dependencies.put(sendEmailFlowUuid, sendEmailExecutionPlan);
		Map<String, Serializable> getRuntimeValues = new HashMap<>();

		return TriggeringProperties.create(parallelFlow).
				setDependencies(dependencies).setRuntimeValues(getRuntimeValues).setStartStep(0L);
	}

	private void createPrepareSendEmailStep(ExecutionPlanBuilder builder, Long prepareSendEmailId, Long sendEmailId) {
		//prepare send email
		List<InputBinding> inputs = new ArrayList<>(2);

		inputs.add(InputBindingFactory.createMergeInputBindingWithSource(OpenstackCommons.HOST_KEY, OpenstackCommons.EMAIL_HOST_KEY));
		inputs.add(InputBindingFactory.createMergeInputBindingWithSource(OpenstackCommons.PORT_KEY, OpenstackCommons.EMAIL_PORT_KEY));

		String body = "CreateServerFlow just started executing. Creating server: ${serverName}";

		inputs.add(InputBindingFactory.createMergeInputBindingWithValue(OpenstackCommons.BODY_KEY, body));
		inputs.add(InputBindingFactory.createMergeInputBindingWithValue(OpenstackCommons.SUBJECT_KEY, "Flow status"));

		builder.addStep(prepareSendEmailId, OpenstackCommons.CONTEXT_MERGER_CLASS, OpenstackCommons.MERGE_METHOD, inputs, sendEmailId);
	}

	private void createSendEmailStep(ExecutionPlanBuilder builder, Long sendEmailId, Long successId, Long failureId) {
		List<NavigationMatcher<Serializable>> navigationMatchers;//send email step
		navigationMatchers = new ArrayList<>();
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.EQUAL, SimpleSendEmail.RETURN_CODE, SimpleSendEmail.SUCCESS, successId));
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.DEFAULT, failureId));
		builder.addOOActionStep(sendEmailId, OpenstackCommons.SEND_EMAIL_CLASS, OpenstackCommons.SEND_EMAIL_METHOD, null, navigationMatchers);
	}

	public void parallelSplitContexts (Map<String, Serializable> executionContext) {
		List<Map<String, Serializable>> branchContexts = new ArrayList<>();
		branchContexts.add(executionContext);
		branchContexts.add(executionContext);

		executionContext.put("branchContexts", (Serializable) branchContexts);
	}
}
