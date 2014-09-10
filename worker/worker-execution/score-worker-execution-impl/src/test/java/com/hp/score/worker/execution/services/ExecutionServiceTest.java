package com.hp.score.worker.execution.services;

import com.hp.score.api.ControlActionMetadata;
import com.hp.score.api.ExecutionPlan;
import com.hp.score.api.ExecutionStep;
import com.hp.score.events.EventBus;
import com.hp.score.events.EventConstants;
import com.hp.score.facade.TempConstants;
import com.hp.score.facade.entities.Execution;
import com.hp.score.facade.entities.RunningExecutionPlan;
import com.hp.score.facade.execution.ExecutionStatus;
import com.hp.score.facade.execution.ExecutionSummary;
import com.hp.score.facade.execution.PauseReason;
import com.hp.score.lang.SystemContext;
import com.hp.score.orchestrator.services.CancelExecutionService;
import com.hp.score.orchestrator.services.PauseResumeService;
import com.hp.score.worker.execution.reflection.ReflectionAdapter;
import com.hp.score.worker.management.WorkerConfigurationService;
import com.hp.score.worker.management.services.WorkerRecoveryManager;
import com.hp.score.worker.management.services.dbsupport.WorkerDbSupportService;
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

import static org.mockito.Matchers.any;
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
	static final Long EXECUTION_ID_1 = 1111L;
	static final Long EXECUTION_ID_2 = 2222L;

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
	public void handlePausedFlow_NotPausedExecutionTest() {
		Execution exe = new Execution(111L,0L, 0L, new HashMap<String,String>(), null);
		exe.getSystemContext().setBranchId("branch_id");
		exe.getSystemContext().put(EventConstants.FLOW_UUID, "flow_uuid");

		//since the resumeService mock will return null - there no such execution in pause state, expect to get false
		boolean result = executionService.handlePausedFlow(exe);

		Assert.assertFalse(result);
	}

	@Test
	public void handlePausedFlow_UserPausedTest() {
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
		Assert.assertTrue(result);
	}

	@Test
	// branch is running, and parent is paused by the user -> branch should be paused
	public void handlePausedFlow_UserPausedParentTest() {
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
		Assert.assertTrue(result);
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

		Assert.assertEquals(exe.getPosition(), null);
		Assert.assertEquals(result, true);

		exe = new Execution(EXECUTION_ID_2,0L, 0L, new HashMap<String,String>(), null);

		result = executionService.handleCancelledFlow(exe);

		Assert.assertEquals(exe.getPosition(), null);
		Assert.assertEquals(result, true);

	}

	@Test
	public void loadStepTest() {
		//FromSystemContext
		ExecutionStep executionStep = new ExecutionStep(EXECUTION_STEP_1_ID);

		Execution exe = new Execution(EXECUTION_ID_1,0L, 0L, new HashMap<String,String>(), null);

		exe.getSystemContext().put(TempConstants.CONTENT_EXECUTION_STEP, executionStep);

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

		loadedStep = executionService.loadExecutionStep(exe);

		Assert.assertEquals(executionStep.getExecStepId(), loadedStep.getExecStepId());
	}

	@Test
	public void executeStepTest() {
		//Test no exception is thrown - all is caught inside
		ExecutionStep executionStep = new ExecutionStep(EXECUTION_STEP_1_ID);
		executionStep.setActionData(new HashMap<String, Serializable>());

		Execution exe = new Execution(0L, 0L, new HashMap<String,String>());

		executionService.executeStep(exe, executionStep);

		Assert.assertEquals(0, exe.getPosition().longValue()); //position is still 0
		Assert.assertTrue(exe.getSystemContext().hasStepErrorKey()); //there is error in context
	}

	@Test
	public void executeNavigationTest() {
		//Test no exception is thrown - all is caught inside
		ExecutionStep executionStep = new ExecutionStep(EXECUTION_STEP_1_ID);
		executionStep.setNavigation(new ControlActionMetadata("class", "method"));
		executionStep.setNavigationData(new HashMap<String, Serializable>());

		Execution exe = new Execution(0L, 0L, new HashMap<String,String>());

		executionService.navigate(exe, executionStep);

		Assert.assertEquals(null, exe.getPosition()); //position was changed to NULL due to exception
		Assert.assertTrue(exe.getSystemContext().hasStepErrorKey()); //there is error in context
	}

	@Test
	public void postExecutionSettingsTest() {
		Execution exe = new Execution(1111111L,0L, 0L, new HashMap<String,String>(), null);

		exe.getSystemContext().put(TempConstants.ACTUALLY_OPERATION_GROUP, "Real_Group");
		exe.getSystemContext().put(TempConstants.MUST_GO_TO_QUEUE, true);
		//for events
		exe.getSystemContext().setExecutionId(123L);

		executionService.postExecutionSettings(exe);

		Assert.assertEquals("Real_Group", exe.getGroupName());
		Assert.assertEquals(true, exe.isMustGoToQueue());
		Assert.assertEquals(false, exe.getSystemContext().get(TempConstants.MUST_GO_TO_QUEUE));
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
			when(adapter.executeControlAction(any(ControlActionMetadata.class), any(Map.class))).thenThrow(RuntimeException.class);

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


