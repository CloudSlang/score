package com.hp.oo.orchestrator.services;

import com.hp.oo.engine.queue.entities.ExecutionMessageConverter;
import com.hp.oo.engine.queue.services.QueueDispatcherService;
import com.hp.oo.enginefacade.execution.ExecutionEnums.ExecutionStatus;
import com.hp.oo.enginefacade.execution.PauseReason;
import com.hp.oo.internal.sdk.execution.Execution;
import com.hp.oo.internal.sdk.execution.ExecutionConstants;
import com.hp.oo.orchestrator.entities.ExecutionSummaryEntity;
import com.hp.oo.orchestrator.repositories.ExecutionSummaryRepository;

import static com.hp.oo.enginefacade.execution.ExecutionSummary.EMPTY_BRANCH;

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

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

@SuppressWarnings({"SpringContextConfigurationInspection"})
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class CancelExecutionServiceTest {

    @Autowired
    private CancelExecutionService service;

    @Autowired
    private ExecutionSummaryRepository repository;

    @Autowired
    private ExecutionSerializationUtil executionSerializationUtil;

    @Before
    public void resetMocks() {
        reset(repository);
    }

    /////////////// requestCancelExecution ///////////////

    @Test(expected = RuntimeException.class)
    public void testRequestCancelNullExecutionId() {
        service.requestCancelExecution(null);
    }

    @Test
    public void testValidRequestCancel() {

        // Running
        checkValidRequestCancel(ExecutionStatus.RUNNING, null, ExecutionStatus.PENDING_CANCEL);

        // Paused
        Execution pausedExecutionObj = new Execution(1L, 1L, Collections.singletonList("context_a"));
        when(executionSerializationUtil.objFromBytes(any(byte[].class))).thenReturn(pausedExecutionObj);
        checkValidRequestCancel(ExecutionStatus.PAUSED, PauseReason.INPUT_REQUIRED, ExecutionStatus.PENDING_CANCEL);
        assertThat(pausedExecutionObj.getPosition()).isNull();
        assertThat(pausedExecutionObj.getSystemContext().get(ExecutionConstants.FLOW_TERMINATION_TYPE)).isEqualTo(ExecutionStatus.CANCELED);

        // Cancel
        checkValidRequestCancel(ExecutionStatus.CANCELED, null, ExecutionStatus.CANCELED);
    }

    private void checkValidRequestCancel(ExecutionStatus origStatus, PauseReason pauseReason, ExecutionStatus expStatusAfterCancellation) {
        String executionId = "111";
        ExecutionSummaryEntity ex1 = createExecutionSummary(executionId, origStatus);
        ex1.setPauseReason(pauseReason);
        when(repository.findByExecutionIdAndBranchId(executionId, EMPTY_BRANCH)).thenReturn(ex1);
        boolean result = service.requestCancelExecution(executionId);

        // Validation - Status should be updated by the service
        assertThat(result).isTrue();
        assertThat(ex1.getStatus()).as("Wrong status after cancelling the execution").isEqualTo(expStatusAfterCancellation);
    }

    @Test
    public void testValidRequestCancel_pausedBranches() {
        mockPausedParentAndBranchAndRequestCancel(PauseReason.BRANCH_PAUSED, PauseReason.DISPLAY);
    }

    @Test
    public void testValidRequestCancel_userPausedWithBranches() {
        mockPausedParentAndBranchAndRequestCancel(PauseReason.USER_PAUSED, PauseReason.USER_PAUSED);
    }

    @Test
    public void testValidRequestCancel_pausedBranchesNoWorkersInGroup() {
        mockPausedParentAndBranchAndRequestCancel(PauseReason.NO_WORKERS_IN_GROUP, PauseReason.NO_WORKERS_IN_GROUP);
    }

    private void mockPausedParentAndBranchAndRequestCancel(PauseReason parentPausedReason, PauseReason branchPausedReason) {
        String executionId = "111";
        ExecutionSummaryEntity parent = createExecutionSummary(executionId, ExecutionStatus.PAUSED);
        parent.setPauseReason(parentPausedReason);
        when(repository.findByExecutionIdAndBranchId(executionId, EMPTY_BRANCH)).thenReturn(parent);
        // mock branch
        ExecutionSummaryEntity branch1 = createExecutionSummary(executionId, ExecutionStatus.PAUSED);
        branch1.setBranchId("b1");
        branch1.setPauseReason(branchPausedReason);

        Execution pausedExecutionObj = new Execution(1L, 1L, Collections.singletonList("context_a"));
        when(executionSerializationUtil.objFromBytes(any(byte[].class))).thenReturn(pausedExecutionObj);
        when(repository.findByExecutionId(executionId)).thenReturn(Arrays.asList(parent, branch1));

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
        ExecutionSummaryEntity ex1 = createExecutionSummary(executionId, executionStatus);

        when(repository.findByExecutionIdAndBranchId(executionId, EMPTY_BRANCH)).thenReturn(ex1);
        boolean result = service.requestCancelExecution(executionId);


        // Validation - Status should be updated by the service
        assertThat(result).isFalse();
        assertThat(ex1.getStatus()).as("Execution status shouldn't change").isEqualTo(executionStatus);
    }

    @Test
    public void testNotExistExecution() {
        String executionId = "stam";
        when(repository.findByExecutionIdAndBranchId(executionId, EMPTY_BRANCH)).thenReturn(null);
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
        ExecutionSummaryEntity entity = null;
        if (expResult) {
            entity = createExecutionSummary("123", executionStatus);
        }

        when(repository.findByExecutionIdAndBranchIdAndStatusIn(executionId, EMPTY_BRANCH, getCancelStatuses())).thenReturn(entity);

        boolean result = service.isCanceledExecution(executionId);
        assertThat(result).isEqualTo(expResult);
    }

    /////////////// readCancelledExecutions ///////////////

    @Test
    public void testReadCancelledExecutions() {
        ExecutionSummaryEntity cancel = createExecutionSummary("cancelId", ExecutionStatus.CANCELED);
        ExecutionSummaryEntity pendingCancel = createExecutionSummary("pendingCancelId", ExecutionStatus.PENDING_CANCEL);
        createExecutionSummary("completedId", ExecutionStatus.COMPLETED);

        when(repository.findByStatusIn(getCancelStatuses())).thenReturn(Arrays.asList(cancel, pendingCancel));
        List<String> result = service.readCanceledExecutionsIds();
        assertThat(result).hasSize(2);
    }

    @Test
    public void testReadCancelledExecutions_emptyList() {
        //noinspection unchecked
        when(repository.findByStatusIn(getCancelStatuses())).thenReturn(Collections.EMPTY_LIST);
        List<String> result = service.readCanceledExecutionsIds();
        assertThat(result).isEmpty();
    }

    @Test
    public void testReadCancelledExecutions_null() {
        when(repository.findByStatusIn(getCancelStatuses())).thenReturn(null);
        List<String> result = service.readCanceledExecutionsIds();
        assertThat(result).isEmpty();
    }

    /////////////// Helpers ///////////////

    private ExecutionSummaryEntity createExecutionSummary(String executionId, ExecutionStatus status) {
        ExecutionSummaryEntity entity = new ExecutionSummaryEntity();
        entity.setExecutionId(executionId);
        entity.setStatus(status);

        return entity;
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
        ExecutionSummaryRepository getExecutionSummaryRepository() {
            return mock(ExecutionSummaryRepository.class);
        }

        @Bean
        QueueDispatcherService queueDispatcherService() {
            return mock(QueueDispatcherService.class);
        }
    }
}