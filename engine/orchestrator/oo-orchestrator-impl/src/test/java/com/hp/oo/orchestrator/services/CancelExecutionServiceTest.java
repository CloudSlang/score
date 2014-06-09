package com.hp.oo.orchestrator.services;

import com.hp.oo.engine.queue.entities.ExecutionMessageConverter;
import com.hp.oo.engine.queue.services.QueueDispatcherService;
import com.hp.oo.enginefacade.execution.ExecutionEnums.ExecutionStatus;
import com.hp.oo.internal.sdk.execution.Execution;
import com.hp.oo.internal.sdk.execution.ExecutionConstants;
import com.hp.score.entities.RunState;
import com.hp.score.services.RunStateService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.hp.oo.enginefacade.execution.ExecutionSummary.EMPTY_BRANCH;
import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@SuppressWarnings({"SpringContextConfigurationInspection"})
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = CancelExecutionServiceTest.Configurator.class)
public class CancelExecutionServiceTest {

    @Autowired
    private CancelExecutionService service;

    @Autowired
    private RunStateService runStateService;

    @Autowired
    private ExecutionSerializationUtil executionSerializationUtil;

    @Before
    public void resetMocks() {
        reset(runStateService);
    }

    /////////////// requestCancelExecution ///////////////

    @Test(expected = RuntimeException.class)
    public void testRequestCancelNullExecutionId() {
        service.requestCancelExecution(null);
    }

    @Test
    public void testValidRequestCancel() {

        // Running
        checkValidRequestCancel(ExecutionStatus.RUNNING, ExecutionStatus.PENDING_CANCEL);

        // Paused
        Execution pausedExecutionObj = new Execution(1L, 1L, Collections.singletonList("context_a"));
        when(executionSerializationUtil.objFromBytes(any(byte[].class))).thenReturn(pausedExecutionObj);
        checkValidRequestCancel(ExecutionStatus.PAUSED, ExecutionStatus.PENDING_CANCEL);
        assertThat(pausedExecutionObj.getPosition()).isNull();
        assertThat(pausedExecutionObj.getSystemContext().get(ExecutionConstants.FLOW_TERMINATION_TYPE)).isEqualTo(ExecutionStatus.CANCELED);

        // Cancel
        checkValidRequestCancel(ExecutionStatus.CANCELED, ExecutionStatus.CANCELED);
    }

    private void checkValidRequestCancel(ExecutionStatus origStatus, ExecutionStatus expStatusAfterCancellation) {
        String executionId = "111";
        RunState ex1 = createRun(executionId, origStatus);
        when(runStateService.readByRunIdAndBranchId(executionId, EMPTY_BRANCH)).thenReturn(ex1);
        boolean result = service.requestCancelExecution(executionId);

        // Validation - Status should be updated by the service
        assertThat(result).isTrue();
        assertThat(ex1.getStatus()).as("Wrong status after cancelling the execution").isEqualTo(expStatusAfterCancellation);
    }

    @Test
    public void testValidRequestCancel_pausedBranches() {
        mockPausedParentAndBranchAndRequestCancel();
    }

    @Test
    public void testValidRequestCancel_userPausedWithBranches() {
        mockPausedParentAndBranchAndRequestCancel();
    }

    @Test
    public void testValidRequestCancel_pausedBranchesNoWorkersInGroup() {
        mockPausedParentAndBranchAndRequestCancel();
    }

    private void mockPausedParentAndBranchAndRequestCancel() {
        String executionId = "111";
        RunState parent = createRun(executionId, ExecutionStatus.PAUSED);
        when(runStateService.readByRunIdAndBranchId(executionId, EMPTY_BRANCH)).thenReturn(parent);
        // mock branch
        RunState branch1 = createRun(executionId, ExecutionStatus.PAUSED);
        branch1.setBranchId("b1");

        Execution pausedExecutionObj = new Execution(1L, 1L, Collections.singletonList("context_a"));
        when(executionSerializationUtil.objFromBytes(any(byte[].class))).thenReturn(pausedExecutionObj);
        when(runStateService.readByRunId(executionId)).thenReturn(Arrays.asList(parent, branch1));

        boolean result = service.requestCancelExecution(executionId);

        // Validation - Parent status should be Pending-cancel
        assertThat(result).isTrue();
        assertThat(parent.getStatus()).as("Wrong status after cancelling the execution").isEqualTo(ExecutionStatus.PENDING_CANCEL);

        // Branch should be canceled
        assertThat(branch1.getStatus()).as("Wrong status after cancelling the branch").isEqualTo(ExecutionStatus.PENDING_CANCEL);
        assertThat(pausedExecutionObj.getPosition()).isNull();
        assertThat(pausedExecutionObj.getSystemContext().get(ExecutionConstants.FLOW_TERMINATION_TYPE)).isEqualTo(ExecutionStatus.CANCELED);
    }

