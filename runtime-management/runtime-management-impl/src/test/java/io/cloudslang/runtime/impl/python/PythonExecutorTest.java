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

package io.cloudslang.runtime.impl.python;

import io.cloudslang.dependency.api.services.DependencyService;
import io.cloudslang.dependency.api.services.MavenConfig;
import io.cloudslang.dependency.impl.services.DependencyServiceImpl;
import io.cloudslang.dependency.impl.services.MavenConfigImpl;
import io.cloudslang.dependency.impl.services.utils.UnzipUtil;
import io.cloudslang.runtime.api.python.PythonEvaluationResult;
import io.cloudslang.runtime.api.python.PythonExecutionResult;
import io.cloudslang.runtime.api.python.PythonRuntimeService;
import io.cloudslang.runtime.impl.python.external.ExternalPythonExecutionEngine;
import io.cloudslang.runtime.impl.python.external.ExternalPythonRuntimeServiceImpl;
import io.cloudslang.runtime.impl.python.external.ExternalPythonServerService;
import io.cloudslang.runtime.impl.python.external.ExternalPythonServerServiceImpl;
import io.cloudslang.runtime.impl.python.external.StatefulRestEasyClientsHolder;
import io.cloudslang.score.events.EventBus;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.python.core.PyBoolean;
import org.python.core.PyString;
import org.python.google.common.collect.Sets;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.io.File;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Created by Genadi Rabinovich, genadi@hpe.com on 05/05/2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = PythonExecutorTest.TestConfig.class)
public class PythonExecutorTest {
    private static boolean shouldRunMaven;

    static {
        ClassLoader classLoader = PythonExecutorTest.class.getClassLoader();

        String settingsXmlPath = classLoader.getResource("settings.xml").getPath();
        File rootHome = new File(settingsXmlPath).getParentFile();
        File mavenHome = new File(rootHome, "maven");
        UnzipUtil.unzipToFolder(mavenHome.getAbsolutePath(), classLoader.getResourceAsStream("maven.zip"));

        System.setProperty(MavenConfig.MAVEN_HOME, mavenHome.getAbsolutePath());

        System.setProperty(MavenConfig.MAVEN_REPO_LOCAL, new TestConfig().mavenConfig().getLocalMavenRepoPath());
        System.setProperty("maven.home", classLoader.getResource("maven").getPath());

        shouldRunMaven = System.getProperties().containsKey(MavenConfigImpl.MAVEN_REMOTE_URL) &&
                System.getProperties().containsKey(MavenConfigImpl.MAVEN_PLUGINS_URL);


        System.setProperty(MavenConfig.MAVEN_SETTINGS_PATH, settingsXmlPath);
        System.setProperty(MavenConfig.MAVEN_M2_CONF_PATH, classLoader.getResource("m2.conf").getPath());

        String provideralAlreadyConfigured = System.setProperty("python.executor.engine", PythonExecutionCachedEngine.class.getSimpleName());
        assertNull("python.executor.engine was configured before this test!!!!!!!", provideralAlreadyConfigured);
    }

    private static String LINE_SEPARATOR = System.lineSeparator();
    private static final String SYSTEM_PROPERTIES_MAP = "__sys_prop__";

    private static final String GET_SP_FUNCTION_DEFINITION =
            "import time" + LINE_SEPARATOR +
                    "def check_env(sysPropName, expectedSysPropValue, variable, expectedVariableValue):" + LINE_SEPARATOR +
                    "  time.sleep(3)" + LINE_SEPARATOR +
                    "  property_value = __sys_prop__.get(sysPropName)" + LINE_SEPARATOR +
                    "  print 'sysProperty: found ' + property_value + ', expected ' + expectedSysPropValue" + LINE_SEPARATOR +
                    "  global_variable_value = globals().get(variable)" + LINE_SEPARATOR +
                    "  print 'global variable: found ' + str(global_variable_value) + ', expected ' + expectedVariableValue" + LINE_SEPARATOR +
                    "  EXPECTED=expectedVariableValue" + LINE_SEPARATOR +
                    "  ACTUAL=property_value" + LINE_SEPARATOR +
                    "  return expectedSysPropValue + ':' + property_value + ',' + expectedVariableValue + ':' + global_variable_value";

    private static final String VAR1 = "VAR1";
    private static final String VAR2 = "VAR2";
    private static final String EXECUTION_SCRIPT =
            "import sys" + LINE_SEPARATOR +
                    "import time" + LINE_SEPARATOR +
                    "time.sleep(3)" + LINE_SEPARATOR +
                    VAR1 + "={0}" + LINE_SEPARATOR +
                    VAR2 + "={1}" + LINE_SEPARATOR +
                    "print ''VAR1='' + str(" + VAR1 + ")" + LINE_SEPARATOR +
                    "print ''VAR2='' + str(" + VAR2 + ")" + LINE_SEPARATOR;
    private static final String PY_CLASS_IS_EXCLUDED_SCRIPT =
            "from Queue import Queue" + LINE_SEPARATOR +
                    "x = 'abc'" + LINE_SEPARATOR;
    private static final Map<String, Serializable> EMPTY_CALL_ARGUMENTS = Collections.emptyMap();
    private static final Map<String, Serializable> EXPECTED_CONTEXT_EXEC;
    private static final Map<String, Serializable> EXPECTED_CONTEXT_EVAL;

