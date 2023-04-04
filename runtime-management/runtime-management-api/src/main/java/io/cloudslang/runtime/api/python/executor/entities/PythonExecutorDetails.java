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
package io.cloudslang.runtime.api.python.executor.entities;

public class PythonExecutorDetails {

    private final String port;
    private final String url;
    private final String runtimeEncodedAuth;
    private final String lifecycleEncodedAuth;
    private final String sourceLocation;
    private final String encodedSecretKeyPath;

    public PythonExecutorDetails() {
        this.port = null;
        this.url = null;
        this.runtimeEncodedAuth = null;
        this.lifecycleEncodedAuth = null;
        this.sourceLocation = null;
        this.encodedSecretKeyPath = null;
    }

    public PythonExecutorDetails(String port, String url, String runtimeEncodedAuth, String lifecycleEncodedAuth, String sourceLocation, String encodedSecretKeyPath) {
        this.port = port;
        this.url = url;
        this.runtimeEncodedAuth = runtimeEncodedAuth;
        this.lifecycleEncodedAuth = lifecycleEncodedAuth;
        this.sourceLocation = sourceLocation;
        this.encodedSecretKeyPath = encodedSecretKeyPath;
    }

    public String getPort() {
        return port;
    }

    public String getUrl() {
        return url;
    }

    public String getRuntimeEncodedAuth() {
        return runtimeEncodedAuth;
    }

    public String getLifecycleEncodedAuth() {
        return lifecycleEncodedAuth;
    }

    public String getSourceLocation() {
        return sourceLocation;
    }

    public String getEncodedSecretKeyPath() {
        return encodedSecretKeyPath;
    }
}
