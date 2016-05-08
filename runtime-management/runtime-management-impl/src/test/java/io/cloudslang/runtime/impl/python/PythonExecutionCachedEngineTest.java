package io.cloudslang.runtime.impl.python;

import io.cloudslang.dependency.api.services.DependencyService;
import io.cloudslang.dependency.api.services.MavenConfig;
import io.cloudslang.dependency.impl.services.MavenConfigImpl;
import io.cloudslang.runtime.impl.AbsExecutionCachedEngineTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Genadi Rabinovich, genadi@hpe.com on 05/05/2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = PythonExecutionCachedEngineTest.TestConfig.class)
public class PythonExecutionCachedEngineTest extends AbsExecutionCachedEngineTest {
    static {
        System.setProperty("python.executor.provider", PythonExecutionCachedEngine.class.getSimpleName());
        System.setProperty("python.executor.cache.size", "3");
    }

    @Autowired
    private PythonExecutionEngine pythonExecutionEngine;

    @Test
    public void testJavaCachedExecutorProviderMultiThreadedTest() throws InterruptedException {
        testCachedExecutorEngineMultiThreaded((PythonExecutionCachedEngineAllocator) pythonExecutionEngine);
    }

    @Test
    public void testJavaCachedExecutorProviderTest() {
        testLeastRecentrlyUse((PythonExecutionCachedEngineAllocator) pythonExecutionEngine);
    }

    @Configuration
    static class TestConfig {
        @Bean
        public PythonExecutionEngine javaExecutorProvider() {return new PythonExecutionCachedEngineAllocator();}

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

    private static class PythonExecutionCachedEngineAllocator extends PythonExecutionCachedEngine {
        public PythonExecutor allocateExecutor(Set<String> dependencies) {
            return super.allocateExecutor(dependencies);
        }
    }
}
