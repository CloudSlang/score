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
package io.cloudslang.runtime.impl.python.executor;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cloudslang.runtime.api.python.PythonEvaluationResult;
import io.cloudslang.runtime.api.python.PythonExecutionResult;
import io.cloudslang.runtime.api.python.PythonExecutorCommunicationService;
import io.cloudslang.runtime.api.python.PythonExecutorConfigurationDataService;
import io.cloudslang.runtime.api.python.PythonRuntimeService;
import io.cloudslang.runtime.api.python.entities.PythonExecutorDetails;
import io.cloudslang.runtime.impl.python.external.EvaluationResults;
import io.cloudslang.runtime.impl.python.external.ExternalPythonEvalException;
import io.cloudslang.runtime.impl.python.external.ExternalPythonRuntimeServiceImpl;
import io.cloudslang.runtime.impl.python.external.ExternalPythonScriptException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.ws.rs.ProcessingException;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;

public class ExternalPythonExecutorServiceImpl extends ExternalPythonRuntimeServiceImpl implements PythonRuntimeService {
    private static final Logger logger = LogManager.getLogger(ExternalPythonExecutorServiceImpl.class);
    private static final String EXTERNAL_PYTHON_EXECUTOR_EVAL_PATH = "/rest/v1/eval";
    private final ObjectMapper objectMapper;

    @Autowired
    PythonExecutorCommunicationService pythonExecutorCommunicationService;
    @Autowired
    @Qualifier("pythonExecutorConfigurationDataService")
    PythonExecutorConfigurationDataService pythonExecutorConfigurationDataService;

    public ExternalPythonExecutorServiceImpl(Semaphore executionControlSemaphore,
                                             Semaphore testingControlSemaphore) {
        super(executionControlSemaphore, testingControlSemaphore);
        JsonFactory factory = new JsonFactory();
        factory.enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES);
        factory.enable(JsonWriteFeature.ESCAPE_NON_ASCII.mappedFeature());
        this.objectMapper = new ObjectMapper(factory);
    }

    @Override
    public PythonEvaluationResult eval(String prepareEnvironmentScript, String script, Map<String, Serializable> vars) {
        try {
            return getPythonEvaluationResult(script, prepareEnvironmentScript, vars);
        } catch (JsonProcessingException ie) {
            logger.error(ie);
            throw new ExternalPythonScriptException("Execution was interrupted while waiting for a python permit.");
        } catch (ProcessingException exception) {
            throw new ExternalPythonScriptException("Python server is down or can't process the execution of the python expression");
        }
    }

    @Override
    public PythonExecutionResult exec(Set<String> dependencies, String script, Map<String, Serializable> vars) {
        return super.exec(dependencies, script, vars);
    }

    @Override
    public PythonEvaluationResult test(String prepareEnvironmentScript, String script, Map<String, Serializable> vars, long timeout) {
        return super.test(prepareEnvironmentScript, script, vars, timeout);
    }


    private PythonEvaluationResult getPythonEvaluationResult(String expression, String prepareEnvironmentScript,
                                                             Map<String, Serializable> context) throws JsonProcessingException {
        String accessedResources = "accessed_resources_set";
        String payload = generatePayloadForEval(expression, prepareEnvironmentScript, context);
        EvaluationResults scriptResults = executeRequestOnPythonServer("POST", payload);

        String exception = scriptResults.getException();
        if (StringUtils.isNotEmpty(exception)) {
            logger.error(String.format("Failed to execute script {%s}", exception));
            throw new ExternalPythonEvalException(exception);
        }
        context.put(accessedResources, (Serializable) scriptResults.getAccessedResources());
        return new PythonEvaluationResult(processReturnResult(scriptResults), context);
    }


    private EvaluationResults executeRequestOnPythonServer(String method, String payload) throws JsonProcessingException {
        PythonExecutorDetails pythonExecutorDetails = pythonExecutorConfigurationDataService.getPythonExecutorConfiguration();
        Pair<Integer, String> scriptResponse = pythonExecutorCommunicationService.performRequest(EXTERNAL_PYTHON_EXECUTOR_EVAL_PATH, method, payload, pythonExecutorDetails.getRuntimeEncodedAuth());
        return objectMapper.readValue(scriptResponse.getRight(), EvaluationResults.class);
    }

    private String generatePayloadForEval(String expression, String prepareEnvironmentScript,
                                          Map<String, Serializable> context) throws JsonProcessingException {
        HashMap<String, Serializable> payload = new HashMap<>(3);
        payload.put("expression", expression);
        payload.put("envSetup", prepareEnvironmentScript);
        payload.put("context", (Serializable) context);
        return objectMapper.writeValueAsString(payload);
    }

    private Serializable processReturnResult(EvaluationResults results) throws JsonProcessingException {
        EvaluationResults.ReturnType returnType = results.getReturnType();
        if (returnType == null) {
            throw new RuntimeException("Missing return type for return result.");
        }
        switch (returnType) {
            case BOOLEAN:
                return Boolean.valueOf(results.getReturnResult());
            case INTEGER:
                return new BigInteger(results.getReturnResult());
            case LIST:
                return objectMapper.readValue(results.getReturnResult(), new TypeReference<ArrayList<Serializable>>() {
                });
            default:
                return results.getReturnResult();
        }
    }
}