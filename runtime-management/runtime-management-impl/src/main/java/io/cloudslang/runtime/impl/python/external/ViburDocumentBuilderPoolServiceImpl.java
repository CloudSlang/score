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
package io.cloudslang.runtime.impl.python.external;


import io.cloudslang.runtime.api.python.external.ViburDocumentBuilderPoolService;
import org.vibur.objectpool.ConcurrentPool;
import org.vibur.objectpool.util.ConcurrentLinkedQueueCollection;

import javax.xml.parsers.DocumentBuilder;

import static java.lang.Integer.getInteger;
import static java.util.concurrent.TimeUnit.SECONDS;

public class ViburDocumentBuilderPoolServiceImpl implements ViburDocumentBuilderPoolService {

    private final ConcurrentPool<DocumentBuilder> poolService;
    private final int timeout;

    public ViburDocumentBuilderPoolServiceImpl() {
        final int poolSize = getInteger("python.concurrent.execution.permits", 30);
        this.poolService = new ConcurrentPool<>(new ConcurrentLinkedQueueCollection<>(),
                new ViburDocumentBuilderPoolObjectFactory(),
                poolSize,
                poolSize,
                false);
        this.timeout = getInteger("python.outputParser.checkoutTimeoutSeconds", 5 * 60);
    }

    @Override
    public DocumentBuilder tryTakeWithTimeout() {
        return poolService.tryTake(timeout, SECONDS);
    }

    @Override
    public void restore(DocumentBuilder pooledObject) {
        poolService.restore(pooledObject);
    }

}
