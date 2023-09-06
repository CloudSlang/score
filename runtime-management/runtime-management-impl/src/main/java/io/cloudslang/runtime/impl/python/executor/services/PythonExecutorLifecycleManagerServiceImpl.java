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
package io.cloudslang.runtime.impl.python.executor.services;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.cloudslang.runtime.api.python.executor.entities.PythonExecutorProcessDetails;
import io.cloudslang.runtime.api.python.executor.services.PythonExecutorCommunicationService;
import io.cloudslang.runtime.api.python.executor.services.PythonExecutorConfigurationDataService;
import io.cloudslang.runtime.api.python.executor.services.PythonExecutorLifecycleManagerService;
import io.cloudslang.runtime.api.python.executor.entities.PythonExecutorDetails;
import io.cloudslang.runtime.api.python.executor.services.PythonExecutorProcessManagerService;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static io.cloudslang.runtime.api.python.enums.PythonStrategy.PYTHON_EXECUTOR;
import static io.cloudslang.runtime.api.python.enums.PythonStrategy.getPythonStrategy;
import static java.lang.Integer.getInteger;
import static java.lang.Long.getLong;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;

public class PythonExecutorLifecycleManagerServiceImpl implements PythonExecutorLifecycleManagerService {

    private static final Logger logger = LogManager.getLogger(PythonExecutorLifecycleManagerServiceImpl.class);
    private static final boolean IS_PYTHON_EXECUTOR_EVAL = getPythonStrategy(System.getProperty("python.expressionsEval"), PYTHON_EXECUTOR) == PYTHON_EXECUTOR;
    private static final String EXTERNAL_PYTHON_EXECUTOR_STOP_PATH = "/rest/v1/stop";
    private static final String EXTERNAL_PYTHON_EXECUTOR_HEALTH_PATH = "/rest/v1/health";
    private static final int START_STOP_RETRIES_COUNT = getInteger("python.executor.startStopRetriesCount", 20);
    private static final long PYTHON_EXECUTOR_INITIAL_DELAY = 30_000L;
    private static final long PYTHON_EXECUTOR_KEEP_ALIVE_INTERVAL = getLong("python.executor.keepAliveDelayMillis", 30_000L);
    private static final int PYTHON_EXECUTOR_KEEP_ALIVE_RETRIES_COUNT = getInteger("python.executor.keepAliveRetriesCount", 50);

    private final PythonExecutorCommunicationService pythonExecutorCommunicationService;
    private final PythonExecutorConfigurationDataService pythonExecutorConfigurationDataService;
    private final PythonExecutorProcessManagerService pythonExecutorProcessManagerService;
    private ScheduledThreadPoolExecutor scheduledExecutor;

    // This state is intentional
    private final AtomicInteger currentKeepAliveRetriesCount;
    private final AtomicBoolean pythonExecutorRunning;
    private final PythonExecutorProcessDetails pythonExecutorProcessDetails;
    private final AtomicReference<Process> pythonExecutorProcess;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    public PythonExecutorLifecycleManagerServiceImpl(PythonExecutorCommunicationService pythonExecutorCommunicationService,
                                                     PythonExecutorConfigurationDataService pythonExecutorConfigurationDataService,
                                                     PythonExecutorProcessManagerService pythonExecutorProcessManagerService) {
        this.pythonExecutorCommunicationService = pythonExecutorCommunicationService;
        this.pythonExecutorConfigurationDataService = pythonExecutorConfigurationDataService;
        this.pythonExecutorProcessManagerService = pythonExecutorProcessManagerService;
        this.currentKeepAliveRetriesCount = new AtomicInteger(0);
        this.pythonExecutorRunning = new AtomicBoolean(false);
        this.pythonExecutorProcessDetails = new PythonExecutorProcessDetails();
        this.pythonExecutorProcess = new AtomicReference<>(null);
    }

    @PostConstruct
    public void init() {
        if (IS_PYTHON_EXECUTOR_EVAL) {
            boolean shouldCreateKeepAlive = false;
            try {
                // on startUp find if there is any leftover process alive
                pythonExecutorProcessManagerService.updatePythonExecutorProcessDetails(pythonExecutorProcessDetails);
                shouldCreateKeepAlive = doStartPythonExecutor();
            } finally {
                if (shouldCreateKeepAlive) {
                    createKeepAliveJob();
                }
            }
        }
    }

    @PreDestroy
    public void destroy() {
        if (IS_PYTHON_EXECUTOR_EVAL) {
            stopKeepAliveJob();
            doStopPythonExecutor();
        }
    }

    @Override
    public boolean isAlive() {
        return pythonExecutorRunning.get();
    }

    @Override
    public void stop() {
        stopKeepAliveJob();
        doStopPythonExecutor();
    }

    private PythonExecutorStatus getPythonExecutorStatus() {
        try {
            Pair<Integer, String> response = pythonExecutorCommunicationService.performNoAuthRequest(EXTERNAL_PYTHON_EXECUTOR_HEALTH_PATH, "GET", null);
            if (response.getLeft() == 200) {
                if (pythonExecutorProcess.get() == null) {
                    logger.warn("Python Executor port is already in use");
                    pythonExecutorRunning.set(false);
                    return PythonExecutorStatus.BLOCKED;
                }
                currentKeepAliveRetriesCount.set(0);
                return PythonExecutorStatus.UP;
            }
            pythonExecutorRunning.set(false);
            return PythonExecutorStatus.DOWN;
        } catch (IllegalArgumentException e) {
            logger.error(e);
            pythonExecutorRunning.set(false);
            return PythonExecutorStatus.DOWN;
        } catch (Exception e) {
            pythonExecutorRunning.set(false);
            if (containsIgnoreCase(e.getMessage(), "signature check failed")) {
                logger.warn("Python Executor port is already in use");
                return PythonExecutorStatus.BLOCKED;
            }
            return PythonExecutorStatus.DOWN;
        }
    }

