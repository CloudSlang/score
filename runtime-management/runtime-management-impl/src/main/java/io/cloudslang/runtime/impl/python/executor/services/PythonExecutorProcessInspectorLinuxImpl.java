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

import io.cloudslang.runtime.api.python.executor.services.PythonExecutorConfigurationDataService;
import io.cloudslang.runtime.api.python.executor.services.PythonExecutorProcessInspector;
import org.apache.commons.lang3.tuple.Pair;
import org.jutils.jprocesses.info.ProcessesFactory;
import org.jutils.jprocesses.info.ProcessesService;
import org.jutils.jprocesses.model.ProcessInfo;
import org.jutils.jprocesses.util.ProcessesUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public class PythonExecutorProcessInspectorLinuxImpl implements PythonExecutorProcessInspector {
    private final PythonExecutorConfigurationDataService pythonExecutorConfigurationDataService;
    private final ProcessesService pythonExecutorProcessesService;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    public PythonExecutorProcessInspectorLinuxImpl(PythonExecutorConfigurationDataService pythonExecutorConfigurationDataService) {
        this.pythonExecutorConfigurationDataService = pythonExecutorConfigurationDataService;
        this.pythonExecutorProcessesService = ProcessesFactory.getService();
    }

    @Override
    public List<ProcessInfo> getPythonProcessInfoList() {
        return pythonExecutorProcessesService.getList("python3");
    }

    @Override
    public Pair<String, List<String>> findPythonExecutorProcessesPid(List<ProcessInfo> processInfoList) {
        String pythonExecutorParentPid = findPythonExecutorParentPid(processInfoList);

        if (pythonExecutorParentPid == null) {
            return Pair.of(null, null);
        }

        List<String> pythonExecutorChildrenPid = findPythonExecutorChildrenPid(pythonExecutorParentPid);

        return Pair.of(pythonExecutorParentPid, pythonExecutorChildrenPid);
    }

    private String findPythonExecutorParentPid(List<ProcessInfo> processInfoList) {
        for (ProcessInfo processInfo : processInfoList) {
            if (isParentProcess(processInfo.getCommand())) {
                return processInfo.getPid();
            }
        }

        return null;
    }

    private List<String> findPythonExecutorChildrenPid(String pythonExecutorParentPid) {
        String commandOutput = ProcessesUtils.executeCommand("pgrep", "-P", pythonExecutorParentPid);
        if (isEmpty(commandOutput)) {
            return null;
        }

        String[] commandOutputLines = commandOutput.split("\\r?\\n");

        return new ArrayList<>(Arrays.asList(commandOutputLines));
    }

    private boolean isParentProcess(String command) {
        String appDirPrefix = "--app-dir=";
        int appDirStartIndex = command.indexOf(appDirPrefix);

        if (appDirStartIndex == -1) {
            return false;
        }

        int appDirEndIndex = command.indexOf(" ", appDirStartIndex + appDirPrefix.length());
        String appDirValue;
        if (appDirEndIndex == -1) {
            appDirValue = command.substring(appDirStartIndex + appDirPrefix.length());
        } else {
            appDirValue = command.substring(appDirStartIndex + appDirPrefix.length(), appDirEndIndex);
        }
        Path appDirValueNormalizedPath = Paths.get(appDirValue).normalize();
        Path sourceLocationPath = Paths.get(pythonExecutorConfigurationDataService.getPythonExecutorConfiguration().getSourceLocation());

        return appDirValueNormalizedPath.equals(sourceLocationPath);
    }
}
