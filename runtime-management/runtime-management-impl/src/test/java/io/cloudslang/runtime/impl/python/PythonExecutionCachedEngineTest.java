package io.cloudslang.runtime.impl.python;

import io.cloudslang.dependency.api.services.DependencyService;
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = PythonExecutionCachedEngineTest.TestConfig.class)
public class PythonExecutionCachedEngineTest {
    static {
        System.setProperty("python.executor.provider", PythonExecutionCachedEngine.class.getSimpleName());
        System.setProperty("python.executor.cache.size", "3");
    }

    @Autowired
    private PythonExecutionEngine pythonExecutionEngine;

    @Test
    public void testJavaCachedExecutorProviderMultiThreadedTest() throws InterruptedException {
        final Set<String> dependencies0 = new HashSet<>(Arrays.asList("g1:a2:v3", "g2:a3:v4"));
        final Set<String> dependencies1 = new HashSet<>(Arrays.asList("g2:a3:v4", "g3:a4:v5"));
        final Set<String> dependencies2 = new HashSet<>(Arrays.asList("g3:a4:v5", "g4:a5:v6"));
        final Set<String> dependencies3 = new HashSet<>(Arrays.asList("g4:a5:v6", "g5:a6:v7"));

        int executionsNumber = 200;
        int threads = 20;

        long start = System.currentTimeMillis();
        final CountDownLatch latch = new CountDownLatch(executionsNumber);

        ExecutorService service = Executors.newFixedThreadPool(threads);

        final PythonExecutionCachedEngineAllocator pythonExecutionEngineAllocator = (PythonExecutionCachedEngineAllocator) pythonExecutionEngine;

        for(int i = 0; i < executionsNumber; i++) {
            final int executioId = i;
            service.submit(new Runnable() {
                public void run() {
                    switch (executioId % 4) {
                        case 0:
                            pythonExecutionEngineAllocator.allocateExecutor(dependencies0);
                            latch.countDown();
                            break;
                        case 1:
                            pythonExecutionEngineAllocator.allocateExecutor(dependencies1);
                            latch.countDown();
                            break;
                        case 2:
                            pythonExecutionEngineAllocator.allocateExecutor(dependencies2);
                            latch.countDown();
                            break;
                        case 3:
                            pythonExecutionEngineAllocator.allocateExecutor(dependencies3);
                            latch.countDown();
                            break;
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        latch.await();
        service.shutdown();
        System.out.println("@@@@@@@@@@@@@@[" + executionsNumber + "] executions by [" +  threads+ "] threads finished in [" + (System.currentTimeMillis() - start) + "] msecs");
    }
    public void testJavaCachedExecutorProviderTest() {
        PythonExecutionCachedEngineAllocator javaExecutionEngineAllocator = (PythonExecutionCachedEngineAllocator) pythonExecutionEngine;
        final Set<String> dependencies1 = new HashSet<>(Arrays.asList("g1:a2:v3", "g2:a3:v4"));
        PythonExecutor javaExecutor1 = javaExecutionEngineAllocator.allocateExecutor(dependencies1);
        // L-> 1
        final Set<String> dependencies2 = new HashSet<>(Arrays.asList("g2:a3:v4", "g3:a4:v5"));
        PythonExecutor javaExecutor2 = javaExecutionEngineAllocator.allocateExecutor(dependencies2);
        // L-> 1 -> 2
        assertNotEquals(javaExecutor1, javaExecutor2);

        final Set<String> dependencies3 = new HashSet<>(Arrays.asList("g3:a4:v5", "g4:a5:v6"));
        PythonExecutor javaExecutor3 = javaExecutionEngineAllocator.allocateExecutor(dependencies3);
        // L-> 1 -> 2 -> 3
        assertNotEquals(javaExecutor1, javaExecutor3);
        assertNotEquals(javaExecutor2, javaExecutor3);

        // already cached
        assertEquals(javaExecutor1, javaExecutionEngineAllocator.allocateExecutor(dependencies1));
        assertEquals(javaExecutor1, javaExecutionEngineAllocator.allocateExecutor(new HashSet<>(Arrays.asList("g2:a3:v4", "g1:a2:v3"))));
        // L-> 2 -> 3 -> 1

        // already cached
        assertEquals(javaExecutor2, javaExecutionEngineAllocator.allocateExecutor(dependencies2));
        assertEquals(javaExecutor2, javaExecutionEngineAllocator.allocateExecutor(new HashSet<>(Arrays.asList("g3:a4:v5", "g2:a3:v4"))));
        // L-> 3 -> 1 -> 2

        // already cached
        assertEquals(javaExecutor3, javaExecutionEngineAllocator.allocateExecutor(dependencies3));
        assertEquals(javaExecutor3, javaExecutionEngineAllocator.allocateExecutor(new HashSet<>(Arrays.asList("g4:a5:v6", "g3:a4:v5"))));
        // L-> 1 -> 2 -> 3


        // new one, should remove javaExecutor1
        final Set<String> dependencies4 = new HashSet<>(Arrays.asList("g4:a5:v6", "g5:a6:v7"));
        PythonExecutor javaExecutor4 = javaExecutionEngineAllocator.allocateExecutor(dependencies4);
        // L-> 2 -> 3 -> 4
        assertNotEquals(javaExecutor1, javaExecutor4);
        assertNotEquals(javaExecutor2, javaExecutor4);
        assertNotEquals(javaExecutor3, javaExecutor4);

        // still cached
        assertEquals(javaExecutor2, javaExecutionEngineAllocator.allocateExecutor(dependencies2));
        assertEquals(javaExecutor2, javaExecutionEngineAllocator.allocateExecutor(new HashSet<>(Arrays.asList("g3:a4:v5", "g2:a3:v4"))));
        // L-> 3 -> 4 -> 2

        // still cached
        assertEquals(javaExecutor3, javaExecutionEngineAllocator.allocateExecutor(dependencies3));
        assertEquals(javaExecutor3, javaExecutionEngineAllocator.allocateExecutor(new HashSet<>(Arrays.asList("g4:a5:v6", "g3:a4:v5"))));
        // L-> 4 -> 2 -> 3

        // already cached
        assertEquals(javaExecutor4, javaExecutionEngineAllocator.allocateExecutor(dependencies4));
        assertEquals(javaExecutor4, javaExecutionEngineAllocator.allocateExecutor(new HashSet<>(Arrays.asList("g5:a6:v7", "g4:a5:v6"))));
        // L-> 2 -> 3 -> 4


        PythonExecutor javaExecutor1New = javaExecutionEngineAllocator.allocateExecutor(dependencies1);
        assertNotEquals(javaExecutor1, javaExecutor1New);
        assertEquals(javaExecutor1New, javaExecutionEngineAllocator.allocateExecutor(dependencies1));
        assertEquals(javaExecutor1New, javaExecutionEngineAllocator.allocateExecutor(new HashSet<>(Arrays.asList("g2:a3:v4", "g1:a2:v3"))));
        // L-> 3 -> 4 -> 1

        // still cached
        assertEquals(javaExecutor3, javaExecutionEngineAllocator.allocateExecutor(dependencies3));
        assertEquals(javaExecutor3, javaExecutionEngineAllocator.allocateExecutor(new HashSet<>(Arrays.asList("g4:a5:v6", "g3:a4:v5"))));
        // L-> 4 -> 1 -> 3

        // still cached
        assertEquals(javaExecutor4, javaExecutionEngineAllocator.allocateExecutor(dependencies4));
        assertEquals(javaExecutor4, javaExecutionEngineAllocator.allocateExecutor(new HashSet<>(Arrays.asList("g5:a6:v7", "g4:a5:v6"))));
        // L-> 1 -> 3 -> 4

        // still cached
        assertEquals(javaExecutor1New, javaExecutionEngineAllocator.allocateExecutor(dependencies1));
        assertEquals(javaExecutor1New, javaExecutionEngineAllocator.allocateExecutor(new HashSet<>(Arrays.asList("g2:a3:v4", "g1:a2:v3"))));
        // L-> 3 -> 4 -> 1

        PythonExecutor javaExecutor2New = javaExecutionEngineAllocator.allocateExecutor(dependencies2);
        assertNotEquals(javaExecutor2, javaExecutor2New);
        assertEquals(javaExecutor2New, javaExecutionEngineAllocator.allocateExecutor(dependencies2));
        assertEquals(javaExecutor2New, javaExecutionEngineAllocator.allocateExecutor(new HashSet<>(Arrays.asList("g3:a4:v5", "g2:a3:v4"))));
        // L-> 4 -> 1 -> 2

        PythonExecutor javaExecutor3New = javaExecutionEngineAllocator.allocateExecutor(dependencies3);
        assertNotEquals(javaExecutor3, javaExecutor3New);
        assertEquals(javaExecutor3New, javaExecutionEngineAllocator.allocateExecutor(dependencies3));
        assertEquals(javaExecutor3New, javaExecutionEngineAllocator.allocateExecutor(new HashSet<>(Arrays.asList("g4:a5:v6", "g3:a4:v5"))));
        // L-> 1 -> 2 -> 3

        // now this fourth one removed --> javaExecutor1New removed
        assertNotEquals(javaExecutor4, javaExecutionEngineAllocator.allocateExecutor(dependencies4));
        // L-> 2 -> 3 -> 1

        // already cached
        assertEquals(javaExecutor2New, javaExecutionEngineAllocator.allocateExecutor(dependencies2));
        assertEquals(javaExecutor2New, javaExecutionEngineAllocator.allocateExecutor(new HashSet<>(Arrays.asList("g3:a4:v5", "g2:a3:v4"))));
        // L-> 3 -> 1 -> 2

        // already cached
        assertEquals(javaExecutor3New, javaExecutionEngineAllocator.allocateExecutor(dependencies3));
        assertEquals(javaExecutor3New, javaExecutionEngineAllocator.allocateExecutor(new HashSet<>(Arrays.asList("g4:a5:v6", "g3:a4:v5"))));
        // L-> 1 -> 2 -> 3
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
    }

    private static class PythonExecutionCachedEngineAllocator extends PythonExecutionCachedEngine {
        public PythonExecutor allocateExecutor(Set<String> dependencies) {
            return super.allocateExecutor(dependencies);
        }
    }
}