    private void doStopPythonExecutor() {
        if (!IS_PYTHON_EXECUTOR_EVAL) {
            return;
        }
        logger.info("A request to stop the Python Executor was sent");
        if (getPythonExecutorStatus() != PythonExecutorStatus.UP || !pythonExecutorRunning.get()
                && (pythonExecutorProcessDetails.getPythonExecutorParentPid() == null
                && pythonExecutorProcessDetails.getPythonExecutorChildrenPid() == null)) {
            logger.info("Python Executor was already stopped");
            return;
        }

        pythonExecutorProcessManagerService.stopPythonExecutorProcess(pythonExecutorProcessDetails);

        try {
            pythonExecutorCommunicationService.performLifecycleRequest(
                    EXTERNAL_PYTHON_EXECUTOR_STOP_PATH, "POST", null);
        } catch (Exception ignore) {}

        waitToStop();
    }

    private synchronized boolean doStartPythonExecutor() {
        if (!IS_PYTHON_EXECUTOR_EVAL) {
            return false;
        }

        if (isPythonInstalledOnSamePort()) {
            return false;
        }
        logger.info("A request to start the Python Executor was sent");
        if (getPythonExecutorStatus() == PythonExecutorStatus.UP) {
            // Do not attempt to start because the python executor is running under other process
            if (pythonExecutorRunning.get()) {
                logger.info("Python Executor is already running");
            }
            return false;
        }

        destroyPythonExecutorProcess();
        pythonExecutorProcess.set(pythonExecutorProcessManagerService.startPythonExecutorProcess());
        boolean hasPythonProcessStarted = pythonExecutorProcess.get() != null;
        if (hasPythonProcessStarted) {
            waitToStart();
        }

        return true;
    }

    private void waitToStart() {
        logger.info("Waiting to start");

        for (int tries = 0; tries < START_STOP_RETRIES_COUNT; tries++) {
            if (getPythonExecutorStatus() == PythonExecutorStatus.UP) {
                pythonExecutorRunning.set(true);
                currentKeepAliveRetriesCount.set(0);
                pythonExecutorProcessManagerService.updatePythonExecutorProcessDetails(pythonExecutorProcessDetails);
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
            if (getPythonExecutorStatus() != PythonExecutorStatus.UP &&
                    pythonExecutorProcessManagerService.stopPythonExecutorProcess(pythonExecutorProcessDetails)) {
                logger.info("Python Executor was successfully stopped");
                pythonExecutorRunning.set(false);
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

    // This method is called from the Scheduled executor repeatedly
    private void pythonExecutorKeepAlive() {
        if (currentKeepAliveRetriesCount.getAndIncrement() >= PYTHON_EXECUTOR_KEEP_ALIVE_RETRIES_COUNT) {
            stopKeepAliveJob();
            logger.info("Python executor did not start in " + (currentKeepAliveRetriesCount.get()-1) + " retries and stopped trying");
            return;
        }

        if (getPythonExecutorStatus() == PythonExecutorStatus.UP) {
            return;
        }

        doStartPythonExecutor();
    }

    private void destroyPythonExecutorProcess() {
        pythonExecutorProcessManagerService.stopPythonExecutorProcess(pythonExecutorProcessDetails);
        if (pythonExecutorProcess.get() != null) {
            pythonExecutorProcess.get().destroy();
            pythonExecutorProcess.set(null);
            pythonExecutorRunning.set(false);
        }
    }

    private void createKeepAliveJob() {
        scheduledExecutor = getScheduledExecutor();
        scheduledExecutor.scheduleWithFixedDelay(this::pythonExecutorKeepAlive,
                PYTHON_EXECUTOR_INITIAL_DELAY, PYTHON_EXECUTOR_KEEP_ALIVE_INTERVAL, MILLISECONDS);
    }

    private void stopKeepAliveJob() {
        try {
            scheduledExecutor.shutdown();
            scheduledExecutor.shutdownNow();
        } catch (Exception failedShutdownEx) {
            logger.error("Could not shutdown executor: ", failedShutdownEx);
        }
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

    private boolean isPythonInstalledOnSamePort() {
        PythonExecutorDetails pythonExecutorConfiguration = pythonExecutorConfigurationDataService.getPythonExecutorConfiguration();
        if (pythonExecutorConfiguration == null) {
            return false;
        }
        String pythonExecutorPort = pythonExecutorConfiguration.getPort();
        String mgmtUrl = System.getProperty("mgmt.url");
        int port = 0;
        try {
            URL url = new URL(mgmtUrl);
            port = url.getPort();
            if (port == -1) {
                port = url.getDefaultPort();
            }
        } catch (MalformedURLException e) {
            logger.error(e);
        }
        return String.valueOf(port).equals(pythonExecutorPort);
    }

    enum PythonExecutorStatus { UP, DOWN, BLOCKED }
}
