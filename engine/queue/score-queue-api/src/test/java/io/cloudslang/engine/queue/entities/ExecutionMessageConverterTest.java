/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package io.cloudslang.engine.queue.entities;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import io.cloudslang.score.lang.SystemContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

/**
 * Created with IntelliJ IDEA.
 * User: kravtsov
 * Date: 20/11/12
 * Time: 14:35
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ExecutionMessageConverterTest.ConfigurationForTest.class)
public class ExecutionMessageConverterTest {
    @Autowired
    private ExecutionMessageConverter executionMessageConverter;
    @Autowired
    private SensitiveDataHandler sensitiveDataHandler;

    @Test
    public void testConverter() throws IOException {
        List<String> names = new ArrayList<>();
        names.add("lala");
        MyExecutionForTest execution = new MyExecutionForTest(111L, 999L, 0L, names);

        Payload payload = executionMessageConverter.createPayload(execution);

        MyExecutionForTest afterConvert = executionMessageConverter.extractExecution(payload);

        assertEquals(execution.getPosition(), afterConvert.getPosition());
        assertEquals(execution.getExecutionId(), afterConvert.getExecutionId());
        assertEquals(execution.getRunningExecutionPlanId(), afterConvert.getRunningExecutionPlanId());
    }

    @Test
    public void testCreatePayloadAndSensitiveDataHandlerReturnsFalse() {
        when(sensitiveDataHandler.containsSensitiveData(any(SystemContext.class), anyMap())).thenReturn(false);
        List<String> names = new ArrayList<>();
        names.add("lala");
        MyExecutionForTest execution = new MyExecutionForTest(111L, 999L, 0L, names);

        Payload payload = executionMessageConverter.createPayload(execution);
        assertFalse(executionMessageConverter.containsSensitiveData(payload));
        assertTrue(payload.getData()[0] == 0);

        payload = executionMessageConverter.createPayload(execution);
        assertFalse(executionMessageConverter.containsSensitiveData(payload));
        assertTrue(payload.getData()[0] == 0);

        payload = executionMessageConverter.createPayload(execution, false);
        assertFalse(executionMessageConverter.containsSensitiveData(payload));
        assertTrue(payload.getData()[0] == 0);

        payload = executionMessageConverter.createPayload(execution, true);
        assertTrue(executionMessageConverter.containsSensitiveData(payload));
        assertTrue(payload.getData()[0] == 1);
    }

    @Test
    public void testCreatePayloadAndSensitiveDataHandlerReturnsTrue() {
        when(sensitiveDataHandler.containsSensitiveData(any(SystemContext.class), anyMap())).thenReturn(true);
        List<String> names = new ArrayList<>();
        names.add("lala");
        MyExecutionForTest execution = new MyExecutionForTest(111L, 999L, 0L, names);

        Payload payload = executionMessageConverter.createPayload(execution);
        assertTrue(executionMessageConverter.containsSensitiveData(payload));
        assertTrue(payload.getData()[0] == 1);

        payload = executionMessageConverter.createPayload(execution);
        assertTrue(executionMessageConverter.containsSensitiveData(payload));
        assertTrue(payload.getData()[0] == 1);

        payload = executionMessageConverter.createPayload(execution, false);
        assertTrue(executionMessageConverter.containsSensitiveData(payload));
        assertTrue(payload.getData()[0] == 1);

        payload = executionMessageConverter.createPayload(execution, true);
        assertTrue(executionMessageConverter.containsSensitiveData(payload));
        assertTrue(payload.getData()[0] == 1);
    }

    @Test
    public void testPayloadForSensitiveData() {
        Payload p = new Payload();
        p.setData(new byte[]{0, 1, 2});
        assertFalse(executionMessageConverter.containsSensitiveData(p));

        p = new Payload();
        p.setData(new byte[]{1, 0, 0});
        assertTrue(executionMessageConverter.containsSensitiveData(p));
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


    @Configuration
    static class ConfigurationForTest {

        @Bean
        public ExecutionMessageConverter executionMessageConverter() {
            return new ExecutionMessageConverter();
        }

        @Bean
        public SensitiveDataHandler sensitiveDataHandler() {
            return mock(SensitiveDataHandler.class);
        }

    }
}
