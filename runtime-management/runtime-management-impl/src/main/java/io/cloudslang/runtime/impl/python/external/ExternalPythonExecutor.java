package io.cloudslang.runtime.impl.python.external;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cloudslang.runtime.api.python.PythonExecutionResult;
import io.cloudslang.runtime.impl.Executor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.ArrayUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

public class ExternalPythonExecutor implements Executor {
    private static final String PYTHON_SCRIPT_FILENAME = "script";
    private static final String PYTHON_WRAPPER_FILENAME = "wrapper";
    private static final String PYTHON_UTILS_FILENAME = "utils";
    private static final String PYTHON_MAIN_FILENAME = "main";
    private static final String PYTHON_SUFFIX = ".py";
    private static final Logger logger = Logger.getLogger(ExternalPythonExecutor.class.getName());
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final Lock allocationLock = new ReentrantLock();
    private int allocationCount = 0;
    private boolean executorMarkedForClosure = false;
    private boolean executorClosed = false;

    public PythonExecutionResult exec(String script, Map<String, Serializable> inputs) {
        checkIfAbleToExecute();
        TempExecutionEnvironment tempExecutionEnvironment = null;
        try {
            String pythonPath = System.getProperty("python_path");
            if (pythonPath.isEmpty() || !new File(pythonPath).exists()) {
                throw new IllegalArgumentException("Missing or invalid python path");
            }
            tempExecutionEnvironment = generateTempExecutionResources(script);
            return runPythonProcess(pythonPath, tempExecutionEnvironment, inputs);

        } catch (IOException e) {
            throw new RuntimeException("Failed to generate execution resources");
        } finally {
            if (tempExecutionEnvironment != null && !FileUtils.deleteQuietly(tempExecutionEnvironment.parentFolder)
                    && tempExecutionEnvironment.parentFolder != null) {
                logger.warning(String.format("Failed to cleanup python execution resources {%s}", tempExecutionEnvironment.parentFolder));
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
                throw new RuntimeException("Script return non 0 result");
            }

            @SuppressWarnings("unchecked")
            Map<String, Serializable> executionReturnResults = (Map<String, Serializable>) objectMapper.readValue(process.getInputStream(),
                    Map.class);

            return new PythonExecutionResult(executionReturnResults);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to run script");
        }
    }

    private void checkIfAbleToExecute() {
        if (executorClosed) {
            throw new RuntimeException("Can't run script. The executor has been closed.");
        }
    }

    @Override
    public void allocate() {
        allocationLock.lock();
        try {
            allocationCount++;
        } finally {
            allocationLock.unlock();
        }
    }

    @Override
    public void release() {
        allocationLock.lock();
        try {
            allocationCount--;
            if (executorMarkedForClosure && allocationCount == 0) {
                close();
            }
        } finally {
            allocationLock.unlock();
        }
    }

    @Override
    public void close() {
        allocationLock.lock();
        try {
            executorMarkedForClosure = true;
            if (allocationCount == 0) {
                executorClosed = true;
                logger.info("Python executor successfully closed");
            }
        } finally {
            allocationLock.unlock();
        }
    }

    private ProcessBuilder preparePythonProcess(TempExecutionEnvironment executionEnvironment, String pythonPath) {
        ProcessBuilder processBuilder = new ProcessBuilder(Arrays.asList(pythonPath + "python",
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
        Path wrapperScriptPath = Paths.get(execTempDirectory.toString(), PYTHON_WRAPPER_FILENAME + PYTHON_SUFFIX);
        Path mainScriptPath = Paths.get(execTempDirectory.toString(), PYTHON_MAIN_FILENAME + PYTHON_SUFFIX);
        Path utilsScriptPath = Paths.get(execTempDirectory.toString(), PYTHON_UTILS_FILENAME + PYTHON_SUFFIX);

        Files.copy(classLoader.getResourceAsStream(PYTHON_WRAPPER_FILENAME + PYTHON_SUFFIX), wrapperScriptPath);
        Files.copy(classLoader.getResourceAsStream(PYTHON_MAIN_FILENAME + PYTHON_SUFFIX), mainScriptPath);
        Files.copy(classLoader.getResourceAsStream(PYTHON_UTILS_FILENAME + PYTHON_SUFFIX), utilsScriptPath);

        String tempUserScriptName = FilenameUtils.getName(tempUserScript.toString());
        String mainScriptName = FilenameUtils.getName(mainScriptPath.toString());
        String wrapperScriptName = FilenameUtils.getName(wrapperScriptPath.toString());
        return new TempExecutionEnvironment(tempUserScriptName, mainScriptName, wrapperScriptName, execTempDirectory.toFile());
    }

    private String generatePayload(String userScript, Map<String, Serializable> inputs) throws JsonProcessingException {
        Map<String, Serializable> payload = new HashMap<>();
        payload.put("script_name", FilenameUtils.removeExtension(userScript));
        payload.put("enc_key", "randomstring");
        payload.put("var", ArrayUtils.EMPTY_INT_ARRAY);
        payload.put("inputs", (Serializable) Optional.ofNullable(inputs).orElse(new HashMap<>()));
        HashMap<Object, Object> out = new HashMap<>();
        out.put("ceva", false);
        payload.put("outputs", out);
        return objectMapper.writeValueAsString(payload);
    }

    public static void main(String[] args) {
        System.setProperty("python_path", "C:\\Users\\bajzat\\AppData\\Local\\Programs\\Python\\Python38-32\\");
        new ExternalPythonExecutor().exec("def execute(): \n    print('ceva') \n    return {'ceva': '2'}", null);
    }

    private class TempExecutionEnvironment {
        private String userScriptName;
        private String mainScriptName;
        private String wrapperScriptName;
        private File parentFolder;

        public TempExecutionEnvironment(String userScriptName, String mainScriptName, String wrapperScriptName, File parentFolder) {
            this.userScriptName = userScriptName;
            this.mainScriptName = mainScriptName;
            this.wrapperScriptName = wrapperScriptName;
            this.parentFolder = parentFolder;
        }
    }
}
