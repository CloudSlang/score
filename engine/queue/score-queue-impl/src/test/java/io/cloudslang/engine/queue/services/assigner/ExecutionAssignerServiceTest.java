/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package io.cloudslang.engine.queue.services.assigner;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import io.cloudslang.engine.node.services.WorkerNodeService;
import io.cloudslang.engine.queue.entities.ExecStatus;
import io.cloudslang.engine.queue.entities.ExecutionMessage;
import io.cloudslang.engine.queue.entities.ExecutionMessageConverter;
import io.cloudslang.engine.queue.entities.Payload;
import io.cloudslang.engine.queue.services.ExecutionQueueService;
import io.cloudslang.orchestrator.services.EngineVersionService;
import io.cloudslang.score.facade.entities.Execution;
import io.cloudslang.score.lang.SystemContext;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by IntelliJ IDEA.
 * User:
 * Date: 26/11/12
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class ExecutionAssignerServiceTest {
    @Autowired
    private ExecutionAssignerService executionAssignerService;

    @Autowired
    private WorkerNodeService workerNodeService;

    @Autowired
    ExecutionQueueService executionQueueService;

    @Autowired
    private ExecutionMessageConverter executionMessageConverter;

    @Autowired
   	private EngineVersionService engineVersionService;

    @Test
    public void assign() throws Exception {

        Multimap<String, String> groupWorkersMap = ArrayListMultimap.create();
        groupWorkersMap.put("DefaultGroup", "worker1");
        groupWorkersMap.put("DefaultGroup", "worker2");

        List<ExecutionMessage> assignMessages = new ArrayList<>();
        ExecutionMessage msg1 = new ExecutionMessage(1, ExecutionMessage.EMPTY_WORKER, "DefaultGroup", "msg1", ExecStatus.PENDING, null, 0, (new Date(0)).getTime());
        ExecutionMessage msg2 = new ExecutionMessage(2, ExecutionMessage.EMPTY_WORKER, "DefaultGroup", "msg2", ExecStatus.PENDING, null, 0, (new Date(0)).getTime());
        assignMessages.add(msg1);
        assignMessages.add(msg2);

        Mockito.reset(executionQueueService);
        Mockito.reset(workerNodeService);
        Mockito.when(workerNodeService.readGroupWorkersMapActiveAndRunningAndVersion(engineVersionService.getEngineVersionId())).thenReturn(groupWorkersMap);

        final List<ExecutionMessage> messagesInQ = executionAssignerService.assignWorkers(assignMessages);

        Assert.assertEquals(2, messagesInQ.size());
        for (ExecutionMessage msg : messagesInQ) {
            Assert.assertEquals(ExecStatus.ASSIGNED.getNumber(), msg.getStatus().getNumber());
            Assert.assertEquals(1, msg.getMsgSeqId());
            Assert.assertTrue(msg.getWorkerId().equals("worker1") || msg.getWorkerId().equals("worker2"));
        }

    }

    @Test
    public void assignWhenHaveNoWorkers() throws Exception {

        Multimap<String, String> groupWorkersMap = ArrayListMultimap.create();
        groupWorkersMap.put("DefaultGroup", "worker1");
        groupWorkersMap.put("DefaultGroup", "worker2");

        List<ExecutionMessage> assignMessages = new ArrayList<>();
        ExecutionMessage msg1 = new ExecutionMessage(1, ExecutionMessage.EMPTY_WORKER, "GroupX", "msg1", ExecStatus.PENDING, null, 0, (new Date(0)).getTime());
        assignMessages.add(msg1);

        Mockito.reset(executionQueueService);
        Mockito.reset(workerNodeService);
        Mockito.when(workerNodeService.readGroupWorkersMapActiveAndRunningAndVersion(engineVersionService.getEngineVersionId())).thenReturn(groupWorkersMap);
        Execution execution = Mockito.mock(Execution.class);
        Mockito.when(execution.getSystemContext()).thenReturn(new SystemContext());
        Mockito.when(executionMessageConverter.extractExecution(any(Payload.class))).thenReturn(execution);

        final List<ExecutionMessage> messagesInQ = executionAssignerService.assignWorkers(assignMessages);

        Assert.assertEquals(2, messagesInQ.size());

        ExecutionMessage finishMsg = messagesInQ.get(0);
        Assert.assertEquals(ExecStatus.FINISHED.getNumber(), finishMsg.getStatus().getNumber());
        Assert.assertEquals(1, finishMsg.getMsgSeqId());
        Assert.assertEquals("EMPTY", finishMsg.getWorkerId());

        ExecutionMessage FailedMsg = messagesInQ.get(1);
        Assert.assertEquals(ExecStatus.FAILED.getNumber(), FailedMsg.getStatus().getNumber());
        Assert.assertEquals(2, FailedMsg.getMsgSeqId());
        Assert.assertEquals("EMPTY", FailedMsg.getWorkerId());
    }

    @Configuration
    static class Configurator {

        @Bean
        public ExecutionAssignerService executionAssignerService() {
            return new ExecutionAssignerServiceImpl();
        }

        @Bean
        public WorkerNodeService workerNodeService() {
            return Mockito.mock(WorkerNodeService.class);
        }

        @Bean
        public ExecutionQueueService executionQueueService() {
            return Mockito.mock(ExecutionQueueService.class);
        }

        @Bean
        public ExecutionMessageConverter executionMessageConverter() {
            return Mockito.mock(ExecutionMessageConverter.class);
        }

        @Bean
        public TransactionTemplate transactionTemplate() {
            TransactionTemplate bean = new TransactionTemplate();
            bean.setTransactionManager(Mockito.mock(PlatformTransactionManager.class));
            return bean;
        }

        @Bean
        EngineVersionService engineVersionService(){
            EngineVersionService mock =  mock(EngineVersionService.class);

            when(mock.getEngineVersionId()).thenReturn("");

            return mock;
        }
    }
}
