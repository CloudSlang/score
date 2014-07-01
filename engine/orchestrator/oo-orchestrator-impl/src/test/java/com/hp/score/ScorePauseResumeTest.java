package com.hp.score;

import com.hp.oo.enginefacade.execution.ExecutionEnums;
import com.hp.oo.orchestrator.services.PauseResumeService;
import com.hp.score.entities.ExecutionState;
import com.hp.score.services.ExecutionStateService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Random;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * User: maromg
 * Date: 01/06/2014
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class ScorePauseResumeTest {

    @Autowired
    private ExecutionStateService executionStateService;

    @Autowired
    private ScorePauseResume scorePauseResume;

    @Test
    public void testPauseExecution_NoSuchExecution() {
        Long executionId = new Random().nextLong();

        when(executionStateService.readByExecutionIdAndBranchId(executionId, ExecutionState.EMPTY_BRANCH)).thenReturn(null);
        assertThat(scorePauseResume.pauseExecution(executionId)).isFalse();
    }

    @Test
    public void testPauseExecution_ExecutionCannotBePaused() {
        Long executionId = new Random().nextLong();
        ExecutionState executionState = new ExecutionState();
        executionState.setStatus(ExecutionEnums.ExecutionStatus.CANCELED);

        when(executionStateService.readByExecutionIdAndBranchId(executionId, ExecutionState.EMPTY_BRANCH)).thenReturn(executionState);
        assertThat(scorePauseResume.pauseExecution(executionId)).isFalse();
    }

    @Test
    public void testPauseExecution_ExecutionCanBePaused() {
        Long executionId = new Random().nextLong();
        ExecutionState executionState = new ExecutionState();
        executionState.setStatus(ExecutionEnums.ExecutionStatus.RUNNING);

        when(executionStateService.readByExecutionIdAndBranchId(executionId, ExecutionState.EMPTY_BRANCH)).thenReturn(executionState);
        assertThat(scorePauseResume.pauseExecution(executionId)).isTrue();
    }

    @Configuration
    static class ScoreExecutionTestContext {

        @Bean
        public ScorePauseResume scoreExecution() {
            return new ScorePauseResumeImpl();
        }

        @Bean
        public ExecutionStateService executionStateService() {
            return mock(ExecutionStateService.class);
        }

        @Bean
        public PauseResumeService pauseResumeService() {
            return mock(PauseResumeService.class);
        }

    }

}
