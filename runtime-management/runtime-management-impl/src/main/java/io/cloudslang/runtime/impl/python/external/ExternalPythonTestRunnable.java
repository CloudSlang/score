package io.cloudslang.runtime.impl.python.external;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicReference;

import static java.nio.charset.StandardCharsets.UTF_8;


public class ExternalPythonTestRunnable implements Runnable {

    private final ProcessBuilder processBuilder;
    private final String payload;

    private final AtomicReference<Process> processRef;
    private final AtomicReference<String> result;
    private final AtomicReference<RuntimeException> exception;

    public ExternalPythonTestRunnable(ProcessBuilder processBuilder, String payload) {
        this.processBuilder = processBuilder;
        this.payload = payload;

        this.processRef = new AtomicReference<>();
        this.result = new AtomicReference<>();
        this.exception = new AtomicReference<>();
    }

    @Override
    public void run() {
        try {
            Process process = processBuilder.start();
            processRef.set(process);
            PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(process.getOutputStream(), UTF_8));
            printWriter.println(payload);
            printWriter.flush();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            StringBuilder returnResult = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                returnResult.append(line);
            }
            String retValue = returnResult.toString();
            result.set(retValue);
            exception.set(null);
        } catch (IOException ioException) {
            result.set(null);
            exception.set(new RuntimeException("Script execution failed: ", ioException));
        }
    }

    public String getResult() {
        return result.get();
    }

    public RuntimeException getException() {
        return exception.get();
    }

    public void destroyProcess() {
        Process process = processRef.get();
        if (process != null) {
            try {
                process.destroy();
            } catch (Exception ignore) {
            }
        }
    }
}
