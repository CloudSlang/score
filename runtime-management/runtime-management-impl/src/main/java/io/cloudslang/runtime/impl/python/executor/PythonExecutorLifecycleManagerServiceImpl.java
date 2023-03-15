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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.cloudslang.runtime.api.python.PythonExecutorCommunicationService;
import io.cloudslang.runtime.api.python.PythonExecutorConfigurationDataService;
import io.cloudslang.runtime.api.python.PythonExecutorLifecycleManagerService;
import io.cloudslang.runtime.api.python.entities.PythonExecutorDetails;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import javax.ws.rs.ProcessingException;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static io.cloudslang.runtime.api.python.enums.PythonStrategy.PYTHON_EXECUTOR;
import static io.cloudslang.runtime.api.python.enums.PythonStrategy.getPythonStrategy;
import static java.io.File.separator;
import static java.lang.Long.getLong;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.apache.commons.lang.StringUtils.containsIgnoreCase;

@Service("pythonExecutorLifecycleManagerService")
public class PythonExecutorLifecycleManagerServiceImpl implements PythonExecutorLifecycleManagerService {

    private static final Logger logger = LogManager.getLogger(PythonExecutorLifecycleManagerServiceImpl.class);
    private static final boolean IS_PYTHON_EXECUTOR_EVAL = getPythonStrategy(System.getProperty("python.expressionsEval"), PYTHON_EXECUTOR).equals(PYTHON_EXECUTOR);
    private static final String EXTERNAL_PYTHON_EXECUTOR_STOP_PATH = "/rest/v1/stop";
    private static final String EXTERNAL_PYTHON_EXECUTOR_HEALTH_PATH = "/rest/v1/health";
    private static final int START_STOP_RETRIES_COUNT = 20;
    private static final int PYTHON_EXECUTOR_INITIAL_DELAY = 30000;
    private static final long PYTHON_EXECUTOR_KEEP_ALIVE_INTERVAL = getLong("python.executor.keepAliveDelayMillis", 30000);

    private static boolean isAlivePythonExecutorValue = false;
    private static ScheduledThreadPoolExecutor scheduledExecutor;
    private static Process pythonExecutorProcess;
    private final PythonExecutorCommunicationService pythonExecutorCommunicationService;
    private final PythonExecutorConfigurationDataService pythonExecutorConfigurationDataService;


    @Autowired
    public PythonExecutorLifecycleManagerServiceImpl(PythonExecutorCommunicationService pythonExecutorCommunicationService,
                                                     PythonExecutorConfigurationDataService pythonExecutorConfigurationDataService) {
        this.pythonExecutorCommunicationService = pythonExecutorCommunicationService;
        this.pythonExecutorConfigurationDataService = pythonExecutorConfigurationDataService;
        if (IS_PYTHON_EXECUTOR_EVAL) {
            createKeepAliveJob();
            doStartPythonExecutor();
        }
    }

    @PreDestroy
    public void destroy() {
        if (IS_PYTHON_EXECUTOR_EVAL) {
            try {
                scheduledExecutor.shutdown();
                scheduledExecutor.shutdownNow();
            } catch (Exception failedShutdownEx) {
                logger.error("Could not shutdown executor: ", failedShutdownEx);
            } finally {
                doStopPythonExecutor();
            }
        }
    }

    @Override
    public void start() {
        doStartPythonExecutor();
    }

    @Override
    public boolean isAlive() {
        return isAlivePythonExecutorValue;
    }

    @Override
    public void stop() {
        doStopPythonExecutor();
    }

    private boolean isAlivePythonExecutor() {
        try {
            Pair<Integer, String> response = pythonExecutorCommunicationService.performNoAuthRequest(EXTERNAL_PYTHON_EXECUTOR_HEALTH_PATH, "GET", null);
            if (response.getLeft() == 200 && pythonExecutorProcess == null) {
                logger.warn("Python Executor port is already in use");
                isAlivePythonExecutorValue = false;
                return true;
            }
            return response.getLeft() == 200;
        } catch (Exception e) {
            isAlivePythonExecutorValue = false;
            if (containsIgnoreCase(e.getMessage(), "signature check failed")) {
                logger.warn("Python Executor port is already in use");
                return true;
            }
            return false;
        }
    }

    private void doStopPythonExecutor() {
        if (!IS_PYTHON_EXECUTOR_EVAL) {
            return;
        }
        logger.info("A request to stop the Python Executor was sent");
        if (!isAlivePythonExecutor() || !isAlivePythonExecutorValue) {
            logger.info("Python Executor was already stopped");
            return;
        }

        try {
            if (pythonExecutorCommunicationService.performLifecycleRequest(
                    EXTERNAL_PYTHON_EXECUTOR_STOP_PATH, "POST", null).getLeft() == 200) {
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
        if (isPythonInstalledOnSamePort()){
            return;
        }
        logger.info("A request to start the Python Executor was sent");
        if (isAlivePythonExecutor()) {
            // Do not attempt to start because the python executor is running under other process
            if (isAlivePythonExecutorValue) {
                logger.info("Python Executor is already running");
            }
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
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(pythonExecutorProcess.getOutputStream()));

            writer.write(pythonExecutorConfiguration.getEncodedSecretKeyPath());
            writer.flush();
            writer.close();
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
                isAlivePythonExecutorValue = true;
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
                isAlivePythonExecutorValue = false;
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

    private void destroyPythonExecutorProcess() {
        if (pythonExecutorProcess != null) {
            pythonExecutorProcess.destroy();
            pythonExecutorProcess = null;
            isAlivePythonExecutorValue = false;
        }
    }

    private boolean isWindows() {
        return SystemUtils.IS_OS_WINDOWS;
    }

    private void createKeepAliveJob() {
        scheduledExecutor = getScheduledExecutor();
        scheduledExecutor.scheduleWithFixedDelay(this::pythonExecutorKeepAlive,
                PYTHON_EXECUTOR_INITIAL_DELAY, PYTHON_EXECUTOR_KEEP_ALIVE_INTERVAL, MILLISECONDS);
    }

    private ScheduledThreadPoolExecutor getScheduledExecutor() {
        final ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNameFormat("python-executor-keepalive-%d")
                .setDaemon(true)
                .build();

        // Intentionally 1 thread
        ScheduledThreadPoolExecutor scheduledExecutor = new ScheduledThreadPoolExecutor(1, threadFactory);
        scheduledExecutor.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
        scheduledExecutor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        scheduledExecutor.setRemoveOnCancelPolicy(true);
        scheduledExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());

        return scheduledExecutor;
    }

    private boolean isPythonInstalledOnSamePort (){
        PythonExecutorDetails pythonExecutorConfiguration = pythonExecutorConfigurationDataService.getPythonExecutorConfiguration();
        String pythonExecutorPort = pythonExecutorConfiguration.getPort();
        String mgmtUrl = System.getProperty("mgmt.url");
        int port = 0;
        try {
            URL url = new URL(mgmtUrl);
            port = url.getPort();
        } catch (MalformedURLException e) {
            logger.error(e);
        }
        return pythonExecutorPort.equals(String.valueOf(port));
    }
}
