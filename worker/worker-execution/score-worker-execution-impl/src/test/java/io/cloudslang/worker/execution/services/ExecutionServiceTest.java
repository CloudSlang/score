/*
 * Copyright © 2014-2017 EntIT Software LLC, a Micro Focus company (L.P.)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.cloudslang.worker.execution.services;

import io.cloudslang.orchestrator.services.CancelExecutionService;
import io.cloudslang.orchestrator.services.PauseResumeService;
import io.cloudslang.score.api.ControlActionMetadata;
import io.cloudslang.score.api.ExecutionPlan;
import io.cloudslang.score.api.ExecutionStep;
import io.cloudslang.score.events.EventBus;
import io.cloudslang.score.events.EventConstants;
import io.cloudslang.score.facade.TempConstants;
import io.cloudslang.score.facade.entities.Execution;
import io.cloudslang.score.facade.entities.RunningExecutionPlan;
import io.cloudslang.score.facade.execution.ExecutionStatus;
import io.cloudslang.score.facade.execution.ExecutionSummary;
import io.cloudslang.score.facade.execution.PauseReason;
import io.cloudslang.worker.execution.reflection.ReflectionAdapter;
import io.cloudslang.worker.management.WorkerConfigurationService;
import io.cloudslang.worker.management.services.WorkerRecoveryManager;
import io.cloudslang.worker.management.services.dbsupport.WorkerDbSupportService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static io.cloudslang.score.api.execution.ExecutionParametersConsts.ACTION_TYPE;
import static io.cloudslang.score.api.execution.ExecutionParametersConsts.SEQUENTIAL;
import static io.cloudslang.score.facade.execution.PauseReason.SEQUENTIAL_EXECUTION;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created with IntelliJ IDEA.
 * User: kravtsov
 * Date: 02/12/12
 * Time: 14:11
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class ExecutionServiceTest {

	private static final Long RUNNING_EXE_PLAN_ID = 333L;
	private static final Long EXECUTION_STEP_1_ID = 1L;
	private static final Long EXECUTION_STEP_2_ID = 2L;
	private static final Long EXECUTION_ID_1 = 1111L;
	private static final Long EXECUTION_ID_2 = 2222L;
	private static final ControlActionMetadata RUNTIME_EXCEPTION_METADATA = new ControlActionMetadata("classForFailure", "method");

	@Autowired
	private ExecutionServiceImpl executionService;

	@Autowired
	private WorkerDbSupportService workerDbSupportService;

	@Autowired
	private PauseResumeService pauseResumeService;

	@Autowired
	private WorkerConfigurationService workerConfigurationService;

	@Before
	public void init() {
		Mockito.reset(workerDbSupportService, pauseResumeService);
	}

	@Test
	public void handlePausedFlow_NotPausedExecutionTest() throws InterruptedException {
		Execution exe = new Execution(111L,0L, 0L, new HashMap<String,String>(), null);
		exe.getSystemContext().setBranchId("branch_id");
		exe.getSystemContext().put(EventConstants.FLOW_UUID, "flow_uuid");

		//since the resumeService mock will return null - there no such execution in pause state, expect to get false
		boolean result = executionService.handlePausedFlow(exe);

		Assert.assertFalse(result);
	}

	@Test
	public void handlePausedFlow_UserPausedTest() throws InterruptedException {
		final Long executionId = 111L;
		final String branch_id = null;

		Execution exe = getExecutionObjToPause(executionId, branch_id);

		ExecutionSummary execSummary = new ExecutionSummary();
		execSummary.setPauseReason(PauseReason.USER_PAUSED);
		execSummary.setStatus(ExecutionStatus.PENDING_PAUSE);
		when(workerConfigurationService.isExecutionPaused(executionId, branch_id)).thenReturn(true);
		when(pauseResumeService.readPausedExecution(executionId, branch_id)).thenReturn(execSummary);

		boolean result = executionService.handlePausedFlow(exe);

		Mockito.verify(pauseResumeService, VerificationModeFactory.times(1)).writeExecutionObject(executionId, branch_id, exe);
		assertTrue(result);
	}

	@Test
	// branch is running, and parent is paused by the user -> branch should be paused
	public void handlePausedFlow_UserPausedParentTest() throws InterruptedException {
		final Long executionId = 111L;
		final String branch_id = "branch_id";

		Execution exe = getExecutionObjToPause(executionId, branch_id);

		// branch is not paused
		ExecutionSummary branch = new ExecutionSummary();
		branch.setStatus(ExecutionStatus.RUNNING);
		when(workerConfigurationService.isExecutionPaused(executionId, branch_id)).thenReturn(false);

		// parent is paused
		ExecutionSummary parent = new ExecutionSummary();
		parent.setPauseReason(PauseReason.USER_PAUSED);
		parent.setStatus(ExecutionStatus.PENDING_PAUSE);
		when(workerConfigurationService.isExecutionPaused(executionId, null)).thenReturn(true);
		when(pauseResumeService.readPausedExecution(executionId, null)).thenReturn(parent);

		boolean result = executionService.handlePausedFlow(exe);

		Mockito.verify(pauseResumeService, VerificationModeFactory.times(1)).pauseExecution(executionId, branch_id, PauseReason.USER_PAUSED);
		Mockito.verify(pauseResumeService, VerificationModeFactory.times(1)).writeExecutionObject(executionId, branch_id, exe);
		assertTrue(result);
	}

	private Execution getExecutionObjToPause(Long executionId, String branch_id) {
		Execution exe = new Execution(executionId,0L, 0L, new HashMap<String,String>(), null);
		exe.getSystemContext().setBranchId(branch_id);
		exe.getSystemContext().put(EventConstants.FLOW_UUID, "flow_uuid");
		//for events
		exe.getSystemContext().setExecutionId(executionId);
		return exe;
	}

	@Test
	public void handleCancelledFlowsTest() {
		Execution exe = new Execution(EXECUTION_ID_1,0L, 0L, new HashMap<String,String>(), null);

		boolean result = executionService.handleCancelledFlow(exe);

		assertNull(exe.getPosition());
		assertTrue(result);

		exe = new Execution(EXECUTION_ID_2,0L, 0L, new HashMap<String,String>(), null);

		result = executionService.handleCancelledFlow(exe);

        assertNull(exe.getPosition());
        assertTrue(result);

	}

	@Test
	// branch is running and execution reaches sequential operation -> branch should be paused
	public void handlePausedFlow_sequentialOperationReached() throws InterruptedException {

		ExecutionStep executionStep = new ExecutionStep(EXECUTION_STEP_1_ID);
		HashMap<String, Serializable> actionData = new HashMap<>();
		actionData.put(ACTION_TYPE, SEQUENTIAL);
		ControlActionMetadata controlActionMetadata = new ControlActionMetadata("className", "methodName");
		executionStep.setActionData(actionData);
		executionStep.setAction(controlActionMetadata);
		Execution execution = new Execution(EXECUTION_ID_1,0L, 0L, new HashMap<String,String>(), null);
		execution.getSystemContext().put(TempConstants.CONTENT_EXECUTION_STEP, executionStep);
		Map<String,Serializable> metadata = new HashMap<>();
		execution.getSystemContext().putMetaData(metadata);


		ExecutionPlan executionPlan = new ExecutionPlan();
		executionPlan.addStep(executionStep);
		RunningExecutionPlan runningExecutionPlan = new RunningExecutionPlan();
		runningExecutionPlan.setId(RUNNING_EXE_PLAN_ID);
		runningExecutionPlan.setExecutionPlan(executionPlan);
		execution.setRunningExecutionPlanId(runningExecutionPlan.getId());
		when(workerDbSupportService.readExecutionPlanById(RUNNING_EXE_PLAN_ID)).thenReturn(runningExecutionPlan);
		when(workerConfigurationService.isExecutionCancelled(EXECUTION_ID_1)).thenReturn(false);

		executionService.execute(execution);
		//position is still 0
		Assert.assertEquals(0, execution.getPosition().longValue());

		//running execution plan id has not changed as result of not navigating
		Assert.assertEquals(RUNNING_EXE_PLAN_ID, execution.getRunningExecutionPlanId());
		Mockito.verify(pauseResumeService, VerificationModeFactory.times(1)).pauseExecution(any(Long.class), any(String.class), eq(SEQUENTIAL_EXECUTION));
	}

	@Test
	public void loadStepTest() {
		//FromSystemContext
		ExecutionStep executionStep = new ExecutionStep(EXECUTION_STEP_1_ID);

		Execution exe = new Execution(EXECUTION_ID_1,0L, 0L, new HashMap<String,String>(), null);

		exe.getSystemContext().put(TempConstants.CONTENT_EXECUTION_STEP, executionStep);
		Map<String,Serializable> metadata = new HashMap<>();
		exe.getSystemContext().putMetaData(metadata);
		ExecutionStep loadedStep = executionService.loadExecutionStep(exe);

		Assert.assertEquals(executionStep.getExecStepId(), loadedStep.getExecStepId());

		//From DB
		executionStep = new ExecutionStep(EXECUTION_STEP_2_ID);
		ExecutionPlan exePlan = new ExecutionPlan();
		exePlan.addStep(executionStep);
		RunningExecutionPlan plan = new RunningExecutionPlan();
		plan.setExecutionPlan(exePlan);

		when(workerDbSupportService.readExecutionPlanById(RUNNING_EXE_PLAN_ID)).thenReturn(plan);

		executionStep = new ExecutionStep(EXECUTION_STEP_2_ID);

		exe = new Execution(RUNNING_EXE_PLAN_ID, EXECUTION_STEP_2_ID, new HashMap<String,String>());
		exe.getSystemContext().putMetaData(metadata);
		loadedStep = executionService.loadExecutionStep(exe);

		Assert.assertEquals(executionStep.getExecStepId(), loadedStep.getExecStepId());
	}

	@Test
	public void executeStepTest() throws InterruptedException {
		//Test no exception is thrown - all is caught inside
		ExecutionStep executionStep = new ExecutionStep(EXECUTION_STEP_1_ID);
		executionStep.setActionData(new HashMap<String, Serializable>());

		Execution exe = new Execution(0L, 0L, new HashMap<String,String>());

		executionService.executeStep(exe, executionStep);

		Assert.assertEquals(0, exe.getPosition().longValue()); //position is still 0
		assertTrue(exe.getSystemContext().hasStepErrorKey()); //there is error in context
	}

	@Test
	public void executeNavigationTest() throws InterruptedException {
		//Test no exception is thrown - all is caught inside
		ExecutionStep executionStep = new ExecutionStep(EXECUTION_STEP_1_ID);
		executionStep.setNavigation(RUNTIME_EXCEPTION_METADATA);
		executionStep.setNavigationData(new HashMap<String, Serializable>());

		Execution exe = new Execution(0L, 0L, new HashMap<String,String>());

		executionService.navigate(exe, executionStep);

        assertNull(exe.getPosition()); //position was changed to NULL due to exception
		assertTrue(exe.getSystemContext().hasStepErrorKey()); //there is error in context
	}

	@Test
	public void postExecutionSettingsTest() {
		Execution exe = new Execution(1111111L,0L, 0L, new HashMap<String,String>(), null);

		exe.getSystemContext().put(TempConstants.ACTUALLY_OPERATION_GROUP, "Real_Group");
		//for events
		exe.getSystemContext().setExecutionId(123L);

		executionService.postExecutionSettings(exe);

		Assert.assertEquals("Real_Group", exe.getGroupName());
	}

	@Configuration
	static class ConfigurationForTest {

		@Bean
		public EventBus getEventBus() {
			return mock(EventBus.class);
		}

		@Bean
		public ExecutionServiceImpl getExecutionService() {
			return new ExecutionServiceImpl();
		}

		@Bean
		public WorkerConfigurationService getWorkerConfigurationService() {
			WorkerConfigurationService serviceMock = mock(WorkerConfigurationService.class);
			when(serviceMock.isExecutionCancelled(EXECUTION_ID_1)).thenReturn(true);
			when(serviceMock.isExecutionCancelled(EXECUTION_ID_2)).thenReturn(true);
			return serviceMock;
		}

		@Bean
		public ReflectionAdapter getReflectionAdapter() {
			ReflectionAdapter adapter = mock(ReflectionAdapter.class);

			//noinspection unchecked
			when(adapter.executeControlAction(eq(RUNTIME_EXCEPTION_METADATA), any(Map.class))).thenThrow(RuntimeException.class);

			return adapter;
		}

		@Bean
		public WorkerDbSupportService getWorkerDbSupportService() {
			return mock(WorkerDbSupportService.class);
		}

		@Bean
		public PauseResumeService getPauseResumeService() {
			return mock(PauseResumeService.class);
		}

		@Bean
		public CancelExecutionService getCancelExecutionService() {
			return mock(CancelExecutionService.class);
		}

		@Bean
		public WorkerRecoveryManager workerRecoveryManager() {
			return mock(WorkerRecoveryManager.class);
		}

	}
}


