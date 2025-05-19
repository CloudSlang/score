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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.cloudslang.runtime.api.python.ExternalPythonProcessRunService;
import io.cloudslang.runtime.api.python.PythonEvaluationResult;
import io.cloudslang.runtime.api.python.PythonExecutionResult;
import io.cloudslang.runtime.api.python.executor.entities.EvaluationResults;
import io.cloudslang.runtime.impl.python.external.model.TempEnvironment;
import io.cloudslang.runtime.impl.python.external.model.TempExecutionEnvironment;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeoutException;

import static io.cloudslang.runtime.impl.python.external.EnvVariablesUtil.processEnvironmentVariablesAllowedForPython;
import static io.cloudslang.runtime.impl.python.external.ExternalPythonExecutionEngine.COMPLETABLE_EXECUTOR_STRATEGY;
import static io.cloudslang.runtime.impl.python.external.ResourceScriptResolver.loadEvalScriptAsString;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.apache.commons.io.FileUtils.deleteQuietly;

public class ExternalPythonExecutorCompletableFutureTimeout implements ExternalPythonProcessRunService {

    private static final Logger logger = LogManager.getLogger(ExternalPythonExecutorCompletableFutureTimeout.class);
    private static final String PYTHON_SCRIPT_FILENAME = "script";
    private static final String EVAL_PY = "eval.py";
    private static final String MAIN_PY = "main.py";
    private static final String PYTHON_SUFFIX = ".py";
    private static final long EXECUTION_TIMEOUT = Long.getLong("python.timeout", 30) * 60 * 1000;
    private static final long EVALUATION_TIMEOUT = Long.getLong("python.evaluation.timeout", 3) * 60 * 1000;
    private static final int ENGINE_EXECUTOR_THREAD_COUNT = Integer.getInteger("python.executor.threadCount", 10);
    private static final int TEST_EXECUTOR_THREAD_COUNT = Integer.getInteger("test.executor.threadCount", 3);
    private static final String PYTHON_FILENAME_SCRIPT_EXTENSION = ".py\"";
    private static final int PYTHON_FILENAME_DELIMITERS = 6;
    private static final ObjectMapper objectMapper;
    private static final ExecutorService engineExecutorService;
    private static final ExecutorService testExecutorService;

