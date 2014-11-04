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
package com.hp.score.lang.tests.runtime;

import com.hp.oo.sdk.content.annotations.Param;
import com.hp.score.lang.runtime.ReturnValues;
import com.hp.score.lang.runtime.RunEnvironment;
import com.hp.score.lang.runtime.steps.ActionSteps;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.python.util.PythonInterpreter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.Serializable;
import java.util.*;

import static com.hp.score.lang.entities.ActionType.JAVA;
import static com.hp.score.lang.entities.ActionType.PYTHON;
import static com.hp.score.lang.entities.ScoreLangConstants.*;
import static org.junit.Assert.assertEquals;

/**
 * Date: 10/31/2014
 *
 * @author Bonczidai Levente
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ActionInvokerTest.Config.class)
public class ActionInvokerTest {

    private static final long DEFAULT_TIMEOUT = 10000;

    @Autowired
    private ActionSteps actionInvoker;

    @Test(timeout = DEFAULT_TIMEOUT)
    public void doActionJavaTest() {
        //prepare doAction arguments
        RunEnvironment runEnv = new RunEnvironment();
        Map<String, Serializable> initialCallArguments = new HashMap<>();
        initialCallArguments.put("name", "nameTest");
        initialCallArguments.put("role", "roleTest");
        runEnv.putCallArguments(initialCallArguments);

        Map<String, Object> nonSerializableExecutionData = new HashMap<>();

        Map<String, Serializable> actionData = new HashMap<>();

        //invoke doAction
        actionInvoker.doAction(
                runEnv,
                nonSerializableExecutionData,
                JAVA,
                ActionInvokerTest.class.getName(),
                "doJavaAction",
                actionData);

        //construct expected outputs
        Map<String, String> expectedOutputs = new HashMap<>();
        expectedOutputs.put("name", "nameTest");
        expectedOutputs.put("role", "roleTest");

        //extract actual outputs
        ReturnValues actualReturnValues = runEnv.removeReturnValues();
        Map<String, String> actualOutputs = actualReturnValues.getOutputs();

        //verify matching
        assertEquals("Java action outputs are not as expected", expectedOutputs, actualOutputs);
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void doActionPythonTest() {
        //prepare doAction arguments
        RunEnvironment runEnv = new RunEnvironment();
        Map<String, Serializable> initialCallArguments = new HashMap<>();
        initialCallArguments.put("host", "localhost");
        initialCallArguments.put("port", "-> 8080 if True else 8081");
        runEnv.putCallArguments(initialCallArguments);

        Map<String, Object> nonSerializableExecutionData = new HashMap<>();

        Map<String, Serializable> actionData = new HashMap<>();

        String userPythonScript = "import os\n" +
                "print host\n" +
                "print port\n" +
                "os.system(\"ping -c 1 \" + host)\n" +
                "url = 'http://' + host + ':' + str(port)\n" +
                "print url";
        actionData.put(PYTHON_SCRIPT_KEY, userPythonScript);

        List<String> inputList = new ArrayList<>();
        inputList.add("host");
        inputList.add("port");
        actionData.put(INPUT_LIST_KEY, (Serializable) inputList);

        Map<String, String> userOutputs = new LinkedHashMap<>();
        userOutputs.put("url", "-> 'http://' + host + ':' + str(port) + '/oo'");
        userOutputs.put("url2", "-> url");
        userOutputs.put("another", "just a string");
        actionData.put(USER_OUTPUTS_KEY, (Serializable) userOutputs);

        //invoke doAction
        actionInvoker.doAction(runEnv, nonSerializableExecutionData, PYTHON, "", "", actionData);

        //construct expected outputs
        Map<String, String> expectedOutputs = new HashMap<>();
        expectedOutputs.put("url", "http://localhost:8080/oo");
        expectedOutputs.put("url2", "http://localhost:8080");
        expectedOutputs.put("another", "just a string");

        //extract actual outputs
        ReturnValues actualReturnValues = runEnv.removeReturnValues();
        Map<String, String> actualOutputs = actualReturnValues.getOutputs();

        //verify matching
        assertEquals("Python action outputs are not as expected", expectedOutputs, actualOutputs);
    }

    @SuppressWarnings("unused")
    public Map<String, String> doJavaAction(@Param("name") String name,
                                            @Param("role") String role) {
        Map<String, String> returnValues = new HashMap<>();
        returnValues.put("name", name);
        returnValues.put("role", role);
        return returnValues;
    }

    static class Config{

        @Bean
        public ActionSteps actionSteps(){
            return new ActionSteps();
        }

        @Bean
        public PythonInterpreter pythonInterpreter(){
            return new PythonInterpreter();
        }

    }
}
