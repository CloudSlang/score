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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cloudslang.runtime.api.python.PythonExecutionResult;
import io.cloudslang.runtime.impl.Executor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ExternalPythonExecutor implements Executor {
    private static final String PYTHON_SCRIPT_FILENAME = "script";
    private static final String PYTHON_MAIN_FILENAME = "main";
    private static final String PYTHON_SUFFIX = ".py";
    private static Logger logger = Logger.getLogger(ExternalPythonExecutor.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public PythonExecutionResult exec(String script, Map<String, Serializable> inputs) {
        TempExecutionEnvironment tempExecutionEnvironment = null;
        try {
            String pythonPath = System.getProperty("python.path");
            if (StringUtils.isEmpty(pythonPath) || !new File(pythonPath).exists()) {
                throw new IllegalArgumentException("Missing or invalid python path");
            }
            tempExecutionEnvironment = generateTempExecutionResources(script);
            return runPythonProcess(pythonPath, tempExecutionEnvironment, inputs);

        } catch (IOException e) {
            String message = "Failed to generate execution resources";
            logger.error(message, e);
            throw new RuntimeException(message);
        } finally {
            if (tempExecutionEnvironment != null && !FileUtils.deleteQuietly(tempExecutionEnvironment.parentFolder)
                    && tempExecutionEnvironment.parentFolder != null) {
                logger.warn(String.format("Failed to cleanup python execution resources {%s}", tempExecutionEnvironment.parentFolder));
            }
        }
    }

    private PythonExecutionResult runPythonProcess(String pythonPath, TempExecutionEnvironment executionEnvironment,
                                                   Map<String, Serializable> inputs) throws IOException {

        String userScript = FilenameUtils.removeExtension(executionEnvironment.userScriptName);
        String payload = generatePayload(userScript, inputs);
        ProcessBuilder processBuilder = preparePythonProcess(executionEnvironment, pythonPath);

        try {
            Process process = processBuilder.start();

            PrintWriter printWriter = new PrintWriter(process.getOutputStream());
            printWriter.println(payload);
            printWriter.flush();

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                StringWriter writer = new StringWriter();
                IOUtils.copy(process.getErrorStream(), writer, StandardCharsets.UTF_8);
                logger.error(writer.toString());
                throw new RuntimeException("Script return non 0 result");
            }

            ScriptResults scriptResults = objectMapper.readValue(process.getInputStream(), ScriptResults.class);

            String exception = scriptResults.getException();
            if (!StringUtils.isEmpty(exception)) {
                logger.error(String.format("Failed to execute script {%s}", exception));
                throw new ExternalPythonScriptException(String.format("Failed to execute script {%s}", exception));
            }

            //noinspection unchecked
            return new PythonExecutionResult(scriptResults.getReturnResult());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to run script");
        }
    }

    @Override
    public void allocate() {
    }

    @Override
    public void release() {
    }

    @Override
    public void close() {
    }

    private ProcessBuilder preparePythonProcess(TempExecutionEnvironment executionEnvironment, String pythonPath) {
        ProcessBuilder processBuilder = new ProcessBuilder(Arrays.asList(Paths.get(pythonPath, "python").toString(),
                Paths.get(executionEnvironment.parentFolder.toString(), executionEnvironment.mainScriptName).toString()));
        processBuilder.environment().clear();
        processBuilder.directory(executionEnvironment.parentFolder);

        return processBuilder;
    }

    private TempExecutionEnvironment generateTempExecutionResources(String script) throws IOException {
        Path execTempDirectory = Files.createTempDirectory("python_execution");
        File tempUserScript = File.createTempFile(PYTHON_SCRIPT_FILENAME, PYTHON_SUFFIX, execTempDirectory.toFile());
        FileUtils.writeStringToFile(tempUserScript, script);

        ClassLoader classLoader = ExternalPythonExecutor.class.getClassLoader();
        Path mainScriptPath = Paths.get(execTempDirectory.toString(), PYTHON_MAIN_FILENAME + PYTHON_SUFFIX);

        Files.copy(classLoader.getResourceAsStream(PYTHON_MAIN_FILENAME + PYTHON_SUFFIX), mainScriptPath);

        String tempUserScriptName = FilenameUtils.getName(tempUserScript.toString());
        String mainScriptName = FilenameUtils.getName(mainScriptPath.toString());
        return new TempExecutionEnvironment(tempUserScriptName, mainScriptName, execTempDirectory.toFile());
    }

    private String generatePayload(String userScript, Map<String, Serializable> inputs) throws JsonProcessingException {
        Map<String, Serializable> payload = new HashMap<>();
        Map<String, String> parsedInputs = new HashMap<>();
        inputs.forEach((key, value) -> parsedInputs.put(key, value.toString()));

        payload.put("script_name", FilenameUtils.removeExtension(userScript));
        payload.put("inputs", (Serializable) parsedInputs);
        return objectMapper.writeValueAsString(payload);
    }

    private class TempExecutionEnvironment {
        private final String userScriptName;
        private final String mainScriptName;
        private final File parentFolder;

        private TempExecutionEnvironment(String userScriptName, String mainScriptName, File parentFolder) {
            this.userScriptName = userScriptName;
            this.mainScriptName = mainScriptName;
            this.parentFolder = parentFolder;
        }
    }
}
