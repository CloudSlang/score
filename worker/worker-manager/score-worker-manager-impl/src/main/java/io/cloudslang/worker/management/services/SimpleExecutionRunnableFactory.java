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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.cloudslang.engine.queue.entities.ExecutionMessageConverter;
import io.cloudslang.engine.queue.services.QueueStateIdGeneratorService;
import io.cloudslang.orchestrator.services.SuspendedExecutionService;
import io.cloudslang.worker.execution.services.ExecutionService;
import io.cloudslang.worker.management.WorkerConfigurationService;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

import static java.lang.Long.MAX_VALUE;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

public class SimpleExecutionRunnableFactory implements FactoryBean<SimpleExecutionRunnable> {

    @Autowired
    private ExecutionService executionService;

    @Autowired
    private OutboundBuffer outBuffer;

    @Autowired
    private InBuffer inBuffer;

    @Autowired
    private ExecutionMessageConverter converter;

    @Autowired
    private EndExecutionCallback endExecutionCallback;

    @Autowired
    private QueueStateIdGeneratorService queueStateIdGeneratorService;

    @Autowired
    private WorkerConfigurationService workerConfigurationService;

    @Autowired
    private SuspendedExecutionService suspendedExecutionService;

    @Autowired
    private WorkerManager workerManager;

    @Resource
    private String workerUuid;

    private ExecutorService executorService;

    @PostConstruct
    public void init() {
        ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("miAsync - %d").build();
        executorService = new ThreadPoolExecutor(5, 6, MAX_VALUE, MILLISECONDS, new LinkedBlockingDeque<>(20), threadFactory, new ThreadPoolExecutor.CallerRunsPolicy());
    }

    @PreDestroy
    public void destroy() {
        executorService.shutdown();
        try {
            executorService.awaitTermination(30, SECONDS);
        } catch (InterruptedException ignored) {
        } finally {
            executorService.shutdownNow();
        }
    }

    @Override
    public SimpleExecutionRunnable getObject() {
        return new SimpleExecutionRunnable(
                executionService,
                outBuffer,
                inBuffer,
                converter,
                endExecutionCallback,
                queueStateIdGeneratorService,
                suspendedExecutionService,
                workerUuid,
                workerConfigurationService,
                workerManager,
                executorService
        );
    }

    @Override
    public Class<?> getObjectType() {
        return SimpleExecutionRunnable.class;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }
}
