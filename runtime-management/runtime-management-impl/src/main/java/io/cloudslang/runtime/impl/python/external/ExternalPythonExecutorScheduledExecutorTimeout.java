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
import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.cloudslang.runtime.api.python.ExternalPythonProcessRunService;
import io.cloudslang.runtime.api.python.PythonEvaluationResult;
import io.cloudslang.runtime.api.python.PythonExecutionResult;
import io.cloudslang.runtime.impl.python.external.model.TempEnvironment;
import io.cloudslang.runtime.impl.python.external.model.TempEvalEnvironment;
import io.cloudslang.runtime.impl.python.external.model.TempExecutionEnvironment;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
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
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;

import static io.cloudslang.runtime.impl.python.external.ExternalPythonExecutionEngine.SCHEDULED_EXECUTOR_STRATEGY;
import static io.cloudslang.runtime.impl.python.external.ResourceScriptResolver.loadEvalScriptAsString;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.apache.commons.io.FileUtils.deleteQuietly;

public class ExternalPythonExecutorScheduledExecutorTimeout implements ExternalPythonProcessRunService {

    private static final Logger logger = LogManager.getLogger(ExternalPythonExecutorScheduledExecutorTimeout.class);
    private static final String PYTHON_SCRIPT_FILENAME = "script";
    private static final String EVAL_PY = "eval.py";
    private static final String MAIN_PY = "main.py";
    private static final String PYTHON_SUFFIX = ".py";
    private static final long EXECUTION_TIMEOUT = Long.getLong("python.timeout", 30) * 60 * 1000;
    private static final long EVALUATION_TIMEOUT = Long.getLong("python.evaluation.timeout", 3) * 60 * 1000;
    private static final String PYTHON_FILENAME_SCRIPT_EXTENSION = ".py\"";
    private static final int PYTHON_FILENAME_DELIMITERS = 6;
    private static final ObjectMapper objectMapper;
    private static final ScheduledThreadPoolExecutor timeoutScheduledExecutor;
    private static final ThreadFactory testThreadFactory;

