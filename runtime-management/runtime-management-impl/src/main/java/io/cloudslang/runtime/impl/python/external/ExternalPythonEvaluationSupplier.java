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


import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ExternalPythonEvaluationSupplier implements Supplier<byte[]> {

    private final AtomicReference<Process> processRef;
    private final ProcessBuilder processBuilder;
    private final String payload;

    public ExternalPythonEvaluationSupplier(ProcessBuilder processBuilder,
                                            String payload) {
        this.processRef = new AtomicReference<>();
        this.processBuilder = processBuilder;
        this.payload = payload;
    }


    @Override
    public byte[] get() {
        try {
            Process process = processBuilder.start();
            this.processRef.set(process);
            PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(process.getOutputStream(), UTF_8));
            printWriter.println(payload);
            printWriter.flush();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            IOUtils.copy(process.getInputStream(), outputStream);
            return outputStream.toByteArray();
        } catch (IOException ioException) {
            throw new RuntimeException("Script execution failed: ", ioException);
        }
    }


    public void destroyProcess() {
        try {
            Process process = processRef.get();
            if (process != null) {
                process.destroy();
            }
        } catch (Exception ignored) {
        }
    }

}
