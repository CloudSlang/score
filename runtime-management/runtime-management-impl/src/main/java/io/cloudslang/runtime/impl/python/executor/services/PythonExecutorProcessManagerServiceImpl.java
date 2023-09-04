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
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.io.File.separator;

@Service("pythonExecutorProcessManagerService")
public class PythonExecutorProcessManagerServiceImpl implements PythonExecutorProcessManagerService {
    private static final Logger logger = LogManager.getLogger(PythonExecutorProcessManagerServiceImpl.class);
    private final PythonExecutorConfigurationDataService pythonExecutorConfigurationDataService;
    private final PythonExecutorProcessInspector pythonExecutorProcessInspector;
    private final ProcessesService pythonExecutorProcessesService;

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
        ProcessBuilder pb = new ProcessBuilder(
                pythonExecutorConfiguration.getSourceLocation() +
                        separator +
                        "bin" +
                        separator +
                        startPythonExecutor,
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
        Pair<String, List<String>> pythonExecutorProcessesPID = pythonExecutorProcessInspector.findPythonExecutorProcessesPID(pythonProcessInfoList);
        pythonExecutorProcessDetails.setPythonExecutorParentPID(pythonExecutorProcessesPID.getLeft());
        pythonExecutorProcessDetails.setPythonExecutorChildrenPID(pythonExecutorProcessesPID.getRight());
    }

    @Override
    public synchronized boolean stopPythonExecutorProcess(PythonExecutorProcessDetails pythonExecutorProcessDetails) {
        String pythonExecutorParentPID = pythonExecutorProcessDetails.getPythonExecutorParentPID();
        if (pythonExecutorParentPID == null) {
            List<String> pythonExecutorChildrenPID = pythonExecutorProcessDetails.getPythonExecutorChildrenPID();
            if (pythonExecutorChildrenPID == null) {
                return true;
            }
            return stopPythonExecutorChildrenProcess(pythonExecutorChildrenPID, pythonExecutorProcessDetails);
        }
        List<String> pythonExecutorChildrenPID = pythonExecutorProcessDetails.getPythonExecutorChildrenPID();
        if (pythonExecutorChildrenPID == null) {
            return stopPythonExecutorParentProcess(pythonExecutorParentPID, pythonExecutorProcessDetails);
        }
        return stopPythonExecutorParentProcess(pythonExecutorParentPID, pythonExecutorProcessDetails) &&
                stopPythonExecutorChildrenProcess(pythonExecutorChildrenPID, pythonExecutorProcessDetails);
    }

    private boolean stopPythonExecutorParentProcess(String pythonExecutorParentPID, PythonExecutorProcessDetails pythonExecutorProcessDetails) {
        int pythonExecutorParentPIDValue = Integer.parseInt(pythonExecutorParentPID);
        JProcessesResponse killProcessGracefullyResponse = pythonExecutorProcessesService.killProcessGracefully(pythonExecutorParentPIDValue);
        if (!killProcessGracefullyResponse.isSuccess()) {
            JProcessesResponse killProcessResponse = pythonExecutorProcessesService.killProcess(pythonExecutorParentPIDValue);
            if (!killProcessResponse.isSuccess()) {
                return false;
            }
        }
        pythonExecutorProcessDetails.setPythonExecutorParentPID(null);
        return true;
    }

    private boolean stopPythonExecutorChildrenProcess(List<String> pythonExecutorChildrenPID, PythonExecutorProcessDetails pythonExecutorProcessDetails) {
        List<String> notRemovedPythonExecutorChildrenPID = new ArrayList<>();
        for (String pythonExecutorChildPID : pythonExecutorChildrenPID) {
            int pythonExecutorChildPIDValue = Integer.parseInt(pythonExecutorChildPID);
            JProcessesResponse killProcessGracefullyResponse = pythonExecutorProcessesService.killProcessGracefully(pythonExecutorChildPIDValue);
            if (!killProcessGracefullyResponse.isSuccess()) {
                JProcessesResponse killProcessResponse = pythonExecutorProcessesService.killProcess(pythonExecutorChildPIDValue);
                if (!killProcessResponse.isSuccess()) {
                    notRemovedPythonExecutorChildrenPID.add(pythonExecutorChildPID);
                }
            }
        }
        if(notRemovedPythonExecutorChildrenPID.isEmpty()) {
            pythonExecutorProcessDetails.setPythonExecutorChildrenPID(null);
            return true;
        }
        pythonExecutorProcessDetails.setPythonExecutorChildrenPID(notRemovedPythonExecutorChildrenPID);
        return false;
    }
}
