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
import io.cloudslang.runtime.api.python.executor.conditions.OnLinuxCondition;
import org.apache.commons.lang3.tuple.Pair;
import org.jutils.jprocesses.info.ProcessesFactory;
import org.jutils.jprocesses.info.ProcessesService;
import org.jutils.jprocesses.model.ProcessInfo;
import org.jutils.jprocesses.util.ProcessesUtils;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isEmpty;

@Conditional(OnLinuxCondition.class)
@Component("pythonExecutorProcessInspector")
public class PythonExecutorProcessInspectorLinuxImpl implements PythonExecutorProcessInspector {
    private final PythonExecutorConfigurationDataService pythonExecutorConfigurationDataService;
    private final ProcessesService pythonExecutorProcessesService;

    public PythonExecutorProcessInspectorLinuxImpl(PythonExecutorConfigurationDataService pythonExecutorConfigurationDataService) {
        this.pythonExecutorConfigurationDataService = pythonExecutorConfigurationDataService;
        this.pythonExecutorProcessesService = ProcessesFactory.getService();
    }

    @Override
    public List<ProcessInfo> getPythonProcessInfoList() {
        return pythonExecutorProcessesService.getList("python3");
    }

    @Override
    public Pair<String, List<String>> findPythonExecutorProcessesPID(List<ProcessInfo> processInfoList) {
        String pythonExecutorParentPID = findPythonExecutorParentPID(processInfoList);

        if (pythonExecutorParentPID == null) {
            return Pair.of(null, null);
        }

        List<String> pythonExecutorChildrenPID = findPythonExecutorChildrenPID(pythonExecutorParentPID);

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

    private List<String> findPythonExecutorChildrenPID(String pythonExecutorParentPID) {
        List<String> pythonExecutorChildrenPID = new ArrayList<>();
        String commandOutput = ProcessesUtils.executeCommand("pgrep", "-P", pythonExecutorParentPID);

        if (isEmpty(commandOutput)) {
            return pythonExecutorChildrenPID;
        }

        String[] commandOutputLines = commandOutput.split("\\r?\\n");
        pythonExecutorChildrenPID.addAll(Arrays.asList(commandOutputLines));

        return pythonExecutorChildrenPID;
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
        Path appDirValueNormalizedParentPath = Paths.get(appDirValue).normalize();
        Path sourceLocationPath = Paths.get(pythonExecutorConfigurationDataService.getPythonExecutorConfiguration().getSourceLocation());

        return appDirValueNormalizedParentPath.equals(sourceLocationPath);
    }
}
