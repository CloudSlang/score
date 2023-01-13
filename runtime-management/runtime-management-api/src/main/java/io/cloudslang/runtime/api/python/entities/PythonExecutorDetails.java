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
    private String runtimeUsername;
    private String runtimePassword;
    private String lifecycleUsername;
    private String lifecyclePassword;

    public PythonExecutorDetails() {
    }

    public PythonExecutorDetails(String port, String runtimeUsername, String runtimePassword, String lifecycleUsername, String lifecyclePassword) {
        this.port = port;
        this.runtimeUsername = runtimeUsername;
        this.runtimePassword = runtimePassword;
        this.lifecycleUsername = lifecycleUsername;
        this.lifecyclePassword = lifecyclePassword;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getRuntimeUsername() {
        return runtimeUsername;
    }

    public void setRuntimeUsername(String runtimeUsername) {
        this.runtimeUsername = runtimeUsername;
    }

    public String getRuntimePassword() {
        return runtimePassword;
    }

    public void setRuntimePassword(String runtimePassword) {
        this.runtimePassword = runtimePassword;
    }

    public String getLifecycleUsername() {
        return lifecycleUsername;
    }

    public void setLifecycleUsername(String lifecycleUsername) {
        this.lifecycleUsername = lifecycleUsername;
    }

    public String getLifecyclePassword() {
        return lifecyclePassword;
    }

    public void setLifecyclePassword(String lifecyclePassword) {
        this.lifecyclePassword = lifecyclePassword;
    }
}
