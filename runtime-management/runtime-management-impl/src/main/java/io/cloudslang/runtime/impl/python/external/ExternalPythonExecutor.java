package io.cloudslang.runtime.impl.python.external;

import io.cloudslang.runtime.api.python.PythonExecutionResult;
import io.cloudslang.runtime.impl.Executor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.logging.Logger;

public class ExternalPythonExecutor implements Executor {
    private static final String PYTHON_SCRIPT_FILENAME = "script";
    private static final String PYTHON_WRAPPER_FILENAME = "wrapper";
    private static final String PYTHON_MAIN_FILENAME = "main";
    private static final String PYTHON_SUFFIX = ".py";
    private static final Logger logger = Logger.getLogger(ExternalPythonExecutor.class.getName());

    public PythonExecutionResult exec(String script, Map<String, String> inputs) {
        TempExecutionEnvironment tempExecutionEnvironment = null;
        try {
            String pythonPath = System.getProperty("python_path");
            if (pythonPath.isEmpty()) {
                throw new IllegalArgumentException("Missing python path");
            }
            tempExecutionEnvironment = generateTempExecutionResources(script);
            PythonExecutionResult pythonExecutionResult = runPythonProcess(pythonPath, tempExecutionEnvironment, inputs);

        } catch (IOException e) {
            throw new RuntimeException("Failed to generate execution resources");
        } finally {
            if (tempExecutionEnvironment != null && !FileUtils.deleteQuietly(tempExecutionEnvironment.parentFolder)
                    && tempExecutionEnvironment.parentFolder != null) {
                logger.warning(String.format("Failed to cleanup python execution resources {%s}", tempExecutionEnvironment.parentFolder));
            }
        }
        return new PythonExecutionResult(null);
    }

    private PythonExecutionResult runPythonProcess(String pythonPath, TempExecutionEnvironment executionEnvironment, Map<String, String> inputs) throws IOException {
        String payload = "{" +
                "        \"script_name\": \"" + executionEnvironment.userScript.toString().replace("\\", "\\\\") +
                "    }\\n";
        ProcessBuilder processBuilder = new ProcessBuilder(Arrays.asList(pythonPath + "python", "-i",
                executionEnvironment.mainScript.toPath().toString()));

        try {
            Process process = processBuilder.start();
            final StringWriter outputWriter = new StringWriter();
            final StringWriter errorWriter = new StringWriter();
//



            new Thread(() -> {
                try {
                    IOUtils.copy(process.getInputStream(), outputWriter);
                } catch (IOException e) {
                    throw new RuntimeException("ceva");
                }
            }).start();

            new Thread(() -> {
                try {
                    IOUtils.copy(process.getErrorStream(), errorWriter);
                } catch (IOException e) {
                    throw new RuntimeException("ceva");
                }
            }).start();

            PrintWriter printWriter = new PrintWriter(process.getOutputStream());
            printWriter.write(payload);
            printWriter.flush();

            String processOutput = outputWriter.toString();
            String processError = errorWriter.toString();

            int exitCode = process.waitFor();
        } catch (IOException | InterruptedException e) {
            return new PythonExecutionResult(null);
        }
        return new PythonExecutionResult(null);

    }

    public static void main(String[] args) {
        System.setProperty("python_path", "C:\\Users\\bajzat\\AppData\\Local\\Programs\\Python\\Python38-32\\");
        new ExternalPythonExecutor().exec("def ceva", null);
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

    private TempExecutionEnvironment generateTempExecutionResources(String script) throws IOException {
        Path execTempDirectory = Files.createTempDirectory("python_execution");
        File tempUserScript = File.createTempFile(PYTHON_SCRIPT_FILENAME, PYTHON_SUFFIX, execTempDirectory.toFile());
        FileUtils.writeStringToFile(tempUserScript, script);

        ClassLoader classLoader = ExternalPythonExecutor.class.getClassLoader();
        Path wrapperScriptPath = Paths.get(execTempDirectory.toString(), PYTHON_WRAPPER_FILENAME + PYTHON_SUFFIX);
        Path mainScriptPath = Paths.get(execTempDirectory.toString(), PYTHON_MAIN_FILENAME + PYTHON_SUFFIX);

        Files.copy(classLoader.getResourceAsStream(PYTHON_WRAPPER_FILENAME + PYTHON_SUFFIX), wrapperScriptPath);
        Files.copy(classLoader.getResourceAsStream(PYTHON_MAIN_FILENAME + PYTHON_SUFFIX), mainScriptPath);

        return new TempExecutionEnvironment(tempUserScript, mainScriptPath.toFile(), wrapperScriptPath.toFile(),
                execTempDirectory.toFile());
    }

    private class TempExecutionEnvironment {
        private File userScript;
        private File mainScript;
        private File wrapperScript;
        private File parentFolder;

        private TempExecutionEnvironment(File userScript, File mainScript, File wrapperScript, File parentFolder) {
            this.userScript = userScript;
            this.mainScript = mainScript;
            this.wrapperScript = wrapperScript;
            this.parentFolder = parentFolder;
        }
    }
}
