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
import io.cloudslang.runtime.api.python.executor.conditions.OnWindowsCondition;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jutils.jprocesses.info.ProcessesFactory;
import org.jutils.jprocesses.info.ProcessesService;
import org.jutils.jprocesses.model.ProcessInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Conditional(OnWindowsCondition.class)
@Component("pythonExecutorProcessInspector")
public class PythonExecutorProcessInspectorWindowsImpl implements PythonExecutorProcessInspector {
    private final PythonExecutorConfigurationDataService pythonExecutorConfigurationDataService;
    private final ProcessesService pythonExecutorProcessesService;

    @Autowired
    public PythonExecutorProcessInspectorWindowsImpl(PythonExecutorConfigurationDataService pythonExecutorConfigurationDataService) {
        this.pythonExecutorConfigurationDataService = pythonExecutorConfigurationDataService;
        this.pythonExecutorProcessesService = ProcessesFactory.getService();
    }

    @Override
    public List<ProcessInfo> getPythonProcessInfoList() {
        return pythonExecutorProcessesService.getList("python.exe");
    }

    @Override
    public Pair<String, List<String>> findPythonExecutorProcessesPID(List<ProcessInfo> processInfoList) {
        String pythonExecutorParentPID = findPythonExecutorParentPID(processInfoList);

        if (pythonExecutorParentPID == null) {
            return Pair.of(null, null);
        }

        List<String> pythonExecutorChildrenPID = findPythonExecutorChildrenPID(processInfoList, pythonExecutorParentPID);

        return Pair.of(pythonExecutorParentPID, pythonExecutorChildrenPID);
    }

    private String findPythonExecutorParentPID(List<ProcessInfo> processInfoList) {
        for (ProcessInfo processInfo : processInfoList) {
            if (isParentProcess(processInfo.getCommand())) {
                return processInfo.getPid();
            }
        }

        return null;
    }

    private List<String> findPythonExecutorChildrenPID(List<ProcessInfo> processInfoList, String pythonExecutorParentPID) {
        List<String> pythonExecutorChildrenPID = new ArrayList<>();

        for (ProcessInfo processInfo : processInfoList) {
            if (isChildProcess(processInfo.getCommand(), pythonExecutorParentPID)) {
                pythonExecutorChildrenPID.add(processInfo.getPid());
            }
        }

        return pythonExecutorChildrenPID;
    }

    private boolean isParentProcess(String command) {
        String appDirPrefix = "--app-dir=\"";
        int appDirStartIndex = command.indexOf(appDirPrefix);

        if (appDirStartIndex == -1) {
            return false;
        }

        int appDirEndIndex = command.indexOf("\"", appDirStartIndex + appDirPrefix.length());
        String appDirValue = command.substring(appDirStartIndex + appDirPrefix.length(), appDirEndIndex);
        Path appDirValueNormalizedParentPath = Paths.get(appDirValue).normalize().getParent();
        Path sourceLocationPath = Paths.get(pythonExecutorConfigurationDataService.getPythonExecutorConfiguration().getSourceLocation());

        return appDirValueNormalizedParentPath.equals(sourceLocationPath);
    }

    private boolean isChildProcess(String command, String pythonExecutorParentPID) {
        String parentPIDPrefix = "parent_pid=";
        int parentPIDStartIndex = command.indexOf(parentPIDPrefix);

        if (parentPIDStartIndex == -1) {
            return false;
        }

        int parentPIDEndIndex = command.indexOf(",", parentPIDStartIndex + parentPIDPrefix.length());
        String parentPIDValue = command.substring(parentPIDStartIndex + parentPIDPrefix.length(), parentPIDEndIndex);

        return StringUtils.equals(parentPIDValue, pythonExecutorParentPID);
    }
}