    static {
        JsonFactory factory = new JsonFactory();
        factory.enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES);
        factory.enable(JsonWriteFeature.ESCAPE_NON_ASCII.mappedFeature());
        factory.setStreamReadConstraints(StreamReadConstraints.builder()
                .maxNestingDepth(DEFAULT_MAX_DEPTH)
                .maxNumberLength(DEFAULT_MAX_NUM_LEN)
                .maxStringLength(DEFAULT_MAX_STRING_LEN)
                .build());
        objectMapper = new ObjectMapper(factory);
    }

    static {
        final ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNameFormat("python-engine-%d")
                .setDaemon(true)
                .build();
        engineExecutorService = Executors.newFixedThreadPool(ENGINE_EXECUTOR_THREAD_COUNT, threadFactory);

    }

    static {
        final ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNameFormat("python-test-%d")
                .setDaemon(true)
                .build();
        testExecutorService = Executors.newFixedThreadPool(TEST_EXECUTOR_THREAD_COUNT, threadFactory);
    }

    @Override
    public PythonExecutionResult exec(String script, Map<String, Serializable> inputs) {
        TempExecutionEnvironment tempExecutionEnvironment = null;
        try {
            String pythonPath = checkPythonPath();
            tempExecutionEnvironment = generateTempResourcesForExec(script);
            String payload = generatePayloadForExec(tempExecutionEnvironment.getUserScriptName(), inputs);
            addFilePermissions(tempExecutionEnvironment.getParentFolder());

            return runPythonExecutionProcess(pythonPath, payload, tempExecutionEnvironment);

        } catch (IOException e) {
            String message = "Failed to generate execution resources";
            logger.error(message, e);
            throw new RuntimeException(message);
        } finally {
            if ((tempExecutionEnvironment != null) && !deleteQuietly(tempExecutionEnvironment.getParentFolder())) {
                logger.warn(String.format("Failed to cleanup python execution resources {%s}",
                        tempExecutionEnvironment.getParentFolder()));
            }
        }
    }

    @Override
    public PythonEvaluationResult eval(String expression, String prepareEnvironmentScript,
            Map<String, Serializable> context) {
        return getPythonEvaluationResult(expression, prepareEnvironmentScript, context, EVALUATION_TIMEOUT,
                engineExecutorService);
    }

    @Override
    public PythonEvaluationResult test(String expression, String prepareEnvironmentScript,
            Map<String, Serializable> context, long timeout) {
        return getPythonEvaluationResult(expression, prepareEnvironmentScript, context, timeout, testExecutorService);
    }

    private PythonEvaluationResult getPythonEvaluationResult(String expression, String prepareEnvironmentScript,
            Map<String, Serializable> context, long evaluationTimeout, ExecutorService executorService) {
        try {
            String pythonPath = checkPythonPath();
            String payload = generatePayloadForEval(expression, prepareEnvironmentScript, context);
            return runPythonEvalProcess(pythonPath, payload, context, evaluationTimeout,
                    executorService);

        } catch (IOException e) {
            String message = "Failed to evaluate Python expression";
            logger.error(message, e);
            throw new RuntimeException(message);
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

    private Document parseScriptExecutionResult(String scriptExecutionResult)
            throws IOException, ParserConfigurationException, SAXException {
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
            String returnResult = getResult(payload, processBuilder, EXECUTION_TIMEOUT, engineExecutorService);
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
        } catch (IOException | InterruptedException | SAXException | ParserConfigurationException | ExecutionException e) {
            logger.error("Failed to run script. ", e.getCause() != null ? e.getCause() : e);
            throw new RuntimeException("Failed to run script.");
        }
    }

    private PythonEvaluationResult runPythonEvalProcess(String pythonPath, String payload,
            Map<String, Serializable> context, long timeout, ExecutorService executorService) {

        ProcessBuilder processBuilder = preparePythonProcessForEval(pythonPath, loadEvalScriptAsString());

        try {
            String returnResult = getResult(payload, processBuilder, timeout, executorService);

            EvaluationResults scriptResults = objectMapper.readValue(returnResult, EvaluationResults.class);
            String exception = scriptResults.getException();
            if (!StringUtils.isEmpty(exception)) {
                logger.error(String.format("Failed to execute script {%s}", exception));
                throw new ExternalPythonEvalException(exception);
            }
            context.put("accessed_resources_set", (Serializable) scriptResults.getAccessedResources());
            //noinspection unchecked
            return new PythonEvaluationResult(processReturnResult(scriptResults), context);
        } catch (IOException | InterruptedException | ExecutionException e) {
            logger.error("Failed to run script. ", e.getCause() != null ? e.getCause() : e);
            throw new RuntimeException("Failed to run script.");
        }
    }


    private Serializable processReturnResult(EvaluationResults results) throws JsonProcessingException {
        EvaluationResults.ReturnType returnType = results.getReturnType();
        if (returnType == null) {
            throw new RuntimeException("Missing return type for return result.");
        }
        switch (returnType) {
            case BOOLEAN:
                return Boolean.valueOf(results.getReturnResult());
            case INTEGER:
                return Integer.valueOf(results.getReturnResult());
            case LIST:
                return objectMapper.readValue(results.getReturnResult(), new TypeReference<ArrayList<Serializable>>() {
                });
            default:
                return results.getReturnResult();
        }
    }

    private String getResult(final String payload, final ProcessBuilder processBuilder, final long timeoutPeriodMillis,
            ExecutorService executorService) throws InterruptedException, ExecutionException {
        ExternalPythonEvaluationSupplier supplier = new ExternalPythonEvaluationSupplier(processBuilder, payload);
        try {
            return supplyAsync(supplier, executorService).get(timeoutPeriodMillis, MILLISECONDS);
        } catch (TimeoutException timeoutException) {
            supplier.destroyProcess();
            throw new RuntimeException("Python timeout of " + timeoutPeriodMillis + " millis has been reached");
        }
    }

    private ProcessBuilder preparePythonProcess(TempEnvironment executionEnvironment, String pythonPath) {
        ProcessBuilder processBuilder = new ProcessBuilder(Arrays.asList(Paths.get(pythonPath, "python").toString(),
                Paths.get(executionEnvironment.getParentFolder().toString(), executionEnvironment.getMainScriptName())
                        .toString()));
        processEnvironmentVariablesAllowedForPython(processBuilder);

        processBuilder.directory(executionEnvironment.getParentFolder());
        return processBuilder;
    }

    private ProcessBuilder preparePythonProcessForEval(String pythonPath, String evalPyCode) {
        // Must make sure that the eval.py evalPyCode does not contain the " character in its contents
        // otherwise an error will be thrown when running python -c "import json\nimport sys..."
        // code from eval.py separated using \n character
        // Also do not use comments for the same reason in eval.py
        // Use 'string' for Python strings instead of "string"
        ProcessBuilder processBuilder = new ProcessBuilder(Arrays.asList(
                Paths.get(pythonPath, "python").toString(),
                "-c",
                evalPyCode)
        );
        processEnvironmentVariablesAllowedForPython(processBuilder);
        return processBuilder;
    }

    private TempExecutionEnvironment generateTempResourcesForExec(String script) throws IOException {
        Path execTempDirectory = Files.createTempDirectory("python_execution");
        File tempUserScript = File.createTempFile(PYTHON_SCRIPT_FILENAME, PYTHON_SUFFIX, execTempDirectory.toFile());
        FileUtils.writeStringToFile(tempUserScript, script, UTF_8);

        File mainScriptFile = new File(execTempDirectory.toString(), MAIN_PY);
        FileUtils.writeByteArrayToFile(mainScriptFile, ResourceScriptResolver.loadExecScriptAsBytes());

        String tempUserScriptName = FilenameUtils.getName(tempUserScript.toString());
        return new TempExecutionEnvironment(tempUserScriptName, MAIN_PY, execTempDirectory.toFile());
    }

    private String generatePayloadForEval(String expression, String prepareEnvironmentScript,
            Map<String, Serializable> context) throws JsonProcessingException {
        HashMap<String, Serializable> payload = new HashMap<>(4);
        payload.put("expression", expression);
        payload.put("envSetup", prepareEnvironmentScript);
        payload.put("context", (Serializable) context);
        return objectMapper.writeValueAsString(payload);
    }

    private String generatePayloadForExec(String userScript, Map<String, Serializable> inputs) throws
            JsonProcessingException {
        HashMap<String, String> parsedInputs = new HashMap<>();
        for (Entry<String, Serializable> entry : inputs.entrySet()) {
            parsedInputs.put(entry.getKey(), entry.getValue().toString());
        }

        Map<String, Serializable> payload = new HashMap<>(3);
        payload.put("script_name", FilenameUtils.removeExtension(userScript));
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

    @Override
    public String getStrategyName() {
        return COMPLETABLE_EXECUTOR_STRATEGY;
    }
}