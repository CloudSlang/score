/*
 * Copyright © 2014-2017 EntIT Software LLC, a Micro Focus company (L.P.)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.cloudslang.worker.management.services;


import com.conversantmedia.util.concurrent.DisruptorBlockingQueue;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static java.lang.Double.parseDouble;
import static java.lang.Integer.getInteger;
import static java.lang.Runtime.getRuntime;
import static java.lang.String.format;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

public class WorkerConfigurationUtils {

    private static final Logger logger = Logger.getLogger(WorkerConfigurationUtils.class);

    private static final long MEMORY_THRESHOLD = 50_000_000; // 50 Mega byte

    private static final String WORKER_INBUFFER_SIZE = "worker.inbuffer.size";
    private static final String WORKER_INBUFFER_MIN_SIZE = "worker.inbuffer.minSize";
    private static final String ENABLE_NEW_INBUFFER = "enable.new.inbuffer";

    private static final String INBUFFER_IMPLEMENTATION_KEY = "worker.inbuffer.strategy";
    private static final String LINKED = "linked";
    private static final String DISRUPTOR = "disruptor";
    private static final String ARRAY = "array";

    private static final String WORKER_BLOCKING_QUEUE_IMPLEMENTATION = "Worker blocking queue implementation: %s";

    private static final String WORKER_MEMORY_RATIO = "worker.freeMemoryRatio";
    public static final double NEW_DEFAULT_WORKER_MEMORY_RATIO = 0.1; // 10 percent of Xmx

    public Pair<Integer, Integer> getMinSizeAndSizeOfInBuffer(int executionThreadsCount) {
        // 20% percent of executionThreadsCount, but bigger than 1
        int defaultMinInBufferSize = Math.max(1, executionThreadsCount / 5);
        int minInBufferSizeLocal = getInteger(WORKER_INBUFFER_MIN_SIZE, defaultMinInBufferSize);
        int minInBufferSize = (minInBufferSizeLocal > 0) ? minInBufferSizeLocal : defaultMinInBufferSize;

        int defaultNewInBufferSize = (executionThreadsCount == 1) ? 2 : ((3 * executionThreadsCount) / 2);
        int newInBufferSizeLocal = getInteger(WORKER_INBUFFER_SIZE, defaultNewInBufferSize);
        int newInBufferSize = (newInBufferSizeLocal > minInBufferSize) ? newInBufferSizeLocal : defaultNewInBufferSize;

        if (newInBufferSize <= minInBufferSize) {
            throw new IllegalStateException(
                    format("Value of property \"%s\" must be greater than the value of property \"%s\".",
                            WORKER_INBUFFER_SIZE, WORKER_INBUFFER_MIN_SIZE));
        }
        return new ImmutablePair<>(minInBufferSize, newInBufferSize);
    }

    public boolean isNewInbuffer() {
        return Boolean.getBoolean(ENABLE_NEW_INBUFFER);
    }

    public BlockingQueue<Runnable> getBlockingQueue(int executionThreadsCount, int capacity) {
        BlockingQueue<Runnable> blockingQueue;
        String workerInBufferQueuePolicy = System.getProperty(INBUFFER_IMPLEMENTATION_KEY, LINKED);
        if (equalsIgnoreCase(workerInBufferQueuePolicy, LINKED)) {
            blockingQueue = getLinkedQueue();
            logger.info(String.format(WORKER_BLOCKING_QUEUE_IMPLEMENTATION, LINKED));
        } else if (equalsIgnoreCase(workerInBufferQueuePolicy, DISRUPTOR)) {
            blockingQueue = getDisruptorQueue(executionThreadsCount, capacity);
            logger.info(String.format(WORKER_BLOCKING_QUEUE_IMPLEMENTATION, DISRUPTOR));
        } else if (equalsIgnoreCase(workerInBufferQueuePolicy, ARRAY)) {
            logger.info(String.format(WORKER_BLOCKING_QUEUE_IMPLEMENTATION, ARRAY));
            blockingQueue = getArrayQueue(executionThreadsCount, capacity);
        } else {
            throw new IllegalArgumentException(String.format("Illegal value %s for property %s",
                    workerInBufferQueuePolicy, INBUFFER_IMPLEMENTATION_KEY));
        }

        return blockingQueue;
    }

    private LinkedBlockingQueue<Runnable> getLinkedQueue() {
        return new LinkedBlockingQueue<>();
    }

    private BlockingQueue<Runnable> getDisruptorQueue(int executionThreadsCount, int capacity) {
        return new DisruptorBlockingQueue<>(doGetFixedSizeQueueCapacity(executionThreadsCount, capacity));
    }

    private BlockingQueue<Runnable> getArrayQueue(int executionThreadsCount, int capacity) {
        return new ArrayBlockingQueue<>(doGetFixedSizeQueueCapacity(executionThreadsCount, capacity));
    }

    private int doGetFixedSizeQueueCapacity(int executionThreadsCount, int capacity) {
        int fixedSizeCapacity;
        if (isNewInbuffer()) {
            Pair<Integer, Integer> minSizeAndSizeOfInBuffer = getMinSizeAndSizeOfInBuffer(executionThreadsCount);
            fixedSizeCapacity = minSizeAndSizeOfInBuffer.getRight();
        } else {
            fixedSizeCapacity = getInteger("worker.inbuffer.capacity", capacity);
        }
        return 2 * fixedSizeCapacity;
    }

    public double getWorkerMemoryRatio() {
        String workerMemoryRatioAsString = System.getProperty(WORKER_MEMORY_RATIO);
        double localWorkerMemoryRatio;
        // New behaviour for polling memory ratio is activated by setting "worker.freeMemoryRatio" system property
        if (isNotBlank(workerMemoryRatioAsString)) {
            try {
                localWorkerMemoryRatio = parseDouble(workerMemoryRatioAsString);
            } catch (NumberFormatException e) {
                localWorkerMemoryRatio = NEW_DEFAULT_WORKER_MEMORY_RATIO;
            }
            // Ignore values that are definitely wrong and use default free memory ratio
            if ((localWorkerMemoryRatio > 0.99) || (localWorkerMemoryRatio < 0.01)) {
                localWorkerMemoryRatio = NEW_DEFAULT_WORKER_MEMORY_RATIO;
            }
        } else { // Backward compatibility
            // To keep equivalence with old code, we don't do any validation
            localWorkerMemoryRatio = ((double) MEMORY_THRESHOLD) / getRuntime().maxMemory();
        }
        return localWorkerMemoryRatio;
    }

}
