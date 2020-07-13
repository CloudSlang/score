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

import io.cloudslang.runtime.external.ResourceScriptCache;

import java.io.IOException;
import java.io.InputStream;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.io.IOUtils.toByteArray;


public class ResourceScriptCacheImpl implements ResourceScriptCache {

    private static final byte[] execScriptBytes;
    private static final byte[] evalScriptBytes;

    static {
        execScriptBytes = loadScriptFromFile(PYTHON_MAIN_SCRIPT_FILENAME);
        evalScriptBytes = loadScriptFromFile(PYTHON_EVAL_SCRIPT_FILENAME);
    }

    private static byte[] loadScriptFromFile(String resourceName) {
        try (InputStream stream = ResourceScriptCacheImpl.class.getClassLoader().getResourceAsStream(resourceName)) {
            return toByteArray(requireNonNull(stream, "Could not locate resource '" + resourceName + "'"));
        } catch (IOException ioEx) {
            throw new RuntimeException("Could not load resource '" + resourceName + "': ", ioEx);
        }
    }

    @Override
    public byte[] loadExecScriptAsBytes() {
        return execScriptBytes;
    }

    @Override
    public byte[] loadEvalScriptAsBytes() {
        return evalScriptBytes;
    }

}
