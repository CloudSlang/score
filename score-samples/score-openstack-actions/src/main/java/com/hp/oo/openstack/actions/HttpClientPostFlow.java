package com.hp.oo.openstack.actions;

import com.hp.score.api.ControlActionMetadata;
import com.hp.score.api.ExecutionPlan;
import com.hp.score.api.ExecutionStep;

import java.util.UUID;

/**
 * Date: 7/18/2014
 *
 * @author Bonczidai Levente
 */
public class HttpClientPostFlow {
	public static final Long START_STEP_ID = 0L;
	public static final Long SUCCESS_STEP_ID = 1L;
	public static final Long FAILURE_STEP_ID = 2L;

	public ExecutionPlan createExecutionPlan() {
		ExecutionPlan executionPlan = new ExecutionPlan();

		executionPlan.setFlowUuid(UUID.randomUUID().toString());

		ExecutionStep postStep = createBasicStep(START_STEP_ID, "com.hp.oo.openstack.actions.HttpClientPostWrapper", "post",
				"com.hp.oo.openstack.actions.HttpClientPostWrapper", "postNavigation");

		ExecutionStep successStep = createBasicStep(SUCCESS_STEP_ID, "com.hp.oo.openstack.actions.ReturnStepActions", "successStepAction",
						"com.hp.oo.openstack.actions.ReturnStepActions", "finalStepNavigation");

		ExecutionStep failureStep = createBasicStep(FAILURE_STEP_ID, "com.hp.oo.openstack.actions.ReturnStepActions", "failureStepAction",
				"com.hp.oo.openstack.actions.ReturnStepActions", "finalStepNavigation");


		executionPlan.addStep(postStep);
		executionPlan.addStep(successStep);
		executionPlan.addStep(failureStep);
		executionPlan.setBeginStep(postStep.getExecStepId());

		return executionPlan;
	}

	private ExecutionStep createBasicStep(long stepId,
										  String actionClassName,
										  String actionMethodName,
										  String navigationClassName,
										  String navigationMethodName) {
		ExecutionStep step = new ExecutionStep(stepId);
		step.setAction(new ControlActionMetadata(actionClassName, actionMethodName));
		step.setNavigation(new ControlActionMetadata(navigationClassName, navigationMethodName));
		step.setSplitStep(false);
		return step;
	}
}
