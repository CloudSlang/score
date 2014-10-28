/*
 * Licensed to Hewlett-Packard Development Company, L.P. under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
*/
package com.hp.score.orchestrator.services;

import com.hp.score.facade.execution.ExecutionStatus;
import com.hp.score.orchestrator.entities.ExecutionState;
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
        executionState.setStatus(ExecutionStatus.CANCELED);

        when(executionStateService.readByExecutionIdAndBranchId(executionId, ExecutionState.EMPTY_BRANCH)).thenReturn(executionState);
        assertThat(scorePauseResume.pauseExecution(executionId)).isFalse();
    }

    @Test
    public void testPauseExecution_ExecutionCanBePaused() {
        Long executionId = new Random().nextLong();
        ExecutionState executionState = new ExecutionState();
        executionState.setStatus(ExecutionStatus.RUNNING);

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
