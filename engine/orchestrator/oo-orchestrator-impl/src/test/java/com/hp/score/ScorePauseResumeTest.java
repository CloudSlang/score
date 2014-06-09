package com.hp.score;

import com.hp.oo.enginefacade.execution.ExecutionEnums;
import com.hp.oo.orchestrator.services.PauseResumeService;
import com.hp.score.entities.RunState;
import com.hp.score.services.RunStateService;
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
    private RunStateService runStateService;

    @Autowired
    private ScorePauseResume scorePauseResume;

    @Test
    public void testPauseExecution_NoSuchExecution() {
        Long executionId = new Random().nextLong();

        when(runStateService.readByRunIdAndBranchId(executionId.toString(), RunState.EMPTY_BRANCH)).thenReturn(null);
        assertThat(scorePauseResume.pauseExecution(executionId)).isFalse();
    }

    @Test
    public void testPauseExecution_ExecutionCannotBePaused() {
        Long executionId = new Random().nextLong();
        RunState runState = new RunState();
        runState.setStatus(ExecutionEnums.ExecutionStatus.CANCELED);

        when(runStateService.readByRunIdAndBranchId(executionId.toString(), RunState.EMPTY_BRANCH)).thenReturn(runState);
        assertThat(scorePauseResume.pauseExecution(executionId)).isFalse();
    }

    @Test
    public void testPauseExecution_ExecutionCanBePaused() {
        Long executionId = new Random().nextLong();
        RunState runState = new RunState();
        runState.setStatus(ExecutionEnums.ExecutionStatus.RUNNING);

        when(runStateService.readByRunIdAndBranchId(executionId.toString(), RunState.EMPTY_BRANCH)).thenReturn(runState);
        assertThat(scorePauseResume.pauseExecution(executionId)).isTrue();
    }

    @Configuration
    static class ScoreExecutionTestContext {

        @Bean
        public ScorePauseResume scoreExecution() {
            return new ScorePauseResumeImpl();
        }

        @Bean
        public RunStateService runStateService() {
            return mock(RunStateService.class);
        }

        @Bean
        public PauseResumeService pauseResumeService() {
            return mock(PauseResumeService.class);
        }

    }

}
