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
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.cloudslang.runtime.api.python.PythonEvaluationResult;
import io.cloudslang.runtime.api.python.PythonExecutionResult;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class ExternalPythonExecutor {
    private static final String PYTHON_SCRIPT_FILENAME = "script";
    private static final String PYTHON_MAIN_FILENAME = "main";
    private static final String PYTHON_EVAL_FILENAME = "eval";
    private static final String PYTHON_SUFFIX = ".py";
    private static final Logger logger = Logger.getLogger(ExternalPythonExecutor.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final long EXECUTION_TIMEOUT = Long.getLong("python.timeout", 30);
    private static final String PYTHON_FILENAME_SCRIPT_EXTENSION = ".py\"";
    private static final int PYTHON_FILENAME_DELIMITERS = 6;

    public PythonExecutionResult exec(String script, Map<String, Serializable> inputs) {
        TempExecutionEnvironment tempExecutionEnvironment = null;
        try {
            String pythonPath = checkPythonPath();
            tempExecutionEnvironment = generateTempExecutionResources(script);
            String payload = generateExecutionPayload(tempExecutionEnvironment.userScriptName, inputs);
            addFilePermissions(tempExecutionEnvironment.parentFolder);

            return runPythonExecutionProcess(pythonPath, payload, tempExecutionEnvironment);

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

    public PythonEvaluationResult eval(String expression, String prepareEnvironmentScript,
                                       Map<String, Serializable> context) {
        TempEvalEnvironment tempEvalEnvironment = null;
        try {
            String pythonPath = checkPythonPath();
            tempEvalEnvironment = generateTempEvalResources();
            String payload = generateEvalPayload(expression, prepareEnvironmentScript, context);
            addFilePermissions(tempEvalEnvironment.parentFolder);

            return runPythonEvalProcess(pythonPath, payload, tempEvalEnvironment, context);

        } catch (IOException e) {
            String message = "Failed to generate execution resources";
            logger.error(message, e);
            throw new RuntimeException(message);
        } finally {
            if (tempEvalEnvironment != null && !FileUtils.deleteQuietly(tempEvalEnvironment.parentFolder)
                    && tempEvalEnvironment.parentFolder != null) {
                logger.warn(String.format("Failed to cleanup python execution resources {%s}", tempEvalEnvironment.parentFolder));
            }
        }
    }

    private void addFilePermissions(File file) throws IOException {
        Set<PosixFilePermission> filePermissions = new HashSet<>();
        filePermissions.add(PosixFilePermission.OWNER_READ);

        File[] fileChildren = file.listFiles();

        if (fileChildren != null) {
            for (File child : fileChildren) {
                if (SystemUtils.IS_OS_WINDOWS) {
                    child.setReadOnly();
                } else {
                    Files.setPosixFilePermissions(child.toPath(), filePermissions);
                }
            }
        }
    }

    private String checkPythonPath() {
        String pythonPath = System.getProperty("python.path");
        if (StringUtils.isEmpty(pythonPath) || !new File(pythonPath).exists()) {
            throw new IllegalArgumentException("Missing or invalid python path");
        }
        return pythonPath;
    }

    private ScriptExecutionResult parseScriptExecutionResult(String scriptExecutionResult) throws JsonProcessingException {
        return new XmlMapper().readValue(scriptExecutionResult, ScriptExecutionResult.class);
    }

    private PythonExecutionResult runPythonExecutionProcess(String pythonPath, String payload,
                                                            TempExecutionEnvironment executionEnvironment) {

        ProcessBuilder processBuilder = preparePythonProcess(executionEnvironment, pythonPath);

        try {
            String returnResult = getResult(payload, processBuilder);
            returnResult = parseScriptExecutionResult(returnResult).getResult();
            ScriptResults scriptResults = objectMapper.readValue(returnResult, ScriptResults.class);
            String exception = formatException(scriptResults.getException(), scriptResults.getTraceback());

            if (!StringUtils.isEmpty(exception)) {
                logger.error(String.format("Failed to execute script {%s}", exception));
                throw new ExternalPythonScriptException(String.format("Failed to execute user script: %s", exception));
            }

            //noinspection unchecked
            return new PythonExecutionResult(scriptResults.getReturnResult());
        } catch (IOException | InterruptedException e) {
            logger.error("Failed to run script. ", e.getCause() != null ? e.getCause() : e);
            throw new RuntimeException("Failed to run script.");
        }
    }

    private PythonEvaluationResult runPythonEvalProcess(String pythonPath, String payload, TempEvalEnvironment executionEnvironment,
                                                        Map<String, Serializable> context) {

        ProcessBuilder processBuilder = preparePythonProcess(executionEnvironment, pythonPath);

        try {
            String returnResult = getResult(payload, processBuilder);

            EvaluationResults scriptResults = objectMapper.readValue(returnResult, EvaluationResults.class);
            String exception = scriptResults.getException();
            if (!StringUtils.isEmpty(exception)) {
                logger.error(String.format("Failed to execute script {%s}", exception));
                throw new ExternalPythonEvalException(exception);
            }
            context.put("accessed_resources_set", (Serializable) scriptResults.getAccessedResources());
            //noinspection unchecked
            return new PythonEvaluationResult(processReturnResult(scriptResults), context);
        } catch (IOException | InterruptedException e) {
            logger.error("Failed to run script. ", e.getCause() != null ? e.getCause() : e);
            throw new RuntimeException("Failed to run script.");
        }
    }

    private Serializable processReturnResult(EvaluationResults results) {
        EvaluationResults.ReturnType returnType = results.getReturnType();
        if (returnType == null) {
            throw new RuntimeException("Missing return type for return result.");
        }
        switch (returnType) {
            case BOOLEAN:
                return Boolean.valueOf(results.getReturnResult());
            case INTEGER:
                return Integer.valueOf(results.getReturnResult());
            default:
                return results.getReturnResult();
        }
    }

    private String getResult(String payload, ProcessBuilder processBuilder) throws IOException, InterruptedException {
        Process process = processBuilder.start();
        PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(process.getOutputStream(),
                StandardCharsets.UTF_8));
        printWriter.println(payload);
        printWriter.flush();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        StringBuilder returnResult = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            returnResult.append(line);
        }
        boolean isInTime = process.waitFor(EXECUTION_TIMEOUT, TimeUnit.MINUTES);
        if (!isInTime) {
            process.destroy();
            throw new RuntimeException("Execution timed out");
        } else if (process.exitValue() != 0) {
            StringWriter writer = new StringWriter();
            IOUtils.copy(process.getErrorStream(), writer, StandardCharsets.UTF_8);
            logger.error(writer.toString());
            throw new RuntimeException("Execution returned non 0 result");
        }
        return returnResult.toString();
    }

    private ProcessBuilder preparePythonProcess(TempEnvironment executionEnvironment, String pythonPath) {
        ProcessBuilder processBuilder = new ProcessBuilder(Arrays.asList(Paths.get(pythonPath, "python").toString(),
                Paths.get(executionEnvironment.parentFolder.toString(), executionEnvironment.mainScriptName).toString()));
        processBuilder.environment().clear();
        processBuilder.directory(executionEnvironment.parentFolder);
        return processBuilder;
    }

    private TempExecutionEnvironment generateTempExecutionResources(String script) throws IOException {
        Path execTempDirectory = Files.createTempDirectory("python_execution");
        File tempUserScript = File.createTempFile(PYTHON_SCRIPT_FILENAME, PYTHON_SUFFIX, execTempDirectory.toFile());
        FileUtils.writeStringToFile(tempUserScript, script, StandardCharsets.UTF_8);

        ClassLoader classLoader = ExternalPythonExecutor.class.getClassLoader();
        Path mainScriptPath = Paths.get(execTempDirectory.toString(), PYTHON_MAIN_FILENAME + PYTHON_SUFFIX);

        Files.copy(classLoader.getResourceAsStream(PYTHON_MAIN_FILENAME + PYTHON_SUFFIX), mainScriptPath);

        String tempUserScriptName = FilenameUtils.getName(tempUserScript.toString());
        String mainScriptName = FilenameUtils.getName(mainScriptPath.toString());
        return new TempExecutionEnvironment(tempUserScriptName, mainScriptName, execTempDirectory.toFile());
    }

    private TempEvalEnvironment generateTempEvalResources() throws IOException {
        Path execTempDirectory = Files.createTempDirectory("python_expression");

        ClassLoader classLoader = ExternalPythonExecutor.class.getClassLoader();
        Path mainScriptPath = Paths.get(execTempDirectory.toString(), PYTHON_EVAL_FILENAME + PYTHON_SUFFIX);

        Files.copy(classLoader.getResourceAsStream(PYTHON_EVAL_FILENAME + PYTHON_SUFFIX), mainScriptPath);

        String mainScriptName = FilenameUtils.getName(mainScriptPath.toString());
        return new TempEvalEnvironment(mainScriptName, execTempDirectory.toFile());
    }

    private String generateEvalPayload(String expression, String prepareEnvironmentScript,
                                       Map<String, Serializable> context) throws JsonProcessingException {
        Map<String, Serializable> payload = new HashMap<>();
        payload.put("expression", expression);
        payload.put("envSetup", prepareEnvironmentScript);
        payload.put("context", (Serializable) context);
        return objectMapper.writeValueAsString(payload);
    }

    private String generateExecutionPayload(String userScript, Map<String, Serializable> inputs) throws JsonProcessingException {
        Map<String, Serializable> payload = new HashMap<>();
        Map<String, String> parsedInputs = new HashMap<>();
        inputs.forEach((key, value) -> parsedInputs.put(key, value.toString()));

        payload.put("script_name", FilenameUtils.removeExtension(userScript));
        payload.put("inputs", (Serializable) parsedInputs);
        return objectMapper.writeValueAsString(payload);
    }

    private String formatException(String exception, List<String> traceback) {
        if (CollectionUtils.isEmpty(traceback)) {
            return exception;
        }

        return removeFileName(traceback.get(traceback.size() - 1)) + ", " + exception;
    }

    private String removeFileName(String trace) {
        int pythonFileNameIndex = trace.indexOf(PYTHON_FILENAME_SCRIPT_EXTENSION);
        return trace.substring(pythonFileNameIndex + PYTHON_FILENAME_DELIMITERS);
    }

    private class TempEnvironment {
        final String mainScriptName;
        final File parentFolder;

        private TempEnvironment(String mainScriptName, File parentFolder) {
            this.mainScriptName = mainScriptName;
            this.parentFolder = parentFolder;
        }
    }

    private class TempExecutionEnvironment extends TempEnvironment {
        private final String userScriptName;

        private TempExecutionEnvironment(String userScriptName, String mainScriptName, File parentFolder) {
            super(mainScriptName, parentFolder);
            this.userScriptName = userScriptName;
        }
    }

    private class TempEvalEnvironment extends TempEnvironment {
        private TempEvalEnvironment(String mainScriptName, File parentFolder) {
            super(mainScriptName, parentFolder);
        }
    }
}
