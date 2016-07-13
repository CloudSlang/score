package io.cloudslang.runtime.impl.java;

import io.cloudslang.runtime.api.java.JavaExecutionParametersProvider;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;

/**
 * Created by Genadi Rabinovich, genadi@hpe.com on 05/05/2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = JavaExecutorTest.TestConfig.class)
public class JavaExecutorTest {

    static {
        ClassLoader classLoader = JavaExecutorTest.class.getClassLoader();

        String settingsXmlPath = classLoader.getResource("settings.xml").getPath();
        File rootHome = new File(settingsXmlPath).getParentFile();

        System.setProperty("app.home", rootHome.getAbsolutePath());
    }

    private static final String CLASS_NAME = "group.artifact.OneClass";
    private static final String METHOD_NAME = "getVersion";
    private static final JavaExecutionParametersProvider PARAM_PROVIDER = new JavaExecutionParametersProvider() {
        @Override
        public Object[] getExecutionParameters(Method executionMethod) {
            return new Object[0];
        }
    };

    @Test
    public void testJavaExecutorDifferentClassloaders() {

        File one1 = new File(getClass().getClassLoader().getResource("one1.zip").getFile());
        File one2 = new File(getClass().getClassLoader().getResource("one2.zip").getFile());
        File one3 = new File(getClass().getClassLoader().getResource("one3.zip").getFile());

        File another1 = new File(getClass().getClassLoader().getResource("another1.zip").getFile());
        File another2 = new File(getClass().getClassLoader().getResource("another2.zip").getFile());
        File another3 = new File(getClass().getClassLoader().getResource("another3.zip").getFile());

        JavaExecutor javaExecutor1 = new JavaExecutor(new HashSet<>(Arrays.asList(one1.getAbsolutePath(), another2.getAbsolutePath())));
        JavaExecutor javaExecutor2 = new JavaExecutor(new HashSet<>(Arrays.asList(one2.getAbsolutePath(), another3.getAbsolutePath())));
        JavaExecutor javaExecutor3 = new JavaExecutor(new HashSet<>(Arrays.asList(one3.getAbsolutePath(), another1.getAbsolutePath())));

        assertEquals("The version is One 1 and [The version is Another 2]", javaExecutor1.execute(CLASS_NAME, METHOD_NAME, PARAM_PROVIDER).toString());
        assertEquals("The version is One 2 and [The version is Another 3]", javaExecutor2.execute(CLASS_NAME, METHOD_NAME, PARAM_PROVIDER).toString());
        assertEquals("The version is One 3 and [The version is Another 1]", javaExecutor3.execute(CLASS_NAME, METHOD_NAME, PARAM_PROVIDER).toString());

        assertEquals("The version is One 1 and [The version is Another 2]", javaExecutor1.execute(CLASS_NAME, METHOD_NAME, PARAM_PROVIDER).toString());
        assertEquals("The version is One 2 and [The version is Another 3]", javaExecutor2.execute(CLASS_NAME, METHOD_NAME, PARAM_PROVIDER).toString());
        assertEquals("The version is One 3 and [The version is Another 1]", javaExecutor3.execute(CLASS_NAME, METHOD_NAME, PARAM_PROVIDER).toString());
    }

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void testJavaExecutorMissingDependency() {
        expectedEx.expect(RuntimeException.class);
        expectedEx.expectMessage("Method [getVersion] invocation of class [group.artifact.OneClass] failed!!!!");

        File one = new File(getClass().getClassLoader().getResource("one1.zip").getFile());

        JavaExecutor javaExecutor = new JavaExecutor(new HashSet<>(Arrays.asList(one.getAbsolutePath())));

        javaExecutor.execute(CLASS_NAME, METHOD_NAME, PARAM_PROVIDER).toString();
    }

    @Configuration
    static class TestConfig {
    }
}
