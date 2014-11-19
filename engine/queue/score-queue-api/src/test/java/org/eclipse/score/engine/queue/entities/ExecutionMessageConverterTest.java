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
