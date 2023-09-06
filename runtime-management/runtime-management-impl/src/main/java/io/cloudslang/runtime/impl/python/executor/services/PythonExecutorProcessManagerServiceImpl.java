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

import io.cloudslang.runtime.api.python.executor.entities.PythonExecutorDetails;
import io.cloudslang.runtime.api.python.executor.entities.PythonExecutorProcessDetails;
import io.cloudslang.runtime.api.python.executor.services.PythonExecutorConfigurationDataService;
import io.cloudslang.runtime.api.python.executor.services.PythonExecutorProcessInspector;
import io.cloudslang.runtime.api.python.executor.services.PythonExecutorProcessManagerService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jutils.jprocesses.info.ProcessesFactory;
import org.jutils.jprocesses.info.ProcessesService;
import org.jutils.jprocesses.model.JProcessesResponse;
import org.jutils.jprocesses.model.ProcessInfo;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.io.File.separator;

public class PythonExecutorProcessManagerServiceImpl implements PythonExecutorProcessManagerService {
    private static final Logger logger = LogManager.getLogger(PythonExecutorProcessManagerServiceImpl.class);
    private final PythonExecutorConfigurationDataService pythonExecutorConfigurationDataService;
    private final PythonExecutorProcessInspector pythonExecutorProcessInspector;
    private final ProcessesService pythonExecutorProcessesService;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    public PythonExecutorProcessManagerServiceImpl(PythonExecutorConfigurationDataService pythonExecutorConfigurationDataService,
                                                   PythonExecutorProcessInspector pythonExecutorProcessInspector) {
        this.pythonExecutorConfigurationDataService = pythonExecutorConfigurationDataService;
        this.pythonExecutorProcessInspector = pythonExecutorProcessInspector;
        this.pythonExecutorProcessesService = ProcessesFactory.getService();
    }

    @Override
    public synchronized Process startPythonExecutorProcess() {
        String startPythonExecutor = SystemUtils.IS_OS_WINDOWS ? "start-python-executor.bat" : "start-python-executor.sh";
        PythonExecutorDetails pythonExecutorConfiguration = pythonExecutorConfigurationDataService.getPythonExecutorConfiguration();
        if (pythonExecutorConfiguration == null) {
            logger.error("Invalid python configuration. Cannot start python process");
            return null;
        }
        String startPythonExecutorSourceLocation = pythonExecutorConfiguration.getSourceLocation() +
                separator + "bin" + separator + startPythonExecutor;
        ProcessBuilder pb = new ProcessBuilder(startPythonExecutorSourceLocation,
                pythonExecutorConfiguration.getPort(),
                pythonExecutorConfiguration.getWorkers());
        pb.directory(FileUtils.getFile(pythonExecutorConfiguration.getSourceLocation() + separator + "bin"));
        try {
            logger.info("Starting Python Executor on port: " + pythonExecutorConfiguration.getPort());
            return pb.start();
        } catch (IOException ioException) {
            logger.error("Failed to start Python Executor", ioException);
        } catch (Exception exception) {
            logger.error("An error occurred while trying to start the Python Executor", exception);
        }
        return null;
    }

    @Override
    public synchronized void updatePythonExecutorProcessDetails(PythonExecutorProcessDetails pythonExecutorProcessDetails) {
        List<ProcessInfo> pythonProcessInfoList = pythonExecutorProcessInspector.getPythonProcessInfoList();
        Pair<String, List<String>> pythonExecutorProcessesPid = pythonExecutorProcessInspector.findPythonExecutorProcessesPid(pythonProcessInfoList);
        pythonExecutorProcessDetails.setPythonExecutorParentPid(pythonExecutorProcessesPid.getLeft());
        pythonExecutorProcessDetails.setPythonExecutorChildrenPid(pythonExecutorProcessesPid.getRight());
        logger.info(pythonExecutorProcessDetails.getPythonExecutorParentPid());
        logger.info(pythonExecutorProcessDetails.getPythonExecutorChildrenPid());
    }

    @Override
    public synchronized boolean stopPythonExecutorProcess(PythonExecutorProcessDetails pythonExecutorProcessDetails) {
        String pythonExecutorParentPid = pythonExecutorProcessDetails.getPythonExecutorParentPid();
        if (pythonExecutorParentPid == null) {
            List<String> pythonExecutorChildrenPid = pythonExecutorProcessDetails.getPythonExecutorChildrenPid();
            if (pythonExecutorChildrenPid == null) {
                return true;
            }
            return doStopPythonExecutorChildrenProcess(pythonExecutorChildrenPid, pythonExecutorProcessDetails);
        }
        List<String> pythonExecutorChildrenPid = pythonExecutorProcessDetails.getPythonExecutorChildrenPid();
        if (pythonExecutorChildrenPid == null) {
            return doStopPythonExecutorParentProcess(pythonExecutorParentPid, pythonExecutorProcessDetails);
        }
        if (doStopPythonExecutorChildrenProcess(pythonExecutorChildrenPid, pythonExecutorProcessDetails)) {
            return doStopPythonExecutorParentProcess(pythonExecutorParentPid, pythonExecutorProcessDetails);
        }
        return false;
    }

    private boolean doStopPythonExecutorParentProcess(String pythonExecutorParentPid, PythonExecutorProcessDetails pythonExecutorProcessDetails) {
        int pythonExecutorParentPidValue = Integer.parseInt(pythonExecutorParentPid);
        JProcessesResponse killProcessGracefullyResponse = pythonExecutorProcessesService.killProcessGracefully(pythonExecutorParentPidValue);
        if (!killProcessGracefullyResponse.isSuccess()) {
            JProcessesResponse killProcessResponse = pythonExecutorProcessesService.killProcess(pythonExecutorParentPidValue);
            if (!killProcessResponse.isSuccess()) {
                return false;
            }
        }
        pythonExecutorProcessDetails.setPythonExecutorParentPid(null);
        return true;
    }

    private boolean doStopPythonExecutorChildrenProcess(List<String> pythonExecutorChildrenPid, PythonExecutorProcessDetails pythonExecutorProcessDetails) {
        List<String> notRemovedPythonExecutorChildrenPid = new ArrayList<>();
        for (String pythonExecutorChildPid : pythonExecutorChildrenPid) {
            int pythonExecutorChildPIDValue = Integer.parseInt(pythonExecutorChildPid);
            JProcessesResponse killProcessGracefullyResponse = pythonExecutorProcessesService.killProcessGracefully(pythonExecutorChildPIDValue);
            if (!killProcessGracefullyResponse.isSuccess()) {
                JProcessesResponse killProcessResponse = pythonExecutorProcessesService.killProcess(pythonExecutorChildPIDValue);
                if (!killProcessResponse.isSuccess()) {
                    notRemovedPythonExecutorChildrenPid.add(pythonExecutorChildPid);
                }
            }
        }
        if(notRemovedPythonExecutorChildrenPid.isEmpty()) {
            pythonExecutorProcessDetails.setPythonExecutorChildrenPid(null);
            return true;
        }
        pythonExecutorProcessDetails.setPythonExecutorChildrenPid(notRemovedPythonExecutorChildrenPid);
        return false;
    }
}
