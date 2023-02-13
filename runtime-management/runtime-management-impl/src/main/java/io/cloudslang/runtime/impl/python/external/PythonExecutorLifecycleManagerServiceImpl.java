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
import io.cloudslang.runtime.api.python.PythonExecutorLifecycleManagerService;
import io.cloudslang.runtime.api.python.entities.PythonExecutorDetails;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static io.cloudslang.runtime.api.python.enums.PythonStrategy.PYTHON_EXECUTOR;
import static io.cloudslang.runtime.api.python.enums.PythonStrategy.getPythonStrategy;
import static java.io.File.separator;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static org.apache.commons.lang.StringUtils.containsIgnoreCase;
import static org.jboss.resteasy.util.HttpHeaderNames.AUTHORIZATION;
import static org.jboss.resteasy.util.HttpHeaderNames.CONTENT_TYPE;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON;

@Service("pythonExecutorLifecycleManagerService")
public class PythonExecutorLifecycleManagerServiceImpl implements PythonExecutorLifecycleManagerService {

    private static final Logger logger = LogManager.getLogger(PythonExecutorLifecycleManagerServiceImpl.class);
    private static final boolean IS_PYTHON_EXECUTOR_EVAL = getPythonStrategy(System.getProperty("python.expressionsEval"), PYTHON_EXECUTOR).equals(PYTHON_EXECUTOR);
    private static final String EXTERNAL_PYTHON_EXECUTOR_STOP_PATH = "/rest/v1/stop";
    private static final String EXTERNAL_PYTHON_EXECUTOR_HEALTH_PATH = "/rest/v1/health";
    private static final int START_STOP_RETRIES_COUNT = 20;
    private static final int PYTHON_EXECUTOR_KEEP_ALIVE_INTERVAL = 30000;
    private static ThreadPoolTaskScheduler threadPoolTaskScheduler;
    private static Process pythonExecutorProcess;
    private final ResteasyClient restEasyClient;
    private final PythonExecutorConfigurationDataService pythonExecutorConfigurationDataService;

    @Autowired
    public PythonExecutorLifecycleManagerServiceImpl(StatefulRestEasyClientsHolder statefulRestEasyClientsHolder,
                                                     @Qualifier("pythonExecutorConfigurationDataService") PythonExecutorConfigurationDataService pythonExecutorConfigurationDataService) {
        this.restEasyClient = statefulRestEasyClientsHolder.getRestEasyClient();
        this.pythonExecutorConfigurationDataService = pythonExecutorConfigurationDataService;
        if (IS_PYTHON_EXECUTOR_EVAL) {
            createKeepAliveJob();
            doStartPythonExecutor();
        }
    }

    @PreDestroy
    public void destroy() {
        if (threadPoolTaskScheduler != null) {
            threadPoolTaskScheduler.destroy();
        }
        doStopPythonExecutor();
    }

    @Override
    public void start() {
        doStartPythonExecutor();
    }

    @Override
    public boolean isAlive() {
        return isAlivePythonExecutor();
    }

    @Override
    public void stop() {
        doStopPythonExecutor();
    }

