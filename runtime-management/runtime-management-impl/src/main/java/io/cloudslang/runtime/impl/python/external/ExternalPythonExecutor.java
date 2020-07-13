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
import io.cloudslang.runtime.api.python.PythonEvaluationResult;
import io.cloudslang.runtime.api.python.PythonExecutionResult;
import io.cloudslang.runtime.external.ResourceScriptCache;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static com.google.common.collect.Maps.newHashMapWithExpectedSize;
import static io.cloudslang.runtime.external.ResourceScriptCache.PYTHON_EVAL_SCRIPT_FILENAME;
import static io.cloudslang.runtime.external.ResourceScriptCache.PYTHON_MAIN_SCRIPT_FILENAME;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.createTempDirectory;
import static java.nio.file.Files.getFileAttributeView;
import static java.nio.file.Files.newBufferedWriter;
import static java.nio.file.Files.setPosixFilePermissions;
import static java.nio.file.Paths.get;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.apache.commons.io.FilenameUtils.removeExtension;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

public class ExternalPythonExecutor {
    private static final Logger logger = Logger.getLogger(ExternalPythonExecutor.class);

    private static final String PYTHON_PROVIDED_SCRIPT_FILENAME = "script.py";
    private static final long EXECUTION_TIMEOUT = Long.getLong("python.timeout", 30);
    private static final String PYTHON_FILENAME_SCRIPT_EXTENSION = ".py\"";
    private static final int PYTHON_FILENAME_DELIMITERS = 6;
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final ResourceScriptCache resourceScriptCache;

    public ExternalPythonExecutor() {
        resourceScriptCache = new ResourceScriptCacheImpl();
    }

    public PythonExecutionResult exec(String script, Map<String, Serializable> inputs) {
        TempExecutionEnvironment tempExecutionEnvironment = null;
        try {
            String pythonPath = checkPythonPath();
            tempExecutionEnvironment = generateTempResourcesForExec(script);
            String payload = generateExecutionPayload(tempExecutionEnvironment.userScriptName, inputs);
            return runPythonExecutionProcess(pythonPath, payload, tempExecutionEnvironment);

        } catch (IOException e) {
            String message = "Failed to generate execution resources";
            logger.error(message, e);
            throw new RuntimeException(message);
        } finally {
            if ((tempExecutionEnvironment != null) && !deleteQuietly(tempExecutionEnvironment.parentFolder.toFile())) {
                logger.warn(String.format("Failed to cleanup python execution resources {%s}",
                        tempExecutionEnvironment.parentFolder));
            }
        }
    }

    public PythonEvaluationResult eval(String expression, String prepareEnvironmentScript,
                                       Map<String, Serializable> context) {
        TempEvalEnvironment tempEvalEnvironment = null;
        try {
            String pythonPath = checkPythonPath();
            tempEvalEnvironment = generateTempResourcesForEval();
            String payload = generateEvalPayload(expression, prepareEnvironmentScript, context);
            return runPythonEvalProcess(pythonPath, payload, tempEvalEnvironment, context);

        } catch (IOException e) {
            String message = "Failed to generate execution resources";
            logger.error(message, e);
            throw new RuntimeException(message);
        } finally {
            if (tempEvalEnvironment != null && !deleteQuietly(tempEvalEnvironment.parentFolder.toFile())) {
                logger.warn(String.format("Failed to cleanup python execution resources {%s}", tempEvalEnvironment.parentFolder));
            }
        }
    }

    private void applyFilePermissions(Path path) throws IOException {
        if (SystemUtils.IS_OS_WINDOWS) {
            applyWindowsFilePermissions(path);
        } else {
            applyPosixFilePermissions(path);
        }
    }

    private void applyWindowsFilePermissions(Path path) throws IOException {
        DosFileAttributeView attributeView = getFileAttributeView(path, DosFileAttributeView.class);
        if (attributeView != null) {
            attributeView.setReadOnly(true);
        }
    }

    private void applyPosixFilePermissions(Path path) throws IOException {
        setPosixFilePermissions(path, EnumSet.of(PosixFilePermission.OWNER_READ));
    }

    private String checkPythonPath() {
        String pythonPath = System.getProperty("python.path");
        if (isEmpty(pythonPath) || !new File(pythonPath).exists()) {
            throw new IllegalArgumentException("Missing or invalid python path");
        }
        return pythonPath;
    }

