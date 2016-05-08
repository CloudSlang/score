package io.cloudslang.runtime.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Created by Genadi Rabinovich, genadi@hpe.com on 05/05/2016.
 */
public class AbsExecutionCachedEngineTest {
    protected void testCachedExecutorEngineMultiThreaded(final ExecutionCachedEngine executionEngineAllocator) throws InterruptedException {
        final Set<String> dependencies0 = new HashSet<>(Arrays.asList("g1:a2:v3", "g2:a3:v4"));
        final Set<String> dependencies1 = new HashSet<>(Arrays.asList("g2:a3:v4", "g3:a4:v5"));
        final Set<String> dependencies2 = new HashSet<>(Arrays.asList("g3:a4:v5", "g4:a5:v6"));
        final Set<String> dependencies3 = new HashSet<>(Arrays.asList("g4:a5:v6", "g5:a6:v7"));

        int executionsNumber = 200;
        int threads = 20;

        long start = System.currentTimeMillis();
        final CountDownLatch latch = new CountDownLatch(executionsNumber);

        ExecutorService service = Executors.newFixedThreadPool(threads);

        for(int i = 0; i < executionsNumber; i++) {
            final int executioId = i;
            service.submit(new Runnable() {
                public void run() {
                    switch (executioId % 4) {
                        case 0:
                            executionEngineAllocator.allocateExecutor(dependencies0);
                            latch.countDown();
                            break;
                        case 1:
                            executionEngineAllocator.allocateExecutor(dependencies1);
                            latch.countDown();
                            break;
                        case 2:
                            executionEngineAllocator.allocateExecutor(dependencies2);
                            latch.countDown();
                            break;
                        case 3:
                            executionEngineAllocator.allocateExecutor(dependencies3);
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

    protected void testLeastRecentrlyUse(ExecutionCachedEngine executionEngineAllocator) {
        final Set<String> dependencies1 = new HashSet<>(Arrays.asList("g1:a2:v3", "g2:a3:v4"));
        Executor javaExecutor1 = executionEngineAllocator.allocateExecutor(dependencies1);
        // L-> 1
        final Set<String> dependencies2 = new HashSet<>(Arrays.asList("g2:a3:v4", "g3:a4:v5"));
        Executor javaExecutor2 = executionEngineAllocator.allocateExecutor(dependencies2);
        // L-> 1 -> 2
        assertNotEquals(javaExecutor1, javaExecutor2);

        final Set<String> dependencies3 = new HashSet<>(Arrays.asList("g3:a4:v5", "g4:a5:v6"));
        Executor javaExecutor3 = executionEngineAllocator.allocateExecutor(dependencies3);
        // L-> 1 -> 2 -> 3
        assertNotEquals(javaExecutor1, javaExecutor3);
        assertNotEquals(javaExecutor2, javaExecutor3);

        // already cached
        assertEquals(javaExecutor1, executionEngineAllocator.allocateExecutor(dependencies1));
        assertEquals(javaExecutor1, executionEngineAllocator.allocateExecutor(new HashSet<>(Arrays.asList("g2:a3:v4", "g1:a2:v3"))));
        // L-> 2 -> 3 -> 1

        // already cached
        assertEquals(javaExecutor2, executionEngineAllocator.allocateExecutor(dependencies2));
        assertEquals(javaExecutor2, executionEngineAllocator.allocateExecutor(new HashSet<>(Arrays.asList("g3:a4:v5", "g2:a3:v4"))));
        // L-> 3 -> 1 -> 2

        // already cached
        assertEquals(javaExecutor3, executionEngineAllocator.allocateExecutor(dependencies3));
        assertEquals(javaExecutor3, executionEngineAllocator.allocateExecutor(new HashSet<>(Arrays.asList("g4:a5:v6", "g3:a4:v5"))));
        // L-> 1 -> 2 -> 3


        // new one, should remove javaExecutor1
        final Set<String> dependencies4 = new HashSet<>(Arrays.asList("g4:a5:v6", "g5:a6:v7"));
        Executor javaExecutor4 = executionEngineAllocator.allocateExecutor(dependencies4);
        // L-> 2 -> 3 -> 4
        assertNotEquals(javaExecutor1, javaExecutor4);
        assertNotEquals(javaExecutor2, javaExecutor4);
        assertNotEquals(javaExecutor3, javaExecutor4);

        // still cached
        assertEquals(javaExecutor2, executionEngineAllocator.allocateExecutor(dependencies2));
        assertEquals(javaExecutor2, executionEngineAllocator.allocateExecutor(new HashSet<>(Arrays.asList("g3:a4:v5", "g2:a3:v4"))));
        // L-> 3 -> 4 -> 2

        // still cached
        assertEquals(javaExecutor3, executionEngineAllocator.allocateExecutor(dependencies3));
        assertEquals(javaExecutor3, executionEngineAllocator.allocateExecutor(new HashSet<>(Arrays.asList("g4:a5:v6", "g3:a4:v5"))));
        // L-> 4 -> 2 -> 3

        // already cached
        assertEquals(javaExecutor4, executionEngineAllocator.allocateExecutor(dependencies4));
        assertEquals(javaExecutor4, executionEngineAllocator.allocateExecutor(new HashSet<>(Arrays.asList("g5:a6:v7", "g4:a5:v6"))));
        // L-> 2 -> 3 -> 4


        Executor javaExecutor1New = executionEngineAllocator.allocateExecutor(dependencies1);
        assertNotEquals(javaExecutor1, javaExecutor1New);
        assertEquals(javaExecutor1New, executionEngineAllocator.allocateExecutor(dependencies1));
        assertEquals(javaExecutor1New, executionEngineAllocator.allocateExecutor(new HashSet<>(Arrays.asList("g2:a3:v4", "g1:a2:v3"))));
        // L-> 3 -> 4 -> 1

        // still cached
        assertEquals(javaExecutor3, executionEngineAllocator.allocateExecutor(dependencies3));
        assertEquals(javaExecutor3, executionEngineAllocator.allocateExecutor(new HashSet<>(Arrays.asList("g4:a5:v6", "g3:a4:v5"))));
        // L-> 4 -> 1 -> 3

        // still cached
        assertEquals(javaExecutor4, executionEngineAllocator.allocateExecutor(dependencies4));
        assertEquals(javaExecutor4, executionEngineAllocator.allocateExecutor(new HashSet<>(Arrays.asList("g5:a6:v7", "g4:a5:v6"))));
        // L-> 1 -> 3 -> 4

        // still cached
        assertEquals(javaExecutor1New, executionEngineAllocator.allocateExecutor(dependencies1));
        assertEquals(javaExecutor1New, executionEngineAllocator.allocateExecutor(new HashSet<>(Arrays.asList("g2:a3:v4", "g1:a2:v3"))));
        // L-> 3 -> 4 -> 1

        Executor javaExecutor2New = executionEngineAllocator.allocateExecutor(dependencies2);
        assertNotEquals(javaExecutor2, javaExecutor2New);
        assertEquals(javaExecutor2New, executionEngineAllocator.allocateExecutor(dependencies2));
        assertEquals(javaExecutor2New, executionEngineAllocator.allocateExecutor(new HashSet<>(Arrays.asList("g3:a4:v5", "g2:a3:v4"))));
        // L-> 4 -> 1 -> 2

        Executor javaExecutor3New = executionEngineAllocator.allocateExecutor(dependencies3);
        assertNotEquals(javaExecutor3, javaExecutor3New);
        assertEquals(javaExecutor3New, executionEngineAllocator.allocateExecutor(dependencies3));
        assertEquals(javaExecutor3New, executionEngineAllocator.allocateExecutor(new HashSet<>(Arrays.asList("g4:a5:v6", "g3:a4:v5"))));
        // L-> 1 -> 2 -> 3

        // now this fourth one removed --> javaExecutor1New removed
        assertNotEquals(javaExecutor4, executionEngineAllocator.allocateExecutor(dependencies4));
        // L-> 2 -> 3 -> 1

        // already cached
        assertEquals(javaExecutor2New, executionEngineAllocator.allocateExecutor(dependencies2));
        assertEquals(javaExecutor2New, executionEngineAllocator.allocateExecutor(new HashSet<>(Arrays.asList("g3:a4:v5", "g2:a3:v4"))));
        // L-> 3 -> 1 -> 2

        // already cached
        assertEquals(javaExecutor3New, executionEngineAllocator.allocateExecutor(dependencies3));
        assertEquals(javaExecutor3New, executionEngineAllocator.allocateExecutor(new HashSet<>(Arrays.asList("g4:a5:v6", "g3:a4:v5"))));
        // L-> 1 -> 2 -> 3
    }

}