    private boolean isAlivePythonExecutor() {
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

    private void doStopPythonExecutor() {
        if (!IS_PYTHON_EXECUTOR_EVAL) {
            return;
        }
        logger.info("A request to stop the Python Executor was sent");
        if (!isAlivePythonExecutor()) {
            logger.info("Python Executor was already stopped");
            return;
        }

        PythonExecutorDetails pythonExecutorConfiguration = pythonExecutorConfigurationDataService.getPythonExecutorConfiguration();
        try (Response response = restEasyClient
                .target(pythonExecutorConfiguration.getUrl())
                .path(EXTERNAL_PYTHON_EXECUTOR_STOP_PATH)
                .request()
                .accept(APPLICATION_JSON_TYPE)
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .header(AUTHORIZATION, pythonExecutorConfiguration.getLifecycleEncodedAuth())
                .build("POST")
                .invoke()) {

            if (response.getStatus() == 200) {
                waitToStop();
            }
        } catch (ProcessingException processingEx) {
            // Might not get a response if server gets shutdown immediately
            if (containsIgnoreCase(processingEx.getMessage(), "RESTEASY004655: Unable to invoke request")) {
                waitToStop();
            }
        }
    }

    private void doStartPythonExecutor() {
        if (!IS_PYTHON_EXECUTOR_EVAL) {
            return;
        }
        logger.info("A request to start the Python Executor was sent");
        if (isAlivePythonExecutor()) {
            logger.info("Python Executor is already running");
            return;
        }

        destroyPythonExecutorProcess();

        if (isWindows()) {
            startWindowsProcess();
        } else {
            startLinuxProcess();
        }

        waitToStart();
    }

    private void startWindowsProcess() {
        startProcess("start-python-executor.bat");
    }

    private void startLinuxProcess() {
        startProcess("start-python-executor.sh");
    }

    private void startProcess(String startPythonExecutor) {
        PythonExecutorDetails pythonExecutorConfiguration = pythonExecutorConfigurationDataService.getPythonExecutorConfiguration();
        ProcessBuilder pb = new ProcessBuilder(
                pythonExecutorConfiguration.getSourceLocation() +
                        separator +
                        "bin" +
                        separator +
                        startPythonExecutor,
                pythonExecutorConfiguration.getPort());
        pb.directory(FileUtils.getFile(pythonExecutorConfiguration.getSourceLocation() + separator + "bin"));
        try {
            logger.info("Starting Python Executor on port: " + pythonExecutorConfiguration.getPort());
            pythonExecutorProcess = pb.start();
        } catch (IOException ioException) {
            logger.error("Failed to start Python Executor", ioException);
        } catch (Exception exception) {
            logger.error("An error occurred while trying to start the Python Executor", exception);
        }
    }

    private void waitToStart() {
        logger.info("Waiting to start");

        for (int tries = 0; tries < START_STOP_RETRIES_COUNT; tries++) {
            if (isAlivePythonExecutor()) {
                logger.info("Python Executor was successfully started");
                return;
            }
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                logger.warn("Interrupted while waiting for Python Executor to start");
            }
        }

        logger.error("Python executor did not start successfully within the allocated time");
        destroyPythonExecutorProcess();
    }

    private void waitToStop() {
        logger.info("Waiting to stop");

        for (int tries = 0; tries < START_STOP_RETRIES_COUNT; tries++) {
            if (!isAlivePythonExecutor()) {
                logger.info("Python Executor was successfully stopped");
                destroyPythonExecutorProcess();
                return;
            }
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                logger.warn("Interrupted while waiting for Python Executor to stop");
            }
        }

        logger.error("Python executor did not stop successfully within the allocated time");
        destroyPythonExecutorProcess();
    }

    private void pythonExecutorKeepAlive() {
        if (isAlivePythonExecutor()) {
            return;
        }

        doStartPythonExecutor();
    }

    private void createKeepAliveJob() {
        threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
        threadPoolTaskScheduler.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        threadPoolTaskScheduler.setDaemon(true);
        threadPoolTaskScheduler.setThreadNamePrefix("pythonExecutor-");
        threadPoolTaskScheduler.afterPropertiesSet();

        PeriodicTrigger periodicTrigger = new PeriodicTrigger(PYTHON_EXECUTOR_KEEP_ALIVE_INTERVAL);
        periodicTrigger.setFixedRate(true);
        periodicTrigger.setInitialDelay(PYTHON_EXECUTOR_KEEP_ALIVE_INTERVAL);

        threadPoolTaskScheduler.schedule(this::pythonExecutorKeepAlive, periodicTrigger);
    }

    private void destroyPythonExecutorProcess() {
        if (pythonExecutorProcess != null) {
            pythonExecutorProcess.destroy();
            pythonExecutorProcess = null;
        }
    }

    private boolean isWindows() {
        return SystemUtils.IS_OS_WINDOWS;
    }
}
