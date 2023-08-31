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
import io.cloudslang.runtime.api.python.executor.services.PythonExecutorCommunicationService;
import io.cloudslang.runtime.api.python.executor.services.PythonExecutorConfigurationDataService;
import io.cloudslang.runtime.api.python.executor.services.PythonExecutorLifecycleManagerService;
import io.cloudslang.runtime.api.python.executor.entities.PythonExecutorDetails;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jutils.jprocesses.JProcesses;
import org.jutils.jprocesses.info.ProcessesFactory;
import org.jutils.jprocesses.info.ProcessesService;
import org.jutils.jprocesses.model.ProcessInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static io.cloudslang.runtime.api.python.enums.PythonStrategy.PYTHON_EXECUTOR;
import static io.cloudslang.runtime.api.python.enums.PythonStrategy.getPythonStrategy;
import static java.io.File.separator;
import static java.lang.Integer.getInteger;
import static java.lang.Long.getLong;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.apache.commons.io.FilenameUtils.separatorsToUnix;
import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.startsWith;

@Service("pythonExecutorLifecycleManagerService")
public class PythonExecutorLifecycleManagerServiceImpl implements PythonExecutorLifecycleManagerService {

    private static final Logger logger = LogManager.getLogger(PythonExecutorLifecycleManagerServiceImpl.class);
    private static final boolean IS_PYTHON_EXECUTOR_EVAL = getPythonStrategy(System.getProperty("python.expressionsEval"), PYTHON_EXECUTOR) == PYTHON_EXECUTOR;
    private static final String EXTERNAL_PYTHON_EXECUTOR_HEALTH_PATH = "/rest/v1/health";
    private static final int START_STOP_RETRIES_COUNT = getInteger("python.executor.startStopRetriesCount", 20);
    private static final long PYTHON_EXECUTOR_INITIAL_DELAY = 30_000L;
    private static final long PYTHON_EXECUTOR_KEEP_ALIVE_INTERVAL = getLong("python.executor.keepAliveDelayMillis", 30_000L);
    private static final int PYTHON_EXECUTOR_KEEP_ALIVE_RETRIES_COUNT = getInteger("python.executor.keepAliveRetriesCount", 50);

    private final AtomicInteger currentKeepAliveRetriesCount;
    private final AtomicBoolean pythonExecutorRunning;
    private ScheduledThreadPoolExecutor scheduledExecutor;
    private final AtomicReference<Process> pythonExecutorProcess;
    private final PythonExecutorCommunicationService pythonExecutorCommunicationService;
    private final PythonExecutorConfigurationDataService pythonExecutorConfigurationDataService;
    private final AtomicReference<PythonExecutorProcessManager> pythonExecutorProcessManager;


    @Autowired
    public PythonExecutorLifecycleManagerServiceImpl(PythonExecutorCommunicationService pythonExecutorCommunicationService,
                                                     PythonExecutorConfigurationDataService pythonExecutorConfigurationDataService) {
        this.pythonExecutorCommunicationService = pythonExecutorCommunicationService;
        this.pythonExecutorConfigurationDataService = pythonExecutorConfigurationDataService;
        this.pythonExecutorRunning = new AtomicBoolean(false);
        this.pythonExecutorProcess = new AtomicReference<>(null);
        this.currentKeepAliveRetriesCount = new AtomicInteger(0);
        this.pythonExecutorProcessManager = new AtomicReference<>(new PythonExecutorProcessManager());
    }

