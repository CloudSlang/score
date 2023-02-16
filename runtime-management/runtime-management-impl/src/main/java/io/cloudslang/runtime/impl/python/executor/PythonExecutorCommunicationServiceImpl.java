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
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;

import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static org.jboss.resteasy.util.HttpHeaderNames.AUTHORIZATION;
import static org.jboss.resteasy.util.HttpHeaderNames.CONTENT_TYPE;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON;

@Service("pythonExecutorCommunicationService")
public class PythonExecutorCommunicationServiceImpl implements PythonExecutorCommunicationService {

    private final ResteasyClient restEasyClient;
    private final PythonExecutorConfigurationDataService pythonExecutorConfigurationDataService;

    @Autowired
    public PythonExecutorCommunicationServiceImpl(StatefulRestEasyClientsHolder statefulRestEasyClientsHolder,
                                                  @Qualifier("pythonExecutorConfigurationDataService") PythonExecutorConfigurationDataService pythonExecutorConfigurationDataService) {
        this.restEasyClient = statefulRestEasyClientsHolder.getRestEasyClient();
        this.pythonExecutorConfigurationDataService = pythonExecutorConfigurationDataService;
    }

    public Pair<Integer, String> performNoAuthRequest(String path,
                                                      String method,
                                                      String requestPayload) {
        PythonExecutorDetails pythonExecutorDetails = pythonExecutorConfigurationDataService.getPythonExecutorConfiguration();
        return executeRequest(
                pythonExecutorDetails.getUrl(),
                path,
                method,
                requestPayload,
                null
        );
    }

    public Pair<Integer, String> performRuntimeRequest(String path,
                                                       String method,
                                                       String requestPayload) {
        PythonExecutorDetails pythonExecutorDetails = pythonExecutorConfigurationDataService.getPythonExecutorConfiguration();
        return executeRequest(
                pythonExecutorDetails.getUrl(),
                path,
                method,
                requestPayload,
                pythonExecutorDetails.getRuntimeEncodedAuth()
        );
    }

    public Pair<Integer, String> performLifecycleRequest(String path,
                                                         String method,
                                                         String requestPayload) {
        PythonExecutorDetails pythonExecutorDetails = pythonExecutorConfigurationDataService.getPythonExecutorConfiguration();
        return executeRequest(
                pythonExecutorDetails.getUrl(),
                path,
                method,
                requestPayload,
                pythonExecutorDetails.getLifecycleEncodedAuth()
        );
    }

    private Pair<Integer, String> executeRequest(String url,
                                                 String path,
                                                 String method,
                                                 @Nullable String requestPayload,
                                                 String auth) {

        Invocation request = null;
        if (auth != null) {
            request = buildRequest(url, path, method, requestPayload, auth);
        } else {
            request = buildRequest(url, path, method, requestPayload);
        }

        try (Response response = request.invoke()) {
            return ImmutablePair.of(
                    response.getStatus(),
                    response.readEntity(String.class)
            );
        }
    }

    private Invocation buildRequest(String url,
                                    String path,
                                    String method,
                                    @Nullable String requestPayload,
                                    String auth) {

        Invocation.Builder request = restEasyClient
                .target(url)
                .path(path)
                .request()
                .accept(APPLICATION_JSON_TYPE)
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .header(AUTHORIZATION, auth);

        Entity<String> entity = (requestPayload == null
                ? null : entity(requestPayload, APPLICATION_JSON_TYPE));

        return request.build(method, entity);
    }

    private Invocation buildRequest(String url,
                                    String path,
                                    String method,
                                    @Nullable String requestPayload) {

        Invocation.Builder request = restEasyClient
                .target(url)
                .path(path)
                .request()
                .accept(APPLICATION_JSON_TYPE)
                .header(CONTENT_TYPE, APPLICATION_JSON);

        Entity<String> entity = (requestPayload == null
                ? null : entity(requestPayload, APPLICATION_JSON_TYPE));

        return request.build(method, entity);
    }


}
