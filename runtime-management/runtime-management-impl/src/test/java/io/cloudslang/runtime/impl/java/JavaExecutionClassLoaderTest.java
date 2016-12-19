package io.cloudslang.runtime.impl.java;

import java.net.URL;
import java.net.URLClassLoader;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Bonczidai Levente
 * @since 12/19/2016
 */
public class JavaExecutionClassLoaderTest {
    private static final String NON_SDK_CLASS_NAME = "NonSdkClass";
    private static final String SDK_CLASS_NAME = "com.hp.oo.sdk.SdkClass";
    private static final String NOT_FOUND_CLASS_NAME = "com.hp.oo.not.found.SomeClass";
    private static final String NON_JDK_JAR = "/classloader/testclasses_non_jdk.jar";
    private static final String JDK_JAR = "/classloader/testclasses_jdk.jar";

    private JavaExecutionClassLoader javaExecutionClassLoader;
    private ClassLoader executorClassLoader;
    private ClassLoader globalClassLoader;

    @Before
    public void setUp() throws Exception {
        executorClassLoader = createClassLoader(NON_JDK_JAR);
        globalClassLoader = createClassLoader(JDK_JAR);
        javaExecutionClassLoader = new JavaExecutionClassLoader(new URL[]{}, executorClassLoader, globalClassLoader);
    }

    @Test
    public void testLoadNonSdkClass() throws Exception {
        Class<?> actualClass = javaExecutionClassLoader.loadClass(NON_SDK_CLASS_NAME);
        Class<?> expectedClass = executorClassLoader.loadClass(NON_SDK_CLASS_NAME);

        assertEquals(expectedClass, actualClass);
    }

    @Test
    public void testLoadSdkClass() throws Exception {
        Class<?> actualClass = javaExecutionClassLoader.loadClass(SDK_CLASS_NAME);
        Class<?> expectedClass = globalClassLoader.loadClass(SDK_CLASS_NAME);

        assertEquals(expectedClass, actualClass);
    }

    @Test
    public void testClassNotFound() throws Exception {
        try {
            javaExecutionClassLoader.loadClass(NOT_FOUND_CLASS_NAME);
            fail("Expected to throw exception");
        } catch (Exception e) {
            assertEquals(
                    "Error loading class [com.hp.oo.not.found.SomeClass]: com.hp.oo.not.found.SomeClass",
                    e.getMessage()
            );
        }
    }

    private ClassLoader createClassLoader(String resourceRelativePath) {
        URL resourceUrl = getClass().getResource(resourceRelativePath);
        URL[] urlArray = new URL[]{resourceUrl};
        return new URLClassLoader(urlArray);
    }

}