    @PostConstruct
    public void init() {
        if (IS_PYTHON_EXECUTOR_EVAL) {
            boolean shouldCreateKeepAlive = false;
            try {
                // on startUp find if there is any leftover process alive
                pythonExecutorProcessManager.get().fillPythonExecutorPIDs();
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
            if (response.getLeft() == 200 && pythonExecutorProcess.get() == null) {
                logger.warn("Python Executor port is already in use");
                pythonExecutorRunning.set(false);
                return PythonExecutorStatus.BLOCKED;
            }
            return response.getLeft() == 200 ? PythonExecutorStatus.UP : PythonExecutorStatus.DOWN;
        } catch (IllegalArgumentException e) {
            logger.error(e);
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
        if (getPythonExecutorStatus() != PythonExecutorStatus.UP || !pythonExecutorRunning.get()) {
            logger.info("Python Executor was already stopped");
            return;
        }

        pythonExecutorProcessManager.get().stopPythonExecutorProcesses();
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

        boolean hasPythonProcessStarted = isWindows() ? startWindowsProcess() : startLinuxProcess();
        if (hasPythonProcessStarted) {
            waitToStart();
        }

        return true;
    }

    private boolean startWindowsProcess() {
        return startProcess("start-python-executor.bat");
    }

    private boolean startLinuxProcess() {
        return startProcess("start-python-executor.sh");
    }

    private boolean startProcess(String startPythonExecutor) {
        PythonExecutorDetails pythonExecutorConfiguration = pythonExecutorConfigurationDataService.getPythonExecutorConfiguration();
        if (pythonExecutorConfiguration == null) {
            logger.error("Invalid python configuration. Cannot start python process");
            return false;
        }
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
            pythonExecutorProcess.set(pb.start());
            return true;
        } catch (IOException ioException) {
            logger.error("Failed to start Python Executor", ioException);
        } catch (Exception exception) {
            logger.error("An error occurred while trying to start the Python Executor", exception);
        }
        return false;
    }

    private void waitToStart() {
        logger.info("Waiting to start");

        for (int tries = 0; tries < START_STOP_RETRIES_COUNT; tries++) {
            if (getPythonExecutorStatus() == PythonExecutorStatus.UP) {
                pythonExecutorRunning.set(true);
                currentKeepAliveRetriesCount.set(0);
                pythonExecutorProcessManager.get().fillPythonExecutorPIDs();
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
                    pythonExecutorProcessManager.get().stopPythonExecutorProcesses()) {
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
            logger.info("Python executor did not start in " + currentKeepAliveRetriesCount.get() + " retries and stopped trying");
            return;
        }

        if (getPythonExecutorStatus() == PythonExecutorStatus.UP) {
            return;
        }

        doStartPythonExecutor();
    }

    private void destroyPythonExecutorProcess() {
        pythonExecutorProcessManager.get().stopPythonExecutorProcesses();
        if (pythonExecutorProcess.get() != null) {
            pythonExecutorProcess.get().destroy();
            pythonExecutorProcess.set(null);
            pythonExecutorRunning.set(false);
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

    private class PythonExecutorProcessManager {
        final ProcessesService processService;
        Integer pythonExecutorParentPID;
        Set<Integer> pythonExecutorChildPIDs;

        PythonExecutorProcessManager() {
            this.processService = ProcessesFactory.getService();
            this.pythonExecutorParentPID = null;
            this.pythonExecutorChildPIDs = new HashSet<>();
        }

        void fillPythonExecutorPIDs() {
            List<ProcessInfo> processInfoList = this.processService.getList("python", true);

            findParentPID(processInfoList);
            findChildPIDs(processInfoList);
        }

        synchronized boolean stopPythonExecutorProcesses() {
            if (this.pythonExecutorParentPID == null) {
                if (this.pythonExecutorChildPIDs.isEmpty()) {
                    return true;
                }
            } else {
                if (!JProcesses.killProcessGracefully(this.pythonExecutorParentPID).isSuccess()) {
                    if (JProcesses.killProcess(this.pythonExecutorParentPID).isSuccess()) {
                        this.pythonExecutorParentPID = null;
                    }
                }
            }

            if (this.pythonExecutorChildPIDs.isEmpty()) {
                return true;
            }

            Set<Integer> removedPythonExecutorPIDs = new HashSet<>(this.pythonExecutorChildPIDs.size());
            for (Integer pythonExecutorPID : this.pythonExecutorChildPIDs) {
                if (!JProcesses.killProcessGracefully(pythonExecutorPID).isSuccess()) {
                    if (JProcesses.killProcess(pythonExecutorPID).isSuccess()) {
                        removedPythonExecutorPIDs.add(pythonExecutorPID);
                    }
                }
            }
            this.pythonExecutorChildPIDs.removeAll(removedPythonExecutorPIDs);

            return this.pythonExecutorChildPIDs.isEmpty();
        }

        void findParentPID(List<ProcessInfo> processInfoList) {
            for (ProcessInfo processInfo : processInfoList) {
                if (computeParentProcess(processInfo.getCommand())) {
                    this.pythonExecutorParentPID = Integer.valueOf(processInfo.getPid());
                    return;
                }
            }
        }

        void findChildPIDs(List<ProcessInfo> processInfoList) {
            if (this.pythonExecutorParentPID == null) {
                return;
            }

            for (ProcessInfo processInfo : processInfoList) {
                if (computeChildProcess(processInfo.getCommand())) {
                    this.pythonExecutorChildPIDs.add(Integer.valueOf(processInfo.getPid()));
                }
            }
        }

        boolean computeParentProcess(String command) {
            String appDirPrefix = "--app-dir=\"";
            int appDirStartIndex = command.indexOf(appDirPrefix);

            if (appDirStartIndex == -1) {
                return false;
            }

            int appDirEndIndex = command.indexOf("\"", appDirStartIndex + appDirPrefix.length());
            String appDirValue = separatorsToUnix(command.substring(appDirStartIndex + appDirPrefix.length(), appDirEndIndex));
            String sourceLocation = separatorsToUnix(pythonExecutorConfigurationDataService.getPythonExecutorConfiguration().getSourceLocation());

            return startsWith(appDirValue, sourceLocation);
        }

        boolean computeChildProcess(String command) {
            String parentPIDPrefix = "parent_pid=";
            int parentPIDStartIndex = command.indexOf(parentPIDPrefix);

            if (parentPIDStartIndex == -1) {
                return false;
            }

            int parentPIDEndIndex = command.indexOf(",", parentPIDStartIndex + parentPIDPrefix.length());
            String parentPIDValue = command.substring(parentPIDStartIndex + parentPIDPrefix.length(), parentPIDEndIndex);
            
            return StringUtils.equals(parentPIDValue, this.pythonExecutorParentPID.toString());
        }
    }
}
