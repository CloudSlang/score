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

package io.cloudslang.score.facade.entities;

import io.cloudslang.score.lang.SystemContext;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Date: 8/1/11
 *
 */
public class Execution implements Serializable {
    private Long executionId;
    private Long runningExecutionPlanId;
    private Long position;

    protected Map<String, Serializable> contexts;
    protected SystemContext systemContext = new SystemContext();

    public Execution() {
        this.contexts = new HashMap<>();
    }

    public Execution(Long executionId, Long runningExecutionPlanId, Long position, Map<String, ? extends Serializable> contexts, Map<String, Serializable> systemContext) {
        this(runningExecutionPlanId, position, contexts);
        if(systemContext != null) {
            this.systemContext.putAll(systemContext);
        }
        this.executionId = executionId;
    }

    public Execution(Long runningExecutionPlanId, Long position, Map<String, ? extends Serializable> contexts) {
        this();
        this.position = position;
        this.runningExecutionPlanId = runningExecutionPlanId;
        if(contexts != null) {
            this.contexts.putAll(contexts);
        }
    }

    public String getRobotSessionAlias() {
        return systemContext.getRobotSessionAlias();
    }

    public void setRobotSessionAlias(String alias) {
        systemContext.setRobotSessionAlias(alias);
    }

    public String getGroupName() {
        return systemContext.getWorkerGroupName();
    }

    public void setGroupName(String groupName) {
        systemContext.setWorkerGroupName(groupName);
    }

    public String getRobotGroupName() {
        return systemContext.getRobotGroupName();
    }

    public void setRobotGroupName(String robotGroupName) {
        systemContext.setRobotGroupName(robotGroupName);
    }

    public Long getExecutionId() {
        return executionId;
    }

    public Long getRunningExecutionPlanId() {
        return runningExecutionPlanId;
    }

    public void setRunningExecutionPlanId(Long runningExecutionPlanId){
        this.runningExecutionPlanId = runningExecutionPlanId;
    }

    public Long getPosition() {
        return position;
    }

    public Execution setPosition(Long position) {
        this.position = position;
        return this;
    }

    public Map<String, Serializable> getContexts() {
        return contexts;
    }

    public void setContexts(Map<String, Serializable> contexts) {
        this.contexts = contexts;
    }

    public SystemContext getSystemContext() {
        return systemContext;
    }

    public void setExecutionId(Long executionId){
        this.executionId = executionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Execution execution = (Execution) o;

        if (executionId != null ? !executionId.equals(execution.executionId) : execution.executionId != null)
            return false;
        if (position != null ? !position.equals(execution.position) : execution.position != null)
            return false;
        if (runningExecutionPlanId != null ? !runningExecutionPlanId.equals(execution.runningExecutionPlanId) : execution.runningExecutionPlanId != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = executionId != null ? executionId.hashCode() : 0;
        result = 31 * result + (runningExecutionPlanId != null ? runningExecutionPlanId.hashCode() : 0);
        result = 31 * result + (position != null ? position.hashCode() : 0);
        return result;
    }
}
