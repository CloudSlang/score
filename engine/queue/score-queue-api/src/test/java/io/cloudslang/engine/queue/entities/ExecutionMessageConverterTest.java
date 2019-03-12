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
import io.cloudslang.score.lang.SystemContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyMapOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ExecutionMessageConverterTest.ConfigurationForTest.class)
public class ExecutionMessageConverterTest {
    @Autowired
    private ExecutionMessageConverter executionMessageConverter;
    @Autowired
    private SensitiveDataHandler sensitiveDataHandler;

    @Test
    public void testConverterCompressed() {
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
    public void testConverterCompressedWithExecution() {

        Execution execution = new Execution(123L, 1L, new HashMap<>());
        execution.setExecutionId(1L);
        Payload payload = executionMessageConverter.createPayload(execution);

        Execution afterConvert = executionMessageConverter.extractExecution(payload);

        assertEquals(execution.getPosition(), afterConvert.getPosition());
        assertEquals(execution.getExecutionId(), afterConvert.getExecutionId());
        assertEquals(execution.getRunningExecutionPlanId(), afterConvert.getRunningExecutionPlanId());
    }

    @Test
    public void testConverterLegacyCaseUncompressed() throws IOException {
        List<String> names = new ArrayList<>();
        names.add("1");
        names.add("2");
        names.add("3");

        MyExecutionForTest execution = new MyExecutionForTest(222L, 9L, 500L, names);

        Payload payload = createLegacyPayload(execution);

        MyExecutionForTest afterConvert = executionMessageConverter.extractExecution(payload);

        assertEquals(execution.getPosition(), afterConvert.getPosition());
        assertEquals(execution.getExecutionId(), afterConvert.getExecutionId());
        assertEquals(execution.getRunningExecutionPlanId(), afterConvert.getRunningExecutionPlanId());
    }

    @Test
    public void testConverterLegacyCaseWithExecution() throws IOException {

        Execution execution = new Execution(576767600L, 1L, new HashMap<>());
        execution.setExecutionId(2L);

        Payload payload = createLegacyPayload(execution);

        Execution afterConvert = executionMessageConverter.extractExecution(payload);

        assertEquals(execution.getPosition(), afterConvert.getPosition());
        assertEquals(execution.getExecutionId(), afterConvert.getExecutionId());
        assertEquals(execution.getRunningExecutionPlanId(), afterConvert.getRunningExecutionPlanId());
    }

    @Test
    public void testCreatePayloadAndSensitiveDataHandlerReturnsFalse() {
        when(sensitiveDataHandler.containsSensitiveData(any(SystemContext.class), anyMapOf(String.class, Serializable.class))).thenReturn(false);
        List<String> names = new ArrayList<>();
        names.add("lala");
        MyExecutionForTest execution = new MyExecutionForTest(111L, 999L, 0L, names);

        Payload payload = executionMessageConverter.createPayload(execution);
        assertFalse(executionMessageConverter.containsSensitiveData(payload));
        assertEquals(0, payload.getData()[0]);

        payload = executionMessageConverter.createPayload(execution);
        assertFalse(executionMessageConverter.containsSensitiveData(payload));
        assertEquals(0, payload.getData()[0]);

        payload = executionMessageConverter.createPayload(execution, false);
        assertFalse(executionMessageConverter.containsSensitiveData(payload));
        assertEquals(0, payload.getData()[0]);

        payload = executionMessageConverter.createPayload(execution, true);
        assertTrue(executionMessageConverter.containsSensitiveData(payload));
        assertEquals(1, payload.getData()[0]);
    }

    @Test
    public void testCreatePayloadAndSensitiveDataHandlerReturnsTrue() {
        when(sensitiveDataHandler.containsSensitiveData(any(SystemContext.class), anyMapOf(String.class, Serializable.class))).thenReturn(true);
        List<String> names = new ArrayList<>();
        names.add("lala");
        MyExecutionForTest execution = new MyExecutionForTest(111L, 999L, 0L, names);

        Payload payload = executionMessageConverter.createPayload(execution);
        assertTrue(executionMessageConverter.containsSensitiveData(payload));
        assertEquals(1, payload.getData()[0]);

        payload = executionMessageConverter.createPayload(execution);
        assertTrue(executionMessageConverter.containsSensitiveData(payload));
        assertEquals(1, payload.getData()[0]);

        payload = executionMessageConverter.createPayload(execution, false);
        assertTrue(executionMessageConverter.containsSensitiveData(payload));
        assertEquals(1, payload.getData()[0]);

        payload = executionMessageConverter.createPayload(execution, true);
        assertTrue(executionMessageConverter.containsSensitiveData(payload));
        assertEquals(1, payload.getData()[0]);
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

    private Payload createLegacyPayload(Serializable serializable) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
        byteArrayOutputStream.write(0);
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            objectOutputStream.writeObject(serializable);
            objectOutputStream.flush();
        }

        return new Payload(byteArrayOutputStream.toByteArray());
    }

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
