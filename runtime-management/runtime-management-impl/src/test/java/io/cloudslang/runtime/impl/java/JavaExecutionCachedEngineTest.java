package io.cloudslang.runtime.impl.java;

import io.cloudslang.dependency.api.services.DependencyService;
import io.cloudslang.dependency.api.services.MavenConfig;
import io.cloudslang.dependency.impl.services.MavenConfigImpl;
import io.cloudslang.runtime.api.java.JavaExecutionParametersProvider;
import io.cloudslang.runtime.impl.AbsExecutionCachedEngineTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Created by Genadi Rabinovich, genadi@hpe.com on 05/05/2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = JavaExecutionCachedEngineTest.TestConfig.class)
public class JavaExecutionCachedEngineTest extends AbsExecutionCachedEngineTest {
    static {
        System.setProperty("java.executor.provider", JavaExecutionCachedEngine.class.getSimpleName());
        System.setProperty("java.executor.cache.size", "3");

        ClassLoader classLoader = JavaExecutorTest.class.getClassLoader();

        String settingsXmlPath = classLoader.getResource("settings.xml").getPath();
        File rootHome = new File(settingsXmlPath).getParentFile();

        System.setProperty("app.home", rootHome.getAbsolutePath());
    }

    @Autowired
    private JavaExecutionEngine javaExecutionEngine;

    @Test
    public void testJavaCachedExecutorEngineMultiThreadedTest() throws InterruptedException {
        testCachedExecutorEngineMultiThreaded((JavaExecutionCachedEngineAllocator) javaExecutionEngine);
    }

    @Test
    public void testJavaCachedExecutorProviderTest() {
        testLeastRecentlyUse((JavaExecutionCachedEngineAllocator) javaExecutionEngine);
    }

    @Test
    public void testJavaExecutorReleasedAfterSuccessExecution() {
        final JavaExecutor javaExecutor = mock(JavaExecutor.class);
        JavaExecutionCachedEngine engine = new JavaExecutionCachedEngine() {
            public JavaExecutor allocateExecutor(Set<String> dependencies) {
                return javaExecutor;
            }
        };
        engine.execute("", "", "", null);
        verify(javaExecutor).release();
    }

    @Test
    public void testJavaExecutorReleasedAfterException() {
        final JavaExecutor javaExecutor = mock(JavaExecutor.class);
        final String gav = "";
        final String className = "";
        final String methodName = "";
        JavaExecutionParametersProvider provider = null;
        when(javaExecutor.execute(className, methodName, provider)).thenThrow(new IllegalArgumentException(""));
        JavaExecutionCachedEngine engine = new JavaExecutionCachedEngine() {
            public JavaExecutor allocateExecutor(Set<String> dependencies) {
                return javaExecutor;
            }
        };
        try {
            engine.execute(gav, className, methodName, provider);
        } catch (Throwable t) {
            assertTrue(t instanceof IllegalArgumentException);
        }
        verify(javaExecutor).release();
    }

    @Configuration
    static class TestConfig {
        @Bean
        public JavaExecutionEngine javaExecutorProvider() {return new JavaExecutionCachedEngineAllocator();}

        @Bean
        public DependencyService dependencyService() {return new DependencyService() {
            @Override
            public Set<String> getDependencies(Set<String> resources) {
                return new HashSet<>(Arrays.asList("c:\\a.jar", "c:\\b.jar"));
            }
        };}

        @Bean
        public MavenConfig mavenConfig() {return new MavenConfigImpl();}
    }

    private static class JavaExecutionCachedEngineAllocator extends JavaExecutionCachedEngine {
        public JavaExecutor allocateExecutor(Set<String> dependencies) {
            return super.allocateExecutor(dependencies);
        }
    }
}
