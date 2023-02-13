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

import io.cloudslang.runtime.api.python.PythonExecutorCommunicationService;
import io.cloudslang.runtime.api.python.PythonExecutorConfigurationDataService;
import io.cloudslang.runtime.api.python.entities.PythonExecutorDetails;
import io.cloudslang.runtime.impl.python.external.StatefulRestEasyClientsHolder;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.Response;

import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static org.jboss.resteasy.util.HttpHeaderNames.AUTHORIZATION;
import static org.jboss.resteasy.util.HttpHeaderNames.CONTENT_TYPE;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON;

@Service("pythonExecutorCommunicationServiceImpl")
public class PythonExecutorCommunicationServiceImpl implements PythonExecutorCommunicationService {

    private static final String EXTERNAL_PYTHON_EXECUTOR_STOP_PATH = "/rest/v1/stop";
    private static final String EXTERNAL_PYTHON_EXECUTOR_HEALTH_PATH = "/rest/v1/health";
    private static final String EXTERNAL_PYTHON_EXECUTOR_EVAL_PATH = "/rest/v1/eval";
//    private static String EXTERNAL_PYTHON_EXECUTOR_URL;
//    private static String ENCODED_AUTH;

    private final ResteasyClient restEasyClient;

    private final PythonExecutorConfigurationDataService pythonExecutorConfigurationDataService;

    @Autowired
    public PythonExecutorCommunicationServiceImpl(StatefulRestEasyClientsHolder statefulRestEasyClientsHolder,
                                                  @Qualifier("pythonExecutorConfigurationDataService") PythonExecutorConfigurationDataService pythonExecutorConfigurationDataService) {
        this.restEasyClient = statefulRestEasyClientsHolder.getRestEasyClient();
        this.pythonExecutorConfigurationDataService = pythonExecutorConfigurationDataService;
    }

    @Override
    public boolean isAlivePythonExecutor() {
        try (Response response = restEasyClient
                .target(pythonExecutorConfigurationDataService.getPythonExecutorConfiguration().getUrl())
                .path(EXTERNAL_PYTHON_EXECUTOR_HEALTH_PATH)
                .request()
                .accept(APPLICATION_JSON_TYPE)
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .build("GET")
                .invoke()) {
            return response.getStatus() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Response stopPythonExecutor() {
        PythonExecutorDetails pythonExecutorConfiguration = pythonExecutorConfigurationDataService.getPythonExecutorConfiguration();
        return restEasyClient
                .target(pythonExecutorConfiguration.getUrl())
                .path(EXTERNAL_PYTHON_EXECUTOR_STOP_PATH)
                .request()
                .accept(APPLICATION_JSON_TYPE)
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .header(AUTHORIZATION, pythonExecutorConfiguration.getLifecycleEncodedAuth())
                .build("POST")
                .invoke();
    }

    @Override
    public Response executeRequestOnPythonServer(String method, String payload) {
        return restEasyClient
                .target(getPythonExecutorConfiguration().getUrl())
                .path(EXTERNAL_PYTHON_EXECUTOR_EVAL_PATH)
                .request()
                .accept(APPLICATION_JSON_TYPE)
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .header(AUTHORIZATION, getPythonExecutorConfiguration().getRuntimeEncodedAuth())
                .build(method, entity(payload, APPLICATION_JSON_TYPE))
                .invoke();
    }

    @Override
    public PythonExecutorDetails getPythonExecutorConfiguration() {
        return pythonExecutorConfigurationDataService.getPythonExecutorConfiguration();
    }
}
