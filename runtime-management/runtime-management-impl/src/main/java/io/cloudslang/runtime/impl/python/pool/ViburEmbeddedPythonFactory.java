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
package io.cloudslang.runtime.impl.python.pool;


import io.cloudslang.runtime.impl.python.EmbeddedPythonExecutorWrapper;
import org.vibur.objectpool.PoolObjectFactory;

public class ViburEmbeddedPythonFactory implements PoolObjectFactory<EmbeddedPythonExecutorWrapper> {

    public ViburEmbeddedPythonFactory() {
    }

    @Override
    public EmbeddedPythonExecutorWrapper create() {
        return new EmbeddedPythonExecutorWrapper();
    }

    @Override
    public boolean readyToTake(EmbeddedPythonExecutorWrapper pythonExecutorWrapper) {
        return true;
    }

    @Override
    public boolean readyToRestore(EmbeddedPythonExecutorWrapper pythonExecutorWrapper) {
        return true;
    }

    @Override
    public void destroy(EmbeddedPythonExecutorWrapper pythonExecutorWrapper) {
        pythonExecutorWrapper.close();
    }

}
