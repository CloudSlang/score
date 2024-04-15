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

package io.cloudslang.engine.queue.entities;

import io.cloudslang.score.facade.entities.Execution;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kravtsov
 * Date: 22/11/12
 * Time: 15:45
 */
public class MyExecutionForTest extends Execution {

        private Long executionId;
        private Long runningExecutionPlanId;
        private Long position;
        private List<String> contextsNames;

        public MyExecutionForTest(){

        }
        public  MyExecutionForTest(Long executionId, Long runningExecutionPlanId, Long position, List<String> contextsNames) {
            this.executionId = executionId;
            this.runningExecutionPlanId = runningExecutionPlanId;
            this.position = position;
            this.contextsNames = contextsNames;
        }

        public List<String> getContextsNames() {
            return contextsNames;
        }

        public void setContextsNames(List<String> contextsNames) {
            this.contextsNames = contextsNames;
        }

        public Long getExecutionId() {
            return executionId;
        }

        public void setExecutionId(Long executionId) {
            this.executionId = executionId;
        }

        public Long getRunningExecutionPlanId() {
            return runningExecutionPlanId;
        }

        public void setRunningExecutionPlanId(Long runningExecutionPlanId) {
            this.runningExecutionPlanId = runningExecutionPlanId;
        }

        public Long getPosition() {
            return position;
        }

    }