    static {
        JsonFactory factory = new JsonFactory();
        factory.enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES);
        factory.enable(JsonWriteFeature.ESCAPE_NON_ASCII.mappedFeature());
        objectMapper = new ObjectMapper(factory);
    }

    static {
        final ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNameFormat("python-timeout-%d")
                .setDaemon(true)
                .build();

        int nrThreads = Integer.getInteger("python.timeout.threadCount", 1);
        ScheduledThreadPoolExecutor scheduledExecutor = new ScheduledThreadPoolExecutor(nrThreads, threadFactory);
        scheduledExecutor.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
        scheduledExecutor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        scheduledExecutor.setRemoveOnCancelPolicy(true);
        scheduledExecutor.setRejectedExecutionHandler(new AbortPolicy());
        timeoutScheduledExecutor = scheduledExecutor;
    }

    static {
        testThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat("python-test-%d")
                .setDaemon(true)
                .build();
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
        return getPythonEvaluationResult(expression, prepareEnvironmentScript, context);
    }

    @Override
    public PythonEvaluationResult test(String expression, String prepareEnvironmentScript,
                                       Map<String, Serializable> context, long timeout) {
        return getPythonTestResult(expression, prepareEnvironmentScript, context, timeout);
    }

    private PythonEvaluationResult getPythonTestResult(String expression, String prepareEnvironmentScript,
                                                       Map<String, Serializable> context, long evaluationTimeout) {
        try {
            String pythonPath = checkPythonPath();
            String payload = generatePayloadForEval(expression, prepareEnvironmentScript, context);
            return runPythonTestProcess(pythonPath, payload, context, evaluationTimeout);

        } catch (IOException e) {
            String message = "Failed to generate execution resources";
            logger.error(message, e);
            throw new RuntimeException(message);
        }
    }

    private PythonEvaluationResult getPythonEvaluationResult(String expression, String prepareEnvironmentScript,
                                                             Map<String, Serializable> context) {
        try {
            String pythonPath = checkPythonPath();
            String payload = generatePayloadForEval(expression, prepareEnvironmentScript, context);
            return runPythonEvalProcess(pythonPath, payload, context,
                    ExternalPythonExecutorScheduledExecutorTimeout.EVALUATION_TIMEOUT);

        } catch (IOException e) {
            String message = "Failed to generate execution resources";
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
            String returnResult = getResult(payload, processBuilder, EXECUTION_TIMEOUT);
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
        } catch (IOException | SAXException | ParserConfigurationException e) {
            logger.error("Failed to run script. ", e.getCause() != null ? e.getCause() : e);
            throw new RuntimeException("Failed to run script.");
        }
    }

    private PythonEvaluationResult runPythonEvalProcess(String pythonPath, String payload,
                                                        Map<String, Serializable> context, long timeout) {

        ProcessBuilder processBuilder = preparePythonProcessForEval(pythonPath, loadEvalScriptAsString());

        try {
            String returnResult = getResult(payload, processBuilder, timeout);

            EvaluationResults scriptResults = objectMapper.readValue(returnResult, EvaluationResults.class);
            String exception = scriptResults.getException();
            if (!StringUtils.isEmpty(exception)) {
                logger.error(String.format("Failed to execute script {%s}", exception));
                throw new ExternalPythonEvalException(exception);
            }
            context.put("accessed_resources_set", (Serializable) scriptResults.getAccessedResources());
            //noinspection unchecked
            return new PythonEvaluationResult(processReturnResult(scriptResults), context);
        } catch (IOException ioException) {
            logger.error("Failed to run script. ",
                    ioException.getCause() != null ? ioException.getCause() : ioException);
            throw new RuntimeException("Failed to run script.");
        }
    }

    private PythonEvaluationResult runPythonTestProcess(String pythonPath, String payload,
                                                        Map<String, Serializable> context, long timeout) {

        ProcessBuilder processBuilder = preparePythonProcessForEval(pythonPath, loadEvalScriptAsString());
        try {
            final ExternalPythonTestRunnable testRunnable = new ExternalPythonTestRunnable(processBuilder, payload);
            Thread threadTest = testThreadFactory.newThread(testRunnable);
            threadTest.start();
            threadTest.join(timeout);

            if (threadTest.isAlive()) { // Test python script timed out
                testRunnable.destroyProcess();
                throw new RuntimeException("Python timeout of " + timeout + " millis has been reached");
            }

            // Test python script encountered an exception during its execution
            RuntimeException scriptExc = testRunnable.getException();
            if (scriptExc != null) {
                throw scriptExc;
            }

            // Test python script finished successfully
            String returnResult = testRunnable.getResult();

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

    private String getResult(final String payload, final ProcessBuilder processBuilder, final long timeoutPeriodMillis) {
        ScheduledFuture<?> scheduledFuture = null;

        final MutableBoolean wasProcessDestroyed = new MutableBoolean(false);
        try {
            final Process process = processBuilder.start();
            final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            ExternalPythonTimeoutRunnable runnable = new ExternalPythonTimeoutRunnable(wasProcessDestroyed, process);
            scheduledFuture = timeoutScheduledExecutor.schedule(runnable, timeoutPeriodMillis, MILLISECONDS);

            PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(process.getOutputStream(), UTF_8));
            printWriter.println(payload);
            printWriter.flush();

            // Start reading in order to prevent buffer getting full, such that process.waitFor does not deadlock
            String line;
            IOException readExc = null;
            StringBuilder returnResult = new StringBuilder();
            try {
                while ((line = reader.readLine()) != null) {
                    returnResult.append(line);
                }
            } catch (IOException exc) {
                if (wasProcessDestroyed.isTrue()) {
                    throw new RuntimeException("Python timeout of " + timeoutPeriodMillis + " millis has been reached");
                } else {
                    readExc = exc;
                }
            }

            // Wait for the process to terminate naturally or be destroyed from ExternalPythonTimeoutRunnable
            process.waitFor();

            if (wasProcessDestroyed.isTrue() || (readExc != null)) { // Timeout or script execution exception
                if (wasProcessDestroyed.isTrue()) {
                    throw new RuntimeException("Python timeout of " + timeoutPeriodMillis + " millis has been reached");
                } else {
                    throw new RuntimeException("Script execution failed: ", readExc);
                }
            }

            // Continue reading leftover, if required
            while ((line = reader.readLine()) != null) {
                returnResult.append(line);
            }
            return returnResult.toString();
        } catch (IOException exception) {
            throw new RuntimeException("Script execution failed: ", exception);
        } catch (InterruptedException interruptedException) {
            throw new RuntimeException(interruptedException);
        } finally {
            // Remove from timeoutScheduledExecutor queue, because of setRemoveOnCancelPolicy(true)
            if (scheduledFuture != null) {
                scheduledFuture.cancel(false);
            }
        }
    }

    private ProcessBuilder preparePythonProcess(TempEnvironment executionEnvironment, String pythonPath) {
        ProcessBuilder processBuilder = new ProcessBuilder(Arrays.asList(Paths.get(pythonPath, "python").toString(),
                Paths.get(executionEnvironment.getParentFolder().toString(), executionEnvironment.getMainScriptName())
                        .toString()));
        processBuilder.environment().clear();
        processBuilder.directory(executionEnvironment.getParentFolder());
        return processBuilder;
    }

    private ProcessBuilder preparePythonProcessForEval(String pythonPath, String evalPyCode) {
        // Must make sure that the eval.py evalPyCode does not contain the " character in its contents
        // otherwise an error will be thrown
        // Use 'string' for Python strings instead of "string"
        ProcessBuilder processBuilder = new ProcessBuilder(Arrays.asList(
                Paths.get(pythonPath, "python").toString(),
                "-c",
                evalPyCode)
        );
        processBuilder.environment().clear();
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
        return SCHEDULED_EXECUTOR_STRATEGY;
    }
}