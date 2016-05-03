package io.cloudslang.runtime.impl.java;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = JavaCachedExecutorProviderTest.TestConfig.class)
public class JavaExecutorTest {

    public static final String CLASS_NAME = "group.artifact.OneClass";
    public static final String METHOD_NAME = "getVersion";
    public static final Object[] ARGS = new Object[0];

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

        assertEquals("The version is One 1 and [The version is Another 2]", javaExecutor1.execute(CLASS_NAME, METHOD_NAME, ARGS).toString());
        assertEquals("The version is One 2 and [The version is Another 3]", javaExecutor2.execute(CLASS_NAME, METHOD_NAME, ARGS).toString());
        assertEquals("The version is One 3 and [The version is Another 1]", javaExecutor3.execute(CLASS_NAME, METHOD_NAME, ARGS).toString());

        assertEquals("The version is One 1 and [The version is Another 2]", javaExecutor1.execute(CLASS_NAME, METHOD_NAME, ARGS).toString());
        assertEquals("The version is One 2 and [The version is Another 3]", javaExecutor2.execute(CLASS_NAME, METHOD_NAME, ARGS).toString());
        assertEquals("The version is One 3 and [The version is Another 1]", javaExecutor3.execute(CLASS_NAME, METHOD_NAME, ARGS).toString());
    }

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void testJavaExecutorMissingDependency() {
        expectedEx.expect(RuntimeException.class);
        expectedEx.expectMessage("Method [getVersion] invocation of class [group.artifact.OneClass] failed!!!!");

        File one = new File(getClass().getClassLoader().getResource("one1.zip").getFile());

        JavaExecutor javaExecutor = new JavaExecutor(new HashSet<>(Arrays.asList(one.getAbsolutePath())));

        javaExecutor.execute(CLASS_NAME, METHOD_NAME, ARGS).toString();
    }

    @Configuration
    static class TestConfig {
    }
}
