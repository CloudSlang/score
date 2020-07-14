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

import java.io.IOException;
import java.io.InputStream;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.io.IOUtils.toByteArray;


public class ResourceScriptResolver {

    private static final byte[] execScriptBytes;
    private static final byte[] evalScriptBytes;

    static {
        execScriptBytes = loadScriptFromResource("main.py");
        evalScriptBytes = loadScriptFromResource("eval.py");
    }

    private static byte[] loadScriptFromResource(String resourceName) {
        try (InputStream stream = ResourceScriptResolver.class.getClassLoader().getResourceAsStream(resourceName)) {
            return toByteArray(requireNonNull(stream, "Could not locate resource '" + resourceName + "'"));
        } catch (IOException ioEx) {
            throw new RuntimeException("Could not load resource '" + resourceName + "': ", ioEx);
        }
    }

    public static byte[] loadExecScriptAsBytes() {
        return execScriptBytes;
    }

    public static byte[] loadEvalScriptAsBytes() {
        return evalScriptBytes;
    }

}
