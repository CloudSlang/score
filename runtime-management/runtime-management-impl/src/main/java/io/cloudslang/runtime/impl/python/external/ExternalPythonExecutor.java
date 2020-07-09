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
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMapWithExpectedSize;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.createTempDirectory;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.getFileAttributeView;
import static java.nio.file.Files.isRegularFile;
import static java.nio.file.Files.newBufferedWriter;
import static java.nio.file.Files.setPosixFilePermissions;
import static java.nio.file.Files.walk;
import static java.nio.file.StandardOpenOption.CREATE_NEW;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.apache.commons.io.FilenameUtils.removeExtension;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

public class ExternalPythonExecutor {
    private static final String PYTHON_SUFFIX = ".py";
    private static final String PYTHON_PROVIDED_SCRIPT_FILENAME = "script.py";
    private static final String PYTHON_EVAL_SCRIPT_FILENAME = "eval.py";
    private static final String PYTHON_MAIN_SCRIPT_FILENAME = "main.py";
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
            tempEvalEnvironment = generateTempEvalResources();
            String payload = generateEvalPayload(expression, prepareEnvironmentScript, context);
            addFilePermissions(tempEvalEnvironment.parentFolder);

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

    private void addFilePermissions(Path path) throws IOException {
        final Iterator<Path> iterator = walk(path).iterator();
        if (SystemUtils.IS_OS_WINDOWS) {
            applyWindowsFilePermissionsForChildren(iterator);
        } else {
            applyPosixFilePermissionsForChildren(iterator);
        }
    }

    private void applyWindowsFilePermissionsForChildren(Iterator<Path> iterator) throws IOException {
        while (iterator.hasNext()) {
            Path childFile = iterator.next();
            if (isRegularFile(childFile)) {
                DosFileAttributeView attributeView = getFileAttributeView(childFile, DosFileAttributeView.class);
                if (attributeView != null) {
                    attributeView.setReadOnly(true);
                }
            }
        }
    }

    private void applyPosixFilePermissionsForChildren(Iterator<Path> iterator) throws IOException {
        final EnumSet<PosixFilePermission> filePermissions = EnumSet.of(PosixFilePermission.OWNER_READ);
        while (iterator.hasNext()) {
            Path childPath = iterator.next();
            if (isRegularFile(childPath)) {
                setPosixFilePermissions(childPath, filePermissions);
            }
        }
    }

    private String checkPythonPath() {
        String pythonPath = System.getProperty("python.path");
        if (isNotEmpty(pythonPath) && exists(Paths.get(pythonPath))) {
            return pythonPath;
        } else {
            throw new IllegalArgumentException("Missing or invalid python path");
        }
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

            if (!StringUtils.isEmpty(exception)) {
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
        PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(process.getOutputStream(), UTF_8));
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
            IOUtils.copy(process.getErrorStream(), writer, UTF_8);
            logger.error(writer.toString());
            throw new RuntimeException("Execution returned non 0 result");
        }
        return returnResult.toString();
    }

    private ProcessBuilder preparePythonProcess(TempEnvironment executionEnvironment, String pythonPath) {
        ProcessBuilder processBuilder = new ProcessBuilder(
                newArrayList(Paths.get(pythonPath, "python").toString(),
                        executionEnvironment.parentFolder.resolve(executionEnvironment.mainScriptName).toString()));
        processBuilder.environment().clear();
        processBuilder.directory(executionEnvironment.parentFolder.toFile());
        return processBuilder;
    }

    private TempExecutionEnvironment generateTempExecutionResources(String script) throws IOException {
        Path execTempDirectory = createTempDirectory("python_execution");
        Path tempUserScript = execTempDirectory.resolve(PYTHON_PROVIDED_SCRIPT_FILENAME);
        try (BufferedWriter bufferedWriter = newBufferedWriter(tempUserScript, UTF_8, CREATE_NEW)) {
            bufferedWriter.write(script);
        }

        Path mainScriptPath = execTempDirectory.resolve(PYTHON_MAIN_SCRIPT_FILENAME);
        try (InputStream mainPyResourceStream = ExternalPythonExecutor.class.getClassLoader()
                .getResourceAsStream(PYTHON_MAIN_SCRIPT_FILENAME)) {
            Files.copy(mainPyResourceStream, mainScriptPath);
        }

        return new TempExecutionEnvironment(PYTHON_PROVIDED_SCRIPT_FILENAME,
                PYTHON_MAIN_SCRIPT_FILENAME,
                execTempDirectory);
    }

    private TempEvalEnvironment generateTempEvalResources() throws IOException {
        Path execTempDirectory = createTempDirectory("python_expression");
        Path evalScriptPath = execTempDirectory.resolve(PYTHON_EVAL_SCRIPT_FILENAME);

        try (InputStream evalScriptResourceStream = ExternalPythonExecutor.class.getClassLoader()
                .getResourceAsStream(PYTHON_EVAL_SCRIPT_FILENAME)) {
            Files.copy(evalScriptResourceStream, evalScriptPath);
        }

        return new TempEvalEnvironment(PYTHON_EVAL_SCRIPT_FILENAME, execTempDirectory);
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
        if (isNotEmpty(traceback)) {
            return removeFileName(traceback.get(traceback.size() - 1)) + ", " + exception;
        } else {
            return exception;
        }
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
