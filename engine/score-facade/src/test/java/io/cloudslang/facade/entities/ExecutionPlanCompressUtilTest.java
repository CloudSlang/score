/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package io.cloudslang.facade.entities;

import io.cloudslang.api.ControlActionMetadata;
import io.cloudslang.api.ExecutionPlan;
import io.cloudslang.api.ExecutionStep;
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
