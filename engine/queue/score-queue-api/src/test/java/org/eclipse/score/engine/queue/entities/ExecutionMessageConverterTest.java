/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package org.eclipse.score.engine.queue.entities;

import junit.framework.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kravtsov
 * Date: 20/11/12
 * Time: 14:35
 */
public class ExecutionMessageConverterTest {
    ExecutionMessageConverter converter = new ExecutionMessageConverter();

    @Test
    public void testConverter() throws IOException {
        List<String> names = new ArrayList<>();
        names.add("lala");
        MyExecutionForTest execution = new MyExecutionForTest("exe_id", 999L, 0L, names);

        Payload payload = converter.createPayload(execution);

        MyExecutionForTest afterConvert = converter.extractExecution(payload);

        Assert.assertEquals(execution.getPosition(), afterConvert.getPosition());
        Assert.assertEquals(execution.getExecutionId(), afterConvert.getExecutionId());
        Assert.assertEquals(execution.getRunningExecutionPlanId(), afterConvert.getRunningExecutionPlanId());
    }

//    @Test
//    public void testConverterWithSession() throws IOException {
//        List<String> names = new ArrayList<>();
//        names.add("serializableSessionContext");
//        Execution execution = new Execution(999L, 0L, names);
//
//        StepSerializableSessionObject stepSerializableObject = new StepSerializableSessionObject("sessionCounter_1bbd31ec-0531-4180-8b70-a592355ea043");
//        stepSerializableObject.setValue(1);
//
//        execution.getSerializableSessionContext().put("sessionCounter_1bbd31ec-0531-4180-8b70-a592355ea043", stepSerializableObject);
//
//        Payload payload = converter.createPayload(execution);
//
//        Execution afterConvert = converter.extractExecution(payload);
//
//        Assert.assertEquals(execution.getPosition(), afterConvert.getPosition());
//        Assert.assertEquals(execution.getExecutionId(), afterConvert.getExecutionId());
//        Assert.assertEquals(execution.getSerializableSessionContext().get("sessionCounter_1bbd31ec-0531-4180-8b70-a592355ea043").getName(), afterConvert.getSerializableSessionContext().get("sessionCounter_1bbd31ec-0531-4180-8b70-a592355ea043").getName());
//    }
}
