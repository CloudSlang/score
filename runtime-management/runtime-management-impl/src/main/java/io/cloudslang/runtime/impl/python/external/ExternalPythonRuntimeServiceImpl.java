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

import com.fasterxml.jackson.core.JsonProcessingException;
import io.cloudslang.runtime.api.python.PythonEvaluationResult;
import io.cloudslang.runtime.api.python.PythonExecutionResult;
import io.cloudslang.runtime.api.python.PythonRuntimeService;
import io.cloudslang.runtime.api.python.enums.PythonStrategy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static io.cloudslang.runtime.api.python.enums.PythonStrategy.PYTHON_SERVER;
import static io.cloudslang.runtime.api.python.enums.PythonStrategy.getPythonStrategy;

public class ExternalPythonRuntimeServiceImpl implements PythonRuntimeService {
    private static final Logger logger = LogManager.getLogger(ExternalPythonRuntimeServiceImpl.class);

    private final Semaphore executionControlSemaphore;

    private final Semaphore testingControlSemaphore;

    private static final PythonStrategy PYTHON_EVALUATOR =
            getPythonStrategy(System.getProperty("python.expressionsEval"), PYTHON_SERVER);

    @Autowired
    private ExternalPythonServerService externalPythonServerService;

    public ExternalPythonRuntimeServiceImpl(Semaphore executionControlSemaphore, Semaphore testingControlSemaphore) {
        this.executionControlSemaphore = executionControlSemaphore;
        this.testingControlSemaphore = testingControlSemaphore;
    }

    @Resource(name = "externalPythonExecutionEngine")
    private ExternalPythonExecutionEngine externalPythonExecutionEngine;

    @Override
    public PythonExecutionResult exec(Set<String> dependencies, String script, Map<String, Serializable> vars) {
        try {
            if (executionControlSemaphore.tryAcquire(1L, TimeUnit.SECONDS)) {
                try {
                    return externalPythonExecutionEngine.exec(dependencies, script, vars);
                } finally {
                    executionControlSemaphore.release();
                }
            } else {
                logger.warn("Maximum number of python processes has been reached. Waiting for a python process to finish. " +
                        "You can configure the number of concurrent python executions by setting " +
                        "'python.concurrent.execution.permits' system property.");
                executionControlSemaphore.acquire();
                try {
                    logger.info("Acquired a permit for a new python process. Continuing with execution...");
                    return externalPythonExecutionEngine.exec(dependencies, script, vars);
                } finally {
                    executionControlSemaphore.release();
                }
            }
        } catch (InterruptedException ie) {
            throw new ExternalPythonScriptException("Execution was interrupted while waiting for a python permit.");
        }
    }

    @Override
    public PythonEvaluationResult eval(String prepareEnvironmentScript, String script, Map<String, Serializable> vars) {
        if (PYTHON_SERVER.equals(PYTHON_EVALUATOR)) {
            try {
                return externalPythonServerService.evalOnExternalPythonServer(script, prepareEnvironmentScript, vars);
            } catch (JsonProcessingException ie) {
                logger.error(ie);
                throw new ExternalPythonScriptException("Execution was interrupted while waiting for a python permit.");
            }

            //case PYTHON.equals(PYTHON_EVALUATOR)
        } else {
            try {
                if (executionControlSemaphore.tryAcquire(1L, TimeUnit.SECONDS)) {
                    try {
                        return externalPythonExecutionEngine.eval(prepareEnvironmentScript, script, vars);
                    } finally {
                        executionControlSemaphore.release();
                    }
                } else {
                    logger.warn("Maximum number of python processes has been reached. Waiting for a python process to finish. " +
                            "You can configure the number of concurrent python executions by setting " +
                            "'python.concurrent.execution.permits' system property.");
                    executionControlSemaphore.acquire();
                    try {
                        logger.info("Acquired a permit for a new python process. Continuing with execution...");
                        return externalPythonExecutionEngine.eval(prepareEnvironmentScript, script, vars);
                    } finally {
                        executionControlSemaphore.release();
                    }
                }
            } catch (InterruptedException ie) {
                throw new ExternalPythonScriptException("Execution was interrupted while waiting for a python permit.");
            }
        }

    }

    @Override
    public PythonEvaluationResult test(String prepareEnvironmentScript, String script, Map<String, Serializable> vars, long timeout) {
        try {
            if (testingControlSemaphore.tryAcquire(1L, TimeUnit.SECONDS)) {
                try {
                    return externalPythonExecutionEngine.test(prepareEnvironmentScript, script, vars, timeout);
                } finally {
                    testingControlSemaphore.release();
                }
            } else {
                logger.warn("Maximum number of python processes has been reached. Waiting for a python process to finish. " +
                        "You can configure the number of concurrent python executions by setting " +
                        "'python.testing.concurrent.execution.permits' system property.");
                testingControlSemaphore.acquire();
                try {
                    logger.info("Acquired a permit for a new python process. Continuing with execution...");
                    return externalPythonExecutionEngine.test(prepareEnvironmentScript, script, vars, timeout);
                } finally {
                    testingControlSemaphore.release();
                }
            }
        } catch (InterruptedException ie) {
            throw new ExternalPythonScriptException("Execution was interrupted while waiting for a python permit.");
        }
    }
}
