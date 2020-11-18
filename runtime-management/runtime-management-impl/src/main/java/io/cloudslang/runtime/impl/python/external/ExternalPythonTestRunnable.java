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
