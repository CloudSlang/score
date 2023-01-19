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
package io.cloudslang.runtime.api.python.entities;

public class PythonExecutorDetails {

    private String port;
    private String url;
    private String runtimeEncodedAuth;
    private String lifecycleEncodedAuth;
    private String pythonExecutorPath;

    public PythonExecutorDetails() {
    }

    public PythonExecutorDetails(String port, String url, String runtimeEncodedAuth, String lifecycleEncodedAuth, String pythonExecutorPath) {
        this.port = port;
        this.url = url;
        this.runtimeEncodedAuth = runtimeEncodedAuth;
        this.lifecycleEncodedAuth = lifecycleEncodedAuth;
        this.pythonExecutorPath = pythonExecutorPath;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getRuntimeEncodedAuth() {
        return runtimeEncodedAuth;
    }

    public void setRuntimeEncodedAuth(String runtimeEncodedAuth) {
        this.runtimeEncodedAuth = runtimeEncodedAuth;
    }

    public String getLifecycleEncodedAuth() {
        return lifecycleEncodedAuth;
    }

    public void setLifecycleEncodedAuth(String lifecycleEncodedAuth) {
        this.lifecycleEncodedAuth = lifecycleEncodedAuth;
    }

    public String getPythonExecutorPath() {
        return pythonExecutorPath;
    }

    public void setPythonExecutorPath(String pythonExecutorPath) {
        this.pythonExecutorPath = pythonExecutorPath;
    }
}
