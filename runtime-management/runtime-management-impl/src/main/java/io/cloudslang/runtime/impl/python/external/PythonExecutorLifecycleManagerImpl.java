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

import io.cloudslang.runtime.api.python.PythonExecutorConfigurationDataService;
import io.cloudslang.runtime.api.python.PythonExecutorLifecycleManager;
import io.cloudslang.runtime.api.python.entities.PythonExecutorDetails;
import io.cloudslang.runtime.api.python.enums.PythonStrategy;
import org.apache.commons.lang3.SystemUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ws.rs.core.Response;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static io.cloudslang.runtime.api.python.enums.PythonStrategy.PYTHON_EXECUTOR;
import static io.cloudslang.runtime.api.python.enums.PythonStrategy.getPythonStrategy;
import static java.io.File.separator;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static org.jboss.resteasy.util.HttpHeaderNames.AUTHORIZATION;
import static org.jboss.resteasy.util.HttpHeaderNames.CONTENT_TYPE;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON;

@Service("pythonExecutorLifecycleManager")
public class PythonExecutorLifecycleManagerImpl implements PythonExecutorLifecycleManager {
    private static final Logger logger = LogManager.getLogger(PythonExecutorLifecycleManagerImpl.class);
    private static final PythonStrategy PYTHON_EVALUATOR = getPythonStrategy(System.getProperty("python.expressionsEval"), PYTHON_EXECUTOR);
    private static final String OO_HOME_PATH = System.getProperty("oo.home");
    private static final String PYTHON_EXECUTOR_PATH = OO_HOME_PATH + separator + "python-executor";
    private static final String EXTERNAL_PYTHON_EXECUTOR_STOP_PATH = "/rest/v1/stop";
    private static final String EXTERNAL_PYTHON_EXECUTOR_HEALTH_PATH = "/rest/v1/health";
    private static ResteasyClient restEasyClient;
    private static String EXTERNAL_PYTHON_EXECUTOR_URL;
    private static String ENCODED_AUTH;
    private static Process pythonExecutorProcess;

    @Autowired
    private StatefulRestEasyClientsHolder statefulRestEasyClientsHolder;

    @Autowired
    @Qualifier("pythonExecutorConfigurationDataService")
    private PythonExecutorConfigurationDataService pythonExecutorConfigurationDataService;

    @PostConstruct
    void initPythonExecutorDetails() {
        PythonExecutorDetails pythonExecutorDetails = pythonExecutorConfigurationDataService.getPythonExecutorConfiguration();
        EXTERNAL_PYTHON_EXECUTOR_URL = pythonExecutorDetails.getUrl();
        ENCODED_AUTH = pythonExecutorDetails.getLifecycleEncodedAuth();
        restEasyClient = statefulRestEasyClientsHolder.getRestEasyClient();
        if (PYTHON_EVALUATOR.equals(PYTHON_EXECUTOR)) {
            start();
        }
    }

    @Override
    public void start() {
        logger.info("A request to start the Python Executor was sent");
        if (isAlive()) {
            logger.info("Python Executor is already running");
            return;
        }

        killPythonExecutorProcess();

        if (isWindows()) {
            startWindowsProcess();
        } else {
            startLinuxProcess();
        }

        waitToStart();
    }

    @Override
    public boolean isAlive() {
        Response response;
        try {
            response = restEasyClient
                    .target(EXTERNAL_PYTHON_EXECUTOR_URL)
                    .path(EXTERNAL_PYTHON_EXECUTOR_HEALTH_PATH)
                    .request()
                    .accept(APPLICATION_JSON_TYPE)
                    .header(CONTENT_TYPE, APPLICATION_JSON)
                    .build("GET")
                    .invoke();
        } catch (Exception e) {
            return false;
        }

        return response.getStatus() == 200;
    }

    @PreDestroy
    @Override
    public void stop() {
        logger.info("A request to stop the Python Executor was sent");
        if (!isAlive()) {
            logger.info("Python Executor was already stopped");
            return;
        }

        Response response = restEasyClient
                .target(EXTERNAL_PYTHON_EXECUTOR_URL)
                .path(EXTERNAL_PYTHON_EXECUTOR_STOP_PATH)
                .request()
                .accept(APPLICATION_JSON_TYPE)
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .header(AUTHORIZATION, ENCODED_AUTH)
                .build("POST")
                .invoke();

        int statusCode = response.getStatus();
        if (statusCode == 200) {
            waitToStop();
        } else {
            logger.error("Failed to stop the Python Executor, response: " + statusCode);
        }
    }

    private void startWindowsProcess() {
        startProcess("start-python-executor.bat");
    }

    private void startLinuxProcess() {
        startProcess("start-python-executor.sh");
    }

    private void startProcess(String startPythonExecutor) {
        ProcessBuilder pb = new ProcessBuilder(PYTHON_EXECUTOR_PATH + separator + startPythonExecutor);
        try {
            logger.info("Starting Python Executor on port: " + pythonExecutorConfigurationDataService.getPythonExecutorConfiguration().getPort());
            pythonExecutorProcess = pb.start();
        } catch (IOException e) {
            logger.error("Failed to start Python Executor", e);
        }
    }

    private boolean isWindows() {
        return SystemUtils.IS_OS_WINDOWS;
    }

    private void waitToStart() {
        int retries = 20;
        int tries = 0;

        logger.info("Waiting to start");

        while (tries < retries) {
            tries ++;
            if (isAlive()) {
                logger.info("Python Executor was successfully started");
                killPythonExecutorProcess();
                return;
            }
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                logger.error(e);
            }
        }

        logger.error("Python Executor didn't successfully start in the allocated time");
        killPythonExecutorProcess();
    }

    private void waitToStop() {
        int retries = 20;
        int tries = 0;

        logger.info("Waiting to stop");

        while (tries < retries) {
            tries ++;
            if (!isAlive()) {
                logger.info("Python Executor was successfully stopped");
                killPythonExecutorProcess();
                return;
            }
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                logger.error(e);
            }
        }

        logger.error("Python Executor didn't successfully stop in the allocated time");
        killPythonExecutorProcess();
    }

    private void killPythonExecutorProcess() {
        if (pythonExecutorProcess != null) {
            pythonExecutorProcess.destroy();
            pythonExecutorProcess = null;
        }
    }
}