    static {
        EXPECTED_CONTEXT_EXEC = new HashMap<>();
        EXPECTED_CONTEXT_EXEC.put("x", "abc");
        EXPECTED_CONTEXT_EVAL = new HashMap<>(3);
        EXPECTED_CONTEXT_EVAL.put("x", new PyString("abc"));
        EXPECTED_CONTEXT_EVAL.put("true", new PyBoolean(true));
        EXPECTED_CONTEXT_EVAL.put("false", new PyBoolean(false));
    }

    @Resource(name = "jythonRuntimeService")
    private PythonRuntimeService pythonRuntimeService;

    @Test
    public void testMultithreadedEval() throws InterruptedException {
        Assume.assumeTrue(shouldRunMaven);
        int executionsNum = 5;
        final String varName = "ABC";
        final String varValue = "YYY";

        final CountDownLatch latch = new CountDownLatch(executionsNum);

        for (int i = 0; i < executionsNum; i++) {
            final int executioId = i;
            new Thread() {
                public void run() {
                    try {
                        Map<String, String> sysProps = new HashMap<>();
                        String value = varValue + executioId;
                        sysProps.put(varName, value);
                        String doubleName = varName + varName;
                        String doubleValue = value + value;

                        Map<String, Serializable> vars = new HashMap<>();
                        vars.put(doubleName, doubleValue);
                        vars.put(SYSTEM_PROPERTIES_MAP, (Serializable) sysProps);
                        String prepareEnvironmentScript = buildAddFunctionsScript(GET_SP_FUNCTION_DEFINITION);
                        String script = "check_env('" + varName + "', '" + value + "', '" + doubleName + "', '" + doubleValue + "')";
                        PythonEvaluationResult result = pythonRuntimeService.eval(prepareEnvironmentScript, script, vars);
                        String[] pyResults = ((String) result.getEvalResult()).split(",");
                        assertNotNull(pyResults.length == 2);
                        String[] sysValues = pyResults[0].split(":");
                        assertNotNull(sysValues.length == 2);
                        assertEquals(sysValues[0], sysValues[1]);
                        String[] globalValues = pyResults[1].split(":");
                        assertNotNull(globalValues.length == 2);
                        assertEquals(globalValues[0], globalValues[1]);
                    } finally {
                        latch.countDown();
                    }
                }
            }.start();
        }
        latch.await();
    }

    @Test
    public void testMultithreadedExecNoDependencies() throws InterruptedException {
        Assume.assumeTrue(shouldRunMaven);
        int executionsNum = 5;

        final CountDownLatch latch = new CountDownLatch(executionsNum);

        for (int i = 0; i < executionsNum; i++) {
            final String executioId = String.valueOf(i);
            new Thread() {
                public void run() {
                    try {
                        String script = MessageFormat.format(EXECUTION_SCRIPT, executioId, executioId);
                        PythonExecutionResult result = pythonRuntimeService.exec(Collections.<String>emptySet(), script, EMPTY_CALL_ARGUMENTS);
                        assertNotNull(result);
                        assertEquals(executioId, result.getExecutionResult().get(VAR1).toString());
                        assertEquals(executioId, result.getExecutionResult().get(VAR2).toString());
                    } finally {
                        latch.countDown();
                    }
                }
            }.start();
        }
        latch.await();
    }

    @Test
    public void testMultithreadedExecWithDependencies() throws InterruptedException {
        Assume.assumeTrue(shouldRunMaven);
        int executionsNum = 5;

        final CountDownLatch latch = new CountDownLatch(executionsNum);

        final String[] dependencies = {
                new File(getClass().getClassLoader().getResource(".m2/repository/python/math2/mult/1.0/mult-1.0.zip").getFile()).getAbsolutePath(),
                new File(getClass().getClassLoader().getResource(".m2/repository/python/math2/sum/2.1/sum-2.1.zip").getFile()).getAbsolutePath(),
                new File(getClass().getClassLoader().getResource(".m2/repository/python/math3/mult/1.2/mult-1.2.zip").getFile()).getAbsolutePath(),
                new File(getClass().getClassLoader().getResource(".m2/repository/python/math3/sum/4.1/sum-4.1.zip").getFile()).getAbsolutePath()
        };

        for (int i = 0; i < executionsNum; i++) {
            final String executioId = String.valueOf(i);
            final String varName = "VAR";
            final String script = "import sys\nimport time\nimport math_fake.utils.print_text as print_text\ntime.sleep(3)\n" + varName + " = print_text.foo('" + executioId + "')\nprint " + varName + "\n";
            final String dependency = dependencies[i % 4];

            int count = dependency.contains("math2") ? 2 : 3;
            String sign = dependency.contains("sum-") ? "+" : "*";

            final StringBuilder expectedResult = new StringBuilder(executioId);
            while (--count > 0) {
                expectedResult.append(sign).append(executioId);
            }

            new Thread() {
                public void run() {
                    try {
                        PythonExecutionResult result = pythonRuntimeService.exec(new HashSet<>(Collections.singletonList(dependency)), script, EMPTY_CALL_ARGUMENTS);
                        assertNotNull(result);
                        assertNotNull(result.getExecutionResult().get(varName));
                        assertEquals(expectedResult.toString(), result.getExecutionResult().get(varName).toString());
                    } finally {
                        latch.countDown();
                    }
                }
            }.start();
        }
        latch.await();
    }

