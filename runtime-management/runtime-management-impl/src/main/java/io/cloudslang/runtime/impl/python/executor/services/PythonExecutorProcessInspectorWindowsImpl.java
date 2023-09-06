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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jutils.jprocesses.info.ProcessesFactory;
import org.jutils.jprocesses.info.ProcessesService;
import org.jutils.jprocesses.model.ProcessInfo;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class PythonExecutorProcessInspectorWindowsImpl implements PythonExecutorProcessInspector {
    private final PythonExecutorConfigurationDataService pythonExecutorConfigurationDataService;
    private final ProcessesService pythonExecutorProcessesService;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    public PythonExecutorProcessInspectorWindowsImpl(PythonExecutorConfigurationDataService pythonExecutorConfigurationDataService) {
        this.pythonExecutorConfigurationDataService = pythonExecutorConfigurationDataService;
        this.pythonExecutorProcessesService = ProcessesFactory.getService();
    }

    @Override
    public List<ProcessInfo> getPythonProcessInfoList() {
        return pythonExecutorProcessesService.getList("python.exe", true);
    }

    @Override
    public Pair<String, List<String>> findPythonExecutorProcessesPid(List<ProcessInfo> processInfoList) {
        String pythonExecutorParentPid = findPythonExecutorParentPid(processInfoList);

        if (pythonExecutorParentPid == null) {
            return Pair.of(null, null);
        }

        List<String> pythonExecutorChildrenPid = findPythonExecutorChildrenPid(processInfoList, pythonExecutorParentPid);

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

    private List<String> findPythonExecutorChildrenPid(List<ProcessInfo> processInfoList, String pythonExecutorParentPid) {
        List<String> pythonExecutorChildrenPid = new ArrayList<>();

        for (ProcessInfo processInfo : processInfoList) {
            if (isChildProcess(processInfo.getCommand(), pythonExecutorParentPid)) {
                pythonExecutorChildrenPid.add(processInfo.getPid());
            }
        }

        if (pythonExecutorChildrenPid.isEmpty()) {
            return null;
        }

        return pythonExecutorChildrenPid;
    }

    private boolean isParentProcess(String command) {
        String appDirPrefix = "--app-dir=\"";
        int appDirStartIndex = command.indexOf(appDirPrefix);

        if (appDirStartIndex == -1) {
            return false;
        }

        int appDirEndIndex = command.indexOf("\"", appDirStartIndex + appDirPrefix.length());
        String appDirValue = command.substring(appDirStartIndex + appDirPrefix.length(), appDirEndIndex);
        Path appDirValueNormalizedPath = Paths.get(appDirValue).normalize();
        Path sourceLocationPath = Paths.get(pythonExecutorConfigurationDataService.getPythonExecutorConfiguration().getSourceLocation());

        return appDirValueNormalizedPath.equals(sourceLocationPath);
    }

    private boolean isChildProcess(String command, String pythonExecutorParentPid) {
        String parentPidPrefix = "parent_pid=";
        int parentPidStartIndex = command.indexOf(parentPidPrefix);

        if (parentPidStartIndex == -1) {
            return false;
        }

        int parentPidEndIndex = command.indexOf(",", parentPidStartIndex + parentPidPrefix.length());
        String parentPidValue = command.substring(parentPidStartIndex + parentPidPrefix.length(), parentPidEndIndex);

        return StringUtils.equals(parentPidValue, pythonExecutorParentPid);
    }
}
