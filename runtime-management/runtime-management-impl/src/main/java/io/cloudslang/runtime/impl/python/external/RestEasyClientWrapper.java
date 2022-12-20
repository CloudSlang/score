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

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.conn.ClientConnectionManager;
import org.jboss.resteasy.client.jaxrs.ClientHttpEngine;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient43Engine;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

@Component("restEasyClientWrapper")
@SuppressWarnings("deprecation")
public class RestEasyClientWrapper {

    public Pair<ResteasyClient, ClientConnectionManager> getClientForRemoteConfiguration(int maxConnections, int maxPerRoute)
            throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {

        ResteasyClientBuilder resteasyClientBuilder =
                (ResteasyClientBuilder) ResteasyClientBuilder.newBuilder();

        resteasyClientBuilder
                .maxPooledPerRoute(maxPerRoute)
                .connectionPoolSize(maxConnections);

        SSLContext sslContext = SSLContext.getDefault();

        resteasyClientBuilder.sslContext(sslContext);

        ResteasyClient restEasyClient = resteasyClientBuilder.build();
        ClientHttpEngine clientHttpEngine = restEasyClient.httpEngine();
        if (!(clientHttpEngine instanceof ApacheHttpClient43Engine)) {
            throw new IllegalStateException("Rest easy does not use apache http client backend");
        }
        ApacheHttpClient43Engine apacheHttpClient43Engine = (ApacheHttpClient43Engine) clientHttpEngine;
        ClientConnectionManager connectionManager = apacheHttpClient43Engine.getHttpClient().getConnectionManager();

        return ImmutablePair.of(restEasyClient, connectionManager);
    }
}