    @Test
    public void testPythonExecutorNoAllocationNotClosed() {
        PythonExecutor executor = getPythonExecutor();
        executor.exec("print 'x'", new HashMap<String, Serializable>());
        executor.close();
        assertTrue(executor.isClosed());
    }

    @Test(expected = RuntimeException.class)
    public void testPythonExecutorNoAllocationClosed() {
        PythonExecutor executor = getPythonExecutor();
        executor.close();
        executor.exec("print 'x'", new HashMap<String, Serializable>());
    }

    @Test
    public void testPythonExecutorAllocationSuccess() {
        PythonExecutor executor = getPythonExecutor();
        executor.exec("print 'x'", new HashMap<String, Serializable>());
        executor.allocate();
        executor.exec("print 'x'", new HashMap<String, Serializable>());
        executor.allocate();
        executor.exec("print 'x'", new HashMap<String, Serializable>());
        executor.close();
        executor.exec("print 'x'", new HashMap<String, Serializable>());
        assertFalse(executor.isClosed());
        executor.exec("print 'x'", new HashMap<String, Serializable>());
        executor.release();
        executor.exec("print 'x'", new HashMap<String, Serializable>());
        assertFalse(executor.isClosed());
        executor.exec("print 'x'", new HashMap<String, Serializable>());
        executor.release();
        assertTrue(executor.isClosed());
    }

    @Test(expected = RuntimeException.class)
    public void testPythonExecutorAllocationFailure() {
        PythonExecutor executor = getPythonExecutor();
        executor.allocate();
        executor.close();
        executor.release();
        assertTrue(executor.isClosed());
        executor.exec("print 'x'", new HashMap<String, Serializable>());
    }

    @Test
    public void testExecPyClassIsExcluded() throws Exception {
        PythonExecutor executor = getPythonExecutor();
        PythonExecutionResult pythonExecutionResult = executor.exec(PY_CLASS_IS_EXCLUDED_SCRIPT, EMPTY_CALL_ARGUMENTS);
        Assert.assertEquals(EXPECTED_CONTEXT_EXEC, pythonExecutionResult.getExecutionResult());
    }

    @Test
    public void testEvalPyClassIsExcluded() throws Exception {
        PythonExecutor executor = getPythonExecutor();
        PythonEvaluationResult pythonEvaluationResult = executor.eval(PY_CLASS_IS_EXCLUDED_SCRIPT, "'hello'", EMPTY_CALL_ARGUMENTS);
        Assert.assertEquals(EXPECTED_CONTEXT_EVAL, pythonEvaluationResult.getResultContext());
    }

    private PythonExecutor getPythonExecutor() {
        return new PythonExecutor(Sets.newHashSet("a.zip, b.zip"));
    }

    private String buildAddFunctionsScript(String... functionDependencies) {
        String functions = "";
        for (String function : functionDependencies) {
            functions = appendDelimiterBetweenFunctions(functions + function);
        }
        return functions;
    }

    private String appendDelimiterBetweenFunctions(String text) {
        return text + LINE_SEPARATOR + LINE_SEPARATOR;
    }

    @Configuration
    static class TestConfig {
        @Bean(name = "jythonRuntimeService")
        public PythonRuntimeService pythonRuntimeService() {
            return new PythonRuntimeServiceImpl();
        }

        @Bean(name = "externalPythonServerService")
        public ExternalPythonServerService externalPythonServerService() {
            return new ExternalPythonServerServiceImpl(mock(StatefulRestEasyClientsHolder.class));
        }

        @Bean(name = "externalPythonRuntimeService")
        public PythonRuntimeService externalPythonRuntimeService() {
            return new ExternalPythonRuntimeServiceImpl(new Semaphore(100), new Semaphore(50));
        }

        @Bean(name = "jythonExecutionEngine")
        PythonExecutionEngine pythonExecutionEngine() {
            return new PythonExecutionCachedEngine();
        }

        @Bean(name = "externalPythonExecutionEngine")
        PythonExecutionEngine externalPythonExecutionEngine() {
            return new ExternalPythonExecutionEngine();
        }

        @Bean
        public DependencyService dependencyService() {
            return new DependencyServiceImpl() {
                public Set<String> getDependencies(Set<String> resources) {
                    return resources;
                }
            };
        }

        @Bean
        public EventBus eventBus() {
            return mock(EventBus.class);
        }

        @Bean
        public MavenConfig mavenConfig() {
            return new MavenConfigImpl();
        }
    }
}
