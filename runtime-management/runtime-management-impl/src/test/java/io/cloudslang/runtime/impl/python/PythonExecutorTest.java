package io.cloudslang.runtime.impl.python;

import io.cloudslang.dependency.api.services.DependencyService;
import io.cloudslang.dependency.api.services.MavenConfig;
import io.cloudslang.dependency.impl.services.DependencyServiceImpl;
import io.cloudslang.dependency.impl.services.MavenConfigImpl;
import io.cloudslang.runtime.api.python.PythonRuntimeService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = PythonExecutorTest.TestConfig.class)
public class PythonExecutorTest {
    private static String LINE_SEPARATOR = System.lineSeparator();
    private static final String SYSTEM_PROPERTIES_MAP = "__sys_prop__";

    private static final String GET_SP_FUNCTION_DEFINITION =
                    "import time" + LINE_SEPARATOR +
                    "def check_env(sysPropName, expectedSysPropValue, variable, expectedVariableValue):" + LINE_SEPARATOR +
                    "  time.sleep(5)" + LINE_SEPARATOR +
                    "  property_value = __sys_prop__.get(sysPropName)" + LINE_SEPARATOR +
                    "  print 'sysProperty: found ' + property_value + ', expected ' + expectedSysPropValue" + LINE_SEPARATOR +
                    "  global_variable_value = globals().get(variable)" + LINE_SEPARATOR +
                    "  print 'global variable: found ' + str(global_variable_value) + ', expected ' + expectedVariableValue" + LINE_SEPARATOR +
                    "  EXPECTED=expectedVariableValue" + LINE_SEPARATOR +
                    "  ACTUAL=property_value" + LINE_SEPARATOR +
                    "  return expectedSysPropValue + ':' + property_value + ',' + expectedVariableValue + ':' + global_variable_value";

    @Autowired
    private PythonRuntimeService pythonRuntimeService;

    @Test
    public void testMultithreadedEval() throws InterruptedException {
        int executionsNum = 20;
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
                        Serializable result = pythonRuntimeService.eval(prepareEnvironmentScript, script, vars);
                        String [] pyResults = ((String) result).split(",");
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
        @Bean public DependencyService dependencyService() {return new DependencyServiceImpl();}
        @Bean public MavenConfig mavenConfig() {return new MavenConfigImpl();}
    }
}
