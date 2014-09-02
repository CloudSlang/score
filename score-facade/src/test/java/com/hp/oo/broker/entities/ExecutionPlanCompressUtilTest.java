package com.hp.oo.broker.entities;

import com.hp.score.api.ControlActionMetadata;
import com.hp.score.api.ExecutionPlan;
import com.hp.score.api.ExecutionStep;
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
