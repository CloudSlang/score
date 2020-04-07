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

package io.cloudslang.score.api;

import java.io.Serializable;
import java.util.Map;

import static java.util.Collections.emptyMap;

/**
 * Date: 8/1/11
 *
 */

public class ExecutionStep implements Serializable {

    private Long execStepId;

    private ControlActionMetadata action;
    private Map<String, ?> actionData;

    private ControlActionMetadata navigation;
    private Map<String, ?> navigationData;

    private boolean splitStep;

    public ExecutionStep() {/** default **/}

    public ExecutionStep(Long execStepId) {
        this.execStepId = execStepId;
    }

    public boolean isSplitStep() {
        return splitStep;
    }

    public void setSplitStep(boolean splitStep) {
        this.splitStep = splitStep;
    }

    public ControlActionMetadata getAction() {
        return action;
    }

    public ExecutionStep setAction(ControlActionMetadata action) {
        this.action = action;
        return this;
    }

    public ControlActionMetadata getNavigation() {
        return navigation;
    }

    public ExecutionStep setNavigation(ControlActionMetadata navigationMetadata) {
        this.navigation = navigationMetadata;
        return this;
    }

    public Map<String, ?> getActionData() {
        return actionData;
    }

    public ExecutionStep setActionData(Map<String, ?> actionData) {
        this.actionData = actionData;
        return this;
    }

    public Map<String, ?> getNavigationData() {
        return (navigationData != null) ? navigationData : emptyMap();
    }

    public ExecutionStep setNavigationData(Map<String, ?> navigationData) {
        this.navigationData = navigationData;
        return this;
    }

    public Long getExecStepId() {
        return execStepId;
    }

    public void setExecStepId(Long execStepId) {
        this.execStepId = execStepId;
    }

    @Override
    public String toString() {
        StringBuilder strBld = new StringBuilder("ExecutionStep: " + execStepId + '\n');
        strBld.append("\t\t").append("ControlAction: ").append(action == null ? "null" : action.toString());
        strBld.append("\n\t\t").append("ControlActionData: ").append(printMap(actionData));
        strBld.append("\n\t\t").append("Navigation: ").append(navigation == null ? "null" : navigation.toString());
        strBld.append("\n\t\t").append("NavigationData: ").append(printMap(navigationData));

        return strBld.toString();
    }

    private String printMap(Map<String, ?> actionData) {
        if (actionData == null) {
            return "null";
        }
        StringBuilder strBld = new StringBuilder("{");
        for (Map.Entry entry : actionData.entrySet()) {
            String StringValue = null;
            if (entry.getValue() != null) {
                StringValue = entry.getValue().toString();
            }
            strBld.append("\n\t\t\t").append(entry.getKey()).append(" -> ").append(StringValue);
        }
        return strBld.append("\n\t\t}").toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        ExecutionStep that = (ExecutionStep) o;

        if (action != null ? !action.equals(that.action) : that.action != null)
            return false;
        if (actionData != null ? !actionData.equals(that.actionData) : that.actionData != null)
            return false;
        if (execStepId != null ? !execStepId.equals(that.execStepId) : that.execStepId != null)
            return false;
        if (navigation != null ? !navigation.equals(that.navigation) : that.navigation != null)
            return false;
        if (navigationData != null ? !navigationData.equals(that.navigationData) : that.navigationData != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = execStepId != null ? execStepId.hashCode() : 0;
        result = 31 * result + (action != null ? action.hashCode() : 0);
        result = 31 * result + (actionData != null ? actionData.hashCode() : 0);
        result = 31 * result + (navigation != null ? navigation.hashCode() : 0);
        result = 31 * result + (navigationData != null ? navigationData.hashCode() : 0);
        return result;
    }
}