    @Test
    public void testInvalidRequestCancel() {

        ExecutionStatus[] invalidStatusesForCancel = {ExecutionStatus.COMPLETED, ExecutionStatus.PENDING_PAUSE, ExecutionStatus.SYSTEM_FAILURE};

        for (ExecutionStatus status : invalidStatusesForCancel) {
            checkInvalidRequestCancel(status);
        }
    }

    private void checkInvalidRequestCancel(ExecutionStatus executionStatus) {
        String executionId = "111";
        RunState ex1 = createRun(executionId, executionStatus);

        when(runStateService.readByRunIdAndBranchId(executionId, EMPTY_BRANCH)).thenReturn(ex1);
        boolean result = service.requestCancelExecution(executionId);

        // Validation - Status should be updated by the service
        assertThat(result).isFalse();
        assertThat(ex1.getStatus()).as("Execution status shouldn't change").isEqualTo(executionStatus);
    }

    @Test
    public void testNotExistExecution() {
        String executionId = "stam";
        when(runStateService.readByRunIdAndBranchId(executionId, EMPTY_BRANCH)).thenReturn(null);
        boolean result = service.requestCancelExecution(executionId);

        assertThat(result).isFalse();
    }

    /////////////// isCancelledExecution ///////////////

    @Test
    public void testIsCancelledExecution() {
        checkIsCancelledExecution(ExecutionStatus.CANCELED, true);
        checkIsCancelledExecution(ExecutionStatus.PENDING_CANCEL, true);

        checkIsCancelledExecution(ExecutionStatus.RUNNING, false);
        checkIsCancelledExecution(ExecutionStatus.PAUSED, false);
        checkIsCancelledExecution(ExecutionStatus.PENDING_PAUSE, false);
        checkIsCancelledExecution(ExecutionStatus.COMPLETED, false);
        checkIsCancelledExecution(ExecutionStatus.SYSTEM_FAILURE, false);
    }

    private void checkIsCancelledExecution(ExecutionStatus executionStatus, boolean expResult) {
        String executionId = "123";
        RunState entity = null;
        if (expResult) {
            entity = createRun("123", executionStatus);
        }

        when(runStateService.readCancelledRun(executionId)).thenReturn(entity);

        boolean result = service.isCanceledExecution(executionId);
        assertThat(result).isEqualTo(expResult);
    }

    /////////////// readCancelledExecutions ///////////////

    @Test
    public void testReadCancelledExecutions() {
        when(runStateService.readRunIdByStatuses(getCancelStatuses())).thenReturn(Arrays.asList("cancelId", "pendingCancelId"));
        List<String> result = service.readCanceledExecutionsIds();
        assertThat(result).hasSize(2);
    }

    @Test
    public void testReadCancelledExecutions_emptyList() {
        //noinspection unchecked
        when(runStateService.readRunIdByStatuses(getCancelStatuses())).thenReturn(Collections.EMPTY_LIST);
        List<String> result = service.readCanceledExecutionsIds();
        assertThat(result).isEmpty();
    }

    @Test
    public void testReadCancelledExecutions_null() {
        when(runStateService.readRunIdByStatuses(getCancelStatuses())).thenReturn(null);
        List<String> result = service.readCanceledExecutionsIds();
        assertThat(result).isEmpty();
    }

    /////////////// Helpers ///////////////

    private RunState createRun(String runId, ExecutionStatus status) {
        RunState runState = new RunState();
        runState.setRunId(runId);
        runState.setStatus(status);

        return runState;
    }

    private List<ExecutionStatus> getCancelStatuses() {
        return Arrays.asList(ExecutionStatus.CANCELED, ExecutionStatus.PENDING_CANCEL);
    }

    @Configuration
    static class Configurator {

        @Bean
        CancelExecutionService cancelExecutionService() {
            return new CancelExecutionServiceImpl();
        }

        @Bean
        ExecutionMessageConverter executionMessageConverter() {
            return new ExecutionMessageConverter();
        }

        @Bean
        ExecutionSerializationUtil executionSerializationUtil() {
            return mock(ExecutionSerializationUtil.class);
        }

        @Bean
        RunStateService runService() {
            return mock(RunStateService.class);
        }

        @Bean
        QueueDispatcherService queueDispatcherService() {
            return mock(QueueDispatcherService.class);
        }
    }
}