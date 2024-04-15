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

import io.cloudslang.score.api.ControlActionMetadata;
import io.cloudslang.score.api.ExecutionPlan;
import io.cloudslang.score.api.ExecutionStep;
import junit.framework.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by IntelliJ IDEA.
 * User: kravtsov
 * Date: 01/08/12
 * Time: 13:36
 */
public class ExecutionPlanCompressUtilTest {

    @Test
    public void testReadWrite() throws ClassNotFoundException, IOException {

        //Create execution plan
        ControlActionMetadata controlActionMetadata = new ControlActionMetadata("className", "methodName");
        Map<String, String> map = new HashMap<>();
        map.put("key", "value");

        ExecutionStep exeStep = new ExecutionStep();
        exeStep.setAction(controlActionMetadata);
        exeStep.setActionData(map);
        exeStep.setExecStepId(0L);

        ExecutionPlan exePlan = new ExecutionPlan();

        exePlan.setBeginStep(0L);
        exePlan.setFlowUuid(UUID.randomUUID().toString());
        exePlan.setLanguage("afl");
        exePlan.setName("Test flow");
        exePlan.addStep(exeStep);

        //write and then read
        byte[] bytes = ExecutionPlanCompressUtil.getBytesFromExecutionPlan(exePlan);

        ExecutionPlan executionPlanAfterStream = ExecutionPlanCompressUtil.getExecutionPlanFromBytes(bytes);

        Assert.assertTrue(executionPlanAfterStream.getBeginStep().equals(0L));

        Assert.assertTrue(executionPlanAfterStream.getStep(0L).getActionData().get("key").equals("value"));

        System.out.println("Map values: " + executionPlanAfterStream.getStep(0L).getActionData().get("key"));
    }
}
