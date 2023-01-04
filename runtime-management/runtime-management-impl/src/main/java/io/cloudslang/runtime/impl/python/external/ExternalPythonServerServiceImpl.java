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
package io.cloudslang.runtime.impl.python.external;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cloudslang.runtime.api.python.PythonEvaluationResult;
import io.cloudslang.runtime.api.python.PythonExecutionResult;
import io.cloudslang.runtime.api.python.PythonRuntimeService;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Base64.getEncoder;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static org.jboss.resteasy.util.HttpHeaderNames.AUTHORIZATION;
import static org.jboss.resteasy.util.HttpHeaderNames.CONTENT_TYPE;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON;

@Service
public class ExternalPythonServerServiceImpl implements PythonRuntimeService {

    private static final Logger logger = LogManager.getLogger(ExternalPythonServerServiceImpl.class);
    private static final String EXTERNAL_PYTHON_PORT = System.getProperty("python.port", String.valueOf(8001));
    private static final String EXTERNAL_PYTHON_SERVER_URL = "https://localhost:" + EXTERNAL_PYTHON_PORT;
    private static final String EXTERNAL_PYTHON_SERVER_EVAL_PATH = "/rest/v1/eval";

    private final Semaphore executionControlSemaphore;

    private final Semaphore testingControlSemaphore;
    private final ResteasyClient restEasyClient;
    private final ObjectMapper objectMapper;

    @Resource(name = "externalPythonExecutionEngine")
    private ExternalPythonExecutionEngine externalPythonExecutionEngine;

    public ExternalPythonServerServiceImpl(StatefulRestEasyClientsHolder statefulRestEasyClient,
                                           Semaphore executionControlSemaphore,
                                           Semaphore testingControlSemaphore) {
        this.restEasyClient = statefulRestEasyClient.getRestEasyClient();
        JsonFactory factory = new JsonFactory();
        factory.enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES);
        factory.enable(JsonWriteFeature.ESCAPE_NON_ASCII.mappedFeature());
        this.objectMapper = new ObjectMapper(factory);
        this.executionControlSemaphore = executionControlSemaphore;
        this.testingControlSemaphore = testingControlSemaphore;
    }

    @Override
    public PythonEvaluationResult eval(String script, String prepareEnvironmentScript, Map<String, Serializable> vars) {
        try {
            return getPythonEvaluationResult(script, prepareEnvironmentScript, vars);
        } catch (JsonProcessingException ie) {
            logger.error(ie);
            throw new ExternalPythonScriptException("Execution was interrupted while waiting for a python permit.");
        }
    }

    @Override
    public PythonExecutionResult exec(Set<String> dependencies, String script, Map<String, Serializable> vars) {
        try {
            if (executionControlSemaphore.tryAcquire(1L, TimeUnit.SECONDS)) {
                try {
                    return externalPythonExecutionEngine.exec(dependencies, script, vars);
                } finally {
                    executionControlSemaphore.release();
                }
            } else {
                logger.warn("Maximum number of python processes has been reached. Waiting for a python process to finish. " +
                        "You can configure the number of concurrent python executions by setting " +
                        "'python.concurrent.execution.permits' system property.");
                executionControlSemaphore.acquire();
                try {
                    logger.info("Acquired a permit for a new python process. Continuing with execution...");
                    return externalPythonExecutionEngine.exec(dependencies, script, vars);
                } finally {
                    executionControlSemaphore.release();
                }
            }
        } catch (InterruptedException ie) {
            throw new ExternalPythonScriptException("Execution was interrupted while waiting for a python permit.");
        }
    }

    @Override
    public PythonEvaluationResult test(String prepareEnvironmentScript, String script, Map<String, Serializable> vars, long timeout) {
        try {
            if (testingControlSemaphore.tryAcquire(1L, TimeUnit.SECONDS)) {
                try {
                    return externalPythonExecutionEngine.test(prepareEnvironmentScript, script, vars, timeout);
                } finally {
                    testingControlSemaphore.release();
                }
            } else {
                logger.warn("Maximum number of python processes has been reached. Waiting for a python process to finish. " +
                        "You can configure the number of concurrent python executions by setting " +
                        "'python.testing.concurrent.execution.permits' system property.");
                testingControlSemaphore.acquire();
                try {
                    logger.info("Acquired a permit for a new python process. Continuing with execution...");
                    return externalPythonExecutionEngine.test(prepareEnvironmentScript, script, vars, timeout);
                } finally {
                    testingControlSemaphore.release();
                }
            }
        } catch (InterruptedException ie) {
            throw new ExternalPythonScriptException("Execution was interrupted while waiting for a python permit.");
        }
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
        Response scriptResponse = restEasyClient
                .target(EXTERNAL_PYTHON_SERVER_URL)
                .path(EXTERNAL_PYTHON_SERVER_EVAL_PATH)
                .request()
                .accept(APPLICATION_JSON_TYPE)
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .header(AUTHORIZATION, getBasicAuthorizationHeaderValue())
                .build(method, entity(payload, APPLICATION_JSON_TYPE))
                .invoke();

        return objectMapper.readValue(scriptResponse.readEntity(String.class), EvaluationResults.class);
    }

    private Properties readFromPropertiesFiles() {
        String filename = "pythonServer.properties";
        Properties prop = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(filename)) {
            prop.load(input);
        } catch (IOException exception) {
            logger.error(String.format("Failed to read from file", exception));
            throw new RuntimeException(exception);
        }
        return prop;
    }

    private String getBasicAuthorizationHeaderValue() {
        Properties prop = readFromPropertiesFiles();
        String username = prop.getProperty("username");
        String password = prop.getProperty("password");
        String encodedAuth = getEncoder().encodeToString((username + ":" + password).getBytes(UTF_8));
        return "Basic " + encodedAuth;
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