    private Document parseScriptExecutionResult(String scriptExecutionResult) throws IOException, ParserConfigurationException, SAXException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        InputSource is = new InputSource();
        is.setCharacterStream(new StringReader(scriptExecutionResult));
        return db.parse(is);
    }

    private PythonExecutionResult runPythonExecutionProcess(String pythonPath, String payload,
                                                            TempExecutionEnvironment executionEnvironment) {

        ProcessBuilder processBuilder = preparePythonProcess(executionEnvironment, pythonPath);

        try {
            String returnResult = getResult(payload, processBuilder);
            returnResult = parseScriptExecutionResult(returnResult).getElementsByTagName("result").item(0)
                    .getTextContent();
            ScriptResults scriptResults = objectMapper.readValue(returnResult, ScriptResults.class);
            String exception = formatException(scriptResults.getException(), scriptResults.getTraceback());

            if (isNotEmpty(exception)) {
                logger.error(String.format("Failed to execute script {%s}", exception));
                throw new ExternalPythonScriptException(String.format("Failed to execute user script: %s", exception));
            }

            //noinspection unchecked
            return new PythonExecutionResult(scriptResults.getReturnResult());
        } catch (IOException | InterruptedException | SAXException | ParserConfigurationException e) {
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
            if (isNotEmpty(exception)) {
                logger.error(String.format("Failed to execute script {%s}", exception));
                throw new ExternalPythonEvalException(exception);
            }
            context.put("accessed_resources_set", (Serializable) scriptResults.getAccessedResources());
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
        PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(process.getOutputStream(), UTF_8));
        printWriter.println(payload);
        printWriter.flush();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        StringBuilder returnResult = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            returnResult.append(line);
        }
        boolean isInTime = process.waitFor(EXECUTION_TIMEOUT, MINUTES);
        if (!isInTime) {
            process.destroy();
            throw new RuntimeException("Execution timed out");
        } else if (process.exitValue() != 0) {
            StringWriter writer = new StringWriter();
            IOUtils.copy(process.getErrorStream(), writer, UTF_8);
            logger.error(writer.toString());
            throw new RuntimeException("Execution returned non 0 result");
        }
        return returnResult.toString();
    }

    private ProcessBuilder preparePythonProcess(TempEnvironment environment, String pythonPath) {
        List<String> arguments = new ArrayList<>(2);
        arguments.add(get(pythonPath, "python").toString());
        arguments.add(get(environment.parentFolder.toString(), environment.mainScriptName).toString());
        ProcessBuilder processBuilder = new ProcessBuilder(arguments);
        processBuilder.environment().clear();
        processBuilder.directory(environment.parentFolder.toFile());
        return processBuilder;
    }

    private TempExecutionEnvironment generateTempResourcesForExec(String script) throws IOException {
        Path tempDirPath = createTempDirectory("python_execution");
        String tempDir = tempDirPath.toString();

        // Handle script.py
        Path tempUserScript = get(tempDir, PYTHON_PROVIDED_SCRIPT_FILENAME);
        try (BufferedWriter bufferedWriter = newBufferedWriter(tempUserScript, UTF_8)) {
            bufferedWriter.write(script);
        }
        applyFilePermissions(tempUserScript);

        // Handle  main.py
        Path mainScriptPath = get(tempDir, PYTHON_MAIN_SCRIPT_FILENAME);
        Files.write(mainScriptPath, resourceScriptCache.loadExecScriptAsBytes());
        applyFilePermissions(mainScriptPath);

        return new TempExecutionEnvironment(PYTHON_PROVIDED_SCRIPT_FILENAME, PYTHON_MAIN_SCRIPT_FILENAME, tempDirPath);
    }

    private TempEvalEnvironment generateTempResourcesForEval() throws IOException {
        Path tempDirPath = createTempDirectory("python_expression");

        // Handle eval.py
        Path evalScriptPath = get(tempDirPath.toString(), PYTHON_EVAL_SCRIPT_FILENAME);
        Files.write(evalScriptPath, resourceScriptCache.loadEvalScriptAsBytes());
        applyFilePermissions(evalScriptPath);

        return new TempEvalEnvironment(PYTHON_EVAL_SCRIPT_FILENAME, tempDirPath);
    }

    private String generateEvalPayload(String expression, String prepareEnvironmentScript,
                                       Map<String, Serializable> context) throws JsonProcessingException {
        Map<String, Serializable> payload = newHashMapWithExpectedSize(3);
        payload.put("expression", expression);
        payload.put("envSetup", prepareEnvironmentScript);
        payload.put("context", (Serializable) context);
        return objectMapper.writeValueAsString(payload);
    }

    private String generateExecutionPayload(String userScript, Map<String, Serializable> inputs) throws JsonProcessingException {
        HashMap<String, String> parsedInputs = newHashMapWithExpectedSize(inputs.size());
        for (Entry<String, Serializable> entry : inputs.entrySet()) {
            parsedInputs.put(entry.getKey(), entry.getValue().toString());
        }

        Map<String, Serializable> payload = newHashMapWithExpectedSize(2);
        payload.put("script_name", removeExtension(userScript));
        payload.put("inputs", parsedInputs);
        return objectMapper.writeValueAsString(payload);
    }

    private String formatException(String exception, List<String> traceback) {
        return CollectionUtils.isEmpty(traceback) ? exception
                : removeFileName(traceback.get(traceback.size() - 1)) + ", " + exception;
    }

    private String removeFileName(String trace) {
        int pythonFileNameIndex = trace.indexOf(PYTHON_FILENAME_SCRIPT_EXTENSION);
        return trace.substring(pythonFileNameIndex + PYTHON_FILENAME_DELIMITERS);
    }

    private static class TempEnvironment {
        final String mainScriptName;
        final Path parentFolder;

        private TempEnvironment(String mainScriptName, Path parentFolder) {
            this.mainScriptName = mainScriptName;
            this.parentFolder = parentFolder;
        }
    }

    private static class TempExecutionEnvironment extends TempEnvironment {
        private final String userScriptName;

        private TempExecutionEnvironment(String userScriptName, String mainScriptName, Path parentFolder) {
            super(mainScriptName, parentFolder);
            this.userScriptName = userScriptName;
        }
    }

    private static class TempEvalEnvironment extends TempEnvironment {
        private TempEvalEnvironment(String mainScriptName, Path parentFolder) {
            super(mainScriptName, parentFolder);
        }
    }
}
