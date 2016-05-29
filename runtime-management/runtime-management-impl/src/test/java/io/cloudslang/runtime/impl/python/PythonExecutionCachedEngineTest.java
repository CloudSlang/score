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

import java.io.Serializable;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

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
        testLeastRecentlyUse((PythonExecutionCachedEngineAllocator) pythonExecutionEngine);
    }

    @Test
    public void testPythonExecutorRelease() {
        PythonExecutionCachedEngineAllocator executionEngineAllocator = (PythonExecutionCachedEngineAllocator) pythonExecutionEngine;
        final Set<String> dependencies1 = new HashSet<>(Arrays.asList("g1:a2:v3", "g2:a3:v4"));
        PythonExecutor executor1 = executionEngineAllocator.allocateExecutor(dependencies1);
        assertFalse(executor1.isClosed());

        // L-> 1
        final Set<String> dependencies2 = new HashSet<>(Arrays.asList("g2:a3:v4", "g3:a4:v5"));
        PythonExecutor executor2 = executionEngineAllocator.allocateExecutor(dependencies2);
        assertFalse(executor2.isClosed());
        // L-> 1 -> 2
        assertNotEquals(executor1, executor2);

        final Set<String> dependencies3 = new HashSet<>(Arrays.asList("g3:a4:v5", "g4:a5:v6"));
        PythonExecutor executor3 = executionEngineAllocator.allocateExecutor(dependencies3);
        assertFalse(executor3.isClosed());
        // L-> 1 -> 2 -> 3
        assertNotEquals(executor1, executor3);
        assertNotEquals(executor2, executor3);

        // already cached
        executionEngineAllocator.allocateExecutor(dependencies1);
        assertFalse(executor1.isClosed());
        assertFalse(executor2.isClosed());
        assertFalse(executor3.isClosed());

        executionEngineAllocator.allocateExecutor(new HashSet<>(Arrays.asList("g2:a3:v4", "g1:a2:v3")));
        assertFalse(executor1.isClosed());
        assertFalse(executor2.isClosed());
        assertFalse(executor3.isClosed());
        // L-> 2 -> 3 -> 1

        // already cached
        executionEngineAllocator.allocateExecutor(dependencies2);
        assertFalse(executor1.isClosed());
        assertFalse(executor2.isClosed());
        assertFalse(executor3.isClosed());

        executionEngineAllocator.allocateExecutor(new HashSet<>(Arrays.asList("g3:a4:v5", "g2:a3:v4")));
        assertFalse(executor1.isClosed());
        assertFalse(executor2.isClosed());
        assertFalse(executor3.isClosed());
        // L-> 3 -> 1 -> 2

        // already cached
        executionEngineAllocator.allocateExecutor(dependencies3);
        assertFalse(executor1.isClosed());
        assertFalse(executor2.isClosed());
        assertFalse(executor3.isClosed());

        executionEngineAllocator.allocateExecutor(new HashSet<>(Arrays.asList("g4:a5:v6", "g3:a4:v5")));
        assertFalse(executor1.isClosed());
        assertFalse(executor2.isClosed());
        assertFalse(executor3.isClosed());
        // L-> 1 -> 2 -> 3


        // new one, should remove javaExecutor1
        final Set<String> dependencies4 = new HashSet<>(Arrays.asList("g4:a5:v6", "g5:a6:v7"));
        PythonExecutor executor4 = executionEngineAllocator.allocateExecutor(dependencies4);
        // L-> 2 -> 3 -> 4
        assertNotEquals(executor1, executor4);
        assertNotEquals(executor2, executor4);
        assertNotEquals(executor3, executor4);

        //executor1 was removed from cache and marked for close but not closed yet since it is in use
        assertFalse(executor1.isClosed());
        assertFalse(executor2.isClosed());
        assertFalse(executor3.isClosed());
        assertFalse(executor4.isClosed());

        executionEngineAllocator.releaseExecutor(executor1);
        //executor1 is released only once but was allocated 3 times
        assertFalse(executor1.isClosed());
        assertFalse(executor2.isClosed());
        assertFalse(executor3.isClosed());
        assertFalse(executor4.isClosed());

        executionEngineAllocator.releaseExecutor(executor1);
        executionEngineAllocator.releaseExecutor(executor1);
        //executor1 is released all 3 time and should be closed
        assertTrue(executor1.isClosed());
        assertFalse(executor2.isClosed());
        assertFalse(executor3.isClosed());
        assertFalse(executor4.isClosed());

        executionEngineAllocator.releaseExecutor(executor2);
        executionEngineAllocator.releaseExecutor(executor2);
        executionEngineAllocator.releaseExecutor(executor2);

        executionEngineAllocator.releaseExecutor(executor3);
        executionEngineAllocator.releaseExecutor(executor3);
        executionEngineAllocator.releaseExecutor(executor3);

        executionEngineAllocator.releaseExecutor(executor4);

        //executor2, executor3, executor4 are released but not closed yet
        assertTrue(executor1.isClosed());
        assertFalse(executor2.isClosed());
        assertFalse(executor3.isClosed());
        assertFalse(executor4.isClosed());

        executor2.close();
        executor3.close();
        executor4.close();

        //executor2, executor3, executor4 are closed now as well
        assertTrue(executor1.isClosed());
        assertTrue(executor2.isClosed());
        assertTrue(executor3.isClosed());
        assertTrue(executor4.isClosed());
    }

    @Test
    public void testPythonExecutorReleasedAfterSuccessExecution() {
        final PythonExecutor pythonExecutor = mock(PythonExecutor.class);
        PythonExecutionCachedEngine engine = new PythonExecutionCachedEngine() {
            public PythonExecutor allocateExecutor(Set<String> dependencies) {
                return pythonExecutor;
            }
        };
        engine.exec(Collections.<String>emptySet(),"", new HashMap<String, Serializable>());
        verify(pythonExecutor).release();
    }

    @Test
    public void testPythonExecutorReleasedAfterException() {
        final String script = "";
        final Map<String, Serializable> args = new HashMap<>();
        final PythonExecutor pythonExecutor = mock(PythonExecutor.class);
        when(pythonExecutor.exec(script, args)).thenThrow(new IllegalArgumentException(""));
        PythonExecutionCachedEngine engine = new PythonExecutionCachedEngine() {
            public PythonExecutor allocateExecutor(Set<String> dependencies) {
                return pythonExecutor;
            }
        };
        try {
            engine.exec(Collections.<String>emptySet(),script, args);
        } catch (Throwable t) {
            assertTrue(t instanceof IllegalArgumentException);
        }
        verify(pythonExecutor).release();
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

        public void releaseExecutor(PythonExecutor executor) {
            super.releaseExecutor(executor);
        }
    }
}
