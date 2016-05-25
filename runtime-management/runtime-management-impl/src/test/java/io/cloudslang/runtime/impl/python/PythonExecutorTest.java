package io.cloudslang.runtime.impl.python;

import io.cloudslang.dependency.api.services.DependencyService;
import io.cloudslang.dependency.api.services.MavenConfig;
import io.cloudslang.dependency.impl.services.DependencyServiceImpl;
import io.cloudslang.dependency.impl.services.MavenConfigImpl;
import io.cloudslang.dependency.impl.services.utils.UnzipUtil;
import io.cloudslang.runtime.api.python.PythonEvaluationResult;
import io.cloudslang.runtime.api.python.PythonExecutionResult;
import io.cloudslang.runtime.api.python.PythonRuntimeService;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.*;

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

    @Autowired
    private PythonRuntimeService pythonRuntimeService;

    @Test
    public void testMultithreadedEval() throws InterruptedException {
        Assume.assumeTrue(shouldRunMaven);
        int executionsNum = 5;
        final String varName = "XXX";
        final String varValue = "YYY";

        final CountDownLatch latch = new CountDownLatch(executionsNum);

        for(int i = 0; i < executionsNum; i++) {
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
                        String [] pyResults = ((String) result.getEvalResult()).split(",");
                        assertNotNull(pyResults.length == 2);
                        String [] sysValues = pyResults[0].split(":");
                        assertNotNull(sysValues.length == 2);
                        assertEquals(sysValues[0], sysValues[1]);
                        String [] globalValues = pyResults[1].split(":");
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

        for(int i = 0; i < executionsNum; i++) {
            final String executioId = String.valueOf(i);
            new Thread() {
                public void run() {
                    try {
                        String script = MessageFormat.format(EXECUTION_SCRIPT, executioId, executioId);
                        PythonExecutionResult result = pythonRuntimeService.exec(Collections.<String>emptySet(), script, Collections.<String, Serializable>emptyMap());
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

        final String [] dependencies = {
                new File(getClass().getClassLoader().getResource(".m2/repository/python/math2/mult/1.0/mult-1.0.zip").getFile()).getAbsolutePath(),
                new File(getClass().getClassLoader().getResource(".m2/repository/python/math2/sum/2.1/sum-2.1.zip").getFile()).getAbsolutePath(),
                new File(getClass().getClassLoader().getResource(".m2/repository/python/math3/mult/1.2/mult-1.2.zip").getFile()).getAbsolutePath(),
                new File(getClass().getClassLoader().getResource(".m2/repository/python/math3/sum/4.1/sum-4.1.zip").getFile()).getAbsolutePath()
        };

        for(int i = 0; i < executionsNum; i++) {
            final String executioId = String.valueOf(i);
            final String varName = "VAR";
            final String script = "import sys\nimport time\nimport math_fake.utils.print_text as print_text\ntime.sleep(3)\n" + varName + " = print_text.foo('" + executioId + "')\nprint " + varName + "\n";
            final String dependency = dependencies[i % 4];

            int count = dependency.indexOf("math2") != -1 ? 2 : 3;
            String sign = dependency.indexOf("sum-") != -1 ? "+" : "*";

            final StringBuilder expectedResult = new StringBuilder(executioId);
            while (--count > 0) {
                expectedResult.append(sign).append(executioId);
            }

            new Thread() {
                public void run() {
                    try {
                        PythonExecutionResult result = pythonRuntimeService.exec(new HashSet<>(Collections.singletonList(dependency)), script, Collections.<String, Serializable>emptyMap());
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

    private String buildAddFunctionsScript(String ... functionDependencies) {
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
        @Bean public PythonRuntimeService pythonRuntimeService() {return new PythonRuntimeServiceImpl();}
        @Bean public PythonExecutionEngine pythonExecutionEngine() {return new PythonExecutionCachedEngine();}
        @Bean public DependencyService dependencyService() {return new DependencyServiceImpl() {
            public Set<String> getDependencies(Set<String> resources) {
                return resources;
            }
        };}
        @Bean public MavenConfig mavenConfig() {return new MavenConfigImpl();}
    }
}
