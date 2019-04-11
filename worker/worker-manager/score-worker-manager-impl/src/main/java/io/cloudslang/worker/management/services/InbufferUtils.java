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


import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import static java.lang.Integer.getInteger;
import static java.lang.String.format;

public class InbufferUtils {

    private static final String WORKER_INBUFFER_SIZE = "worker.inbuffer.size";
    private static final String WORKER_INBUFFER_MIN_SIZE = "worker.inbuffer.minSize";
    public static final String ENABLE_NEW_INBUFFER = "enable.new.inbuffer";

    public Pair<Integer, Integer> getMinSizeAndSizeOfInBuffer(int executionThreadsCount) {
        int minInBufferSizeLocal = getInteger(WORKER_INBUFFER_MIN_SIZE, executionThreadsCount);
        int minInBufferSize = (minInBufferSizeLocal > 0) ? minInBufferSizeLocal : executionThreadsCount;

        int defaultNewInBufferSize = 2 * executionThreadsCount;
        int newInBufferSizeLocal = getInteger(WORKER_INBUFFER_SIZE, defaultNewInBufferSize);
        int newInBufferSize = (newInBufferSizeLocal > minInBufferSize) ? newInBufferSizeLocal : defaultNewInBufferSize;

        if (newInBufferSize <= minInBufferSize) {
            throw new IllegalStateException(format("Value of property \"%s\" must be greater than the value of property \"%s\".",
                    WORKER_INBUFFER_SIZE, WORKER_INBUFFER_MIN_SIZE));
        }
        return new ImmutablePair<>(minInBufferSize, newInBufferSize);
    }

    public boolean isNewInbuffer() {
        return Boolean.getBoolean(ENABLE_NEW_INBUFFER);
    }

}
