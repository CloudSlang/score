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

    protected void testLeastRecentlyUse(ExecutionCachedEngine executionEngineAllocator) {
        final Set<String> dependencies1 = new HashSet<>(Arrays.asList("g1:a2:v3", "g2:a3:v4"));
        Executor executor1 = executionEngineAllocator.allocateExecutor(dependencies1);
        // L-> 1
        final Set<String> dependencies2 = new HashSet<>(Arrays.asList("g2:a3:v4", "g3:a4:v5"));
        Executor executor2 = executionEngineAllocator.allocateExecutor(dependencies2);
        // L-> 1 -> 2
        assertNotEquals(executor1, executor2);

        final Set<String> dependencies3 = new HashSet<>(Arrays.asList("g3:a4:v5", "g4:a5:v6"));
        Executor executor3 = executionEngineAllocator.allocateExecutor(dependencies3);
        // L-> 1 -> 2 -> 3
        assertNotEquals(executor1, executor3);
        assertNotEquals(executor2, executor3);

        // already cached
        assertEquals(executor1, executionEngineAllocator.allocateExecutor(dependencies1));
        assertEquals(executor1, executionEngineAllocator.allocateExecutor(new HashSet<>(Arrays.asList("g2:a3:v4", "g1:a2:v3"))));
        // L-> 2 -> 3 -> 1

        // already cached
        assertEquals(executor2, executionEngineAllocator.allocateExecutor(dependencies2));
        assertEquals(executor2, executionEngineAllocator.allocateExecutor(new HashSet<>(Arrays.asList("g3:a4:v5", "g2:a3:v4"))));
        // L-> 3 -> 1 -> 2

        // already cached
        assertEquals(executor3, executionEngineAllocator.allocateExecutor(dependencies3));
        assertEquals(executor3, executionEngineAllocator.allocateExecutor(new HashSet<>(Arrays.asList("g4:a5:v6", "g3:a4:v5"))));
        // L-> 1 -> 2 -> 3


        // new one, should remove javaExecutor1
        final Set<String> dependencies4 = new HashSet<>(Arrays.asList("g4:a5:v6", "g5:a6:v7"));
        Executor executor4 = executionEngineAllocator.allocateExecutor(dependencies4);
        // L-> 2 -> 3 -> 4
        assertNotEquals(executor1, executor4);
        assertNotEquals(executor2, executor4);
        assertNotEquals(executor3, executor4);

        // still cached
        assertEquals(executor2, executionEngineAllocator.allocateExecutor(dependencies2));
        assertEquals(executor2, executionEngineAllocator.allocateExecutor(new HashSet<>(Arrays.asList("g3:a4:v5", "g2:a3:v4"))));
        // L-> 3 -> 4 -> 2

        // still cached
        assertEquals(executor3, executionEngineAllocator.allocateExecutor(dependencies3));
        assertEquals(executor3, executionEngineAllocator.allocateExecutor(new HashSet<>(Arrays.asList("g4:a5:v6", "g3:a4:v5"))));
        // L-> 4 -> 2 -> 3

        // already cached
        assertEquals(executor4, executionEngineAllocator.allocateExecutor(dependencies4));
        assertEquals(executor4, executionEngineAllocator.allocateExecutor(new HashSet<>(Arrays.asList("g5:a6:v7", "g4:a5:v6"))));
        // L-> 2 -> 3 -> 4


        Executor executor1New = executionEngineAllocator.allocateExecutor(dependencies1);
        assertNotEquals(executor1, executor1New);
        assertEquals(executor1New, executionEngineAllocator.allocateExecutor(dependencies1));
        assertEquals(executor1New, executionEngineAllocator.allocateExecutor(new HashSet<>(Arrays.asList("g2:a3:v4", "g1:a2:v3"))));
        // L-> 3 -> 4 -> 1

        // still cached
        assertEquals(executor3, executionEngineAllocator.allocateExecutor(dependencies3));
        assertEquals(executor3, executionEngineAllocator.allocateExecutor(new HashSet<>(Arrays.asList("g4:a5:v6", "g3:a4:v5"))));
        // L-> 4 -> 1 -> 3

        // still cached
        assertEquals(executor4, executionEngineAllocator.allocateExecutor(dependencies4));
        assertEquals(executor4, executionEngineAllocator.allocateExecutor(new HashSet<>(Arrays.asList("g5:a6:v7", "g4:a5:v6"))));
        // L-> 1 -> 3 -> 4

        // still cached
        assertEquals(executor1New, executionEngineAllocator.allocateExecutor(dependencies1));
        assertEquals(executor1New, executionEngineAllocator.allocateExecutor(new HashSet<>(Arrays.asList("g2:a3:v4", "g1:a2:v3"))));
        // L-> 3 -> 4 -> 1

        Executor executor2New = executionEngineAllocator.allocateExecutor(dependencies2);
        assertNotEquals(executor2, executor2New);
        assertEquals(executor2New, executionEngineAllocator.allocateExecutor(dependencies2));
        assertEquals(executor2New, executionEngineAllocator.allocateExecutor(new HashSet<>(Arrays.asList("g3:a4:v5", "g2:a3:v4"))));
        // L-> 4 -> 1 -> 2

        Executor executor3New = executionEngineAllocator.allocateExecutor(dependencies3);
        assertNotEquals(executor3, executor3New);
        assertEquals(executor3New, executionEngineAllocator.allocateExecutor(dependencies3));
        assertEquals(executor3New, executionEngineAllocator.allocateExecutor(new HashSet<>(Arrays.asList("g4:a5:v6", "g3:a4:v5"))));
        // L-> 1 -> 2 -> 3

        // now this fourth one removed --> javaExecutor1New removed
        assertNotEquals(executor4, executionEngineAllocator.allocateExecutor(dependencies4));
        // L-> 2 -> 3 -> 1

        // already cached
        assertEquals(executor2New, executionEngineAllocator.allocateExecutor(dependencies2));
        assertEquals(executor2New, executionEngineAllocator.allocateExecutor(new HashSet<>(Arrays.asList("g3:a4:v5", "g2:a3:v4"))));
        // L-> 3 -> 1 -> 2

        // already cached
        assertEquals(executor3New, executionEngineAllocator.allocateExecutor(dependencies3));
        assertEquals(executor3New, executionEngineAllocator.allocateExecutor(new HashSet<>(Arrays.asList("g4:a5:v6", "g3:a4:v5"))));
        // L-> 1 -> 2 -> 3
    }

}
