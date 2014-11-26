/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package org.eclipse.score.engine.queue.services;

import org.eclipse.score.engine.queue.entities.ExecStatus;
import org.eclipse.score.engine.queue.entities.ExecutionMessage;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.*;

/**
 * User: wahnonm
 * Date: 07/08/13
 * Time: 16:21
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class QueueDispatcherServiceTest {



    @Mock
    private ExecutionQueueService executionQueueService;


    @InjectMocks
    private QueueDispatcherService queueDispatcherService = new QueueDispatcherServiceImpl();

    @Configuration
    static class EmptyConfig {}

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }


    @Test
    public void testDispatchEmptyValues() throws Exception {
        List<ExecutionMessage> msg = new ArrayList<>();
        queueDispatcherService.dispatch(msg);
        verify(executionQueueService,never()).enqueue(anyList());

        queueDispatcherService.dispatch(null);
        verify(executionQueueService,never()).enqueue(anyList());
    }

    @Test
    public void testDispatch() throws Exception {
        List<ExecutionMessage> msg = new ArrayList<>();
        msg.add(new ExecutionMessage());

        queueDispatcherService.dispatch(msg);
        verify(executionQueueService,times(1)).enqueue(anyList());
    }

    @Test
    public void testPoll() throws Exception {
        Date now = new Date();
        queueDispatcherService.poll("workerId",5,now);
        verify(executionQueueService,times(1)).poll(now,"workerId",5, ExecStatus.ASSIGNED);
    }

    @Test
    public void testPollEmptyResult() throws Exception {
        Date now = new Date();
        List<ExecutionMessage> msg = new ArrayList<>();

        when(executionQueueService.poll(now,"workerId",5, ExecStatus.ASSIGNED)).thenReturn(msg);
        List<ExecutionMessage> result = queueDispatcherService.poll("workerId",5,now);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void testPollWithResult() throws Exception {
        Date now = new Date();
        List<ExecutionMessage> msg = new ArrayList<>();

        msg.add(new ExecutionMessage());
        msg.get(0).setMsgId("id1");

        msg.add(new ExecutionMessage());
        msg.get(1).setMsgId("id2");

        when(executionQueueService.poll(now, "workerId", 5, ExecStatus.ASSIGNED)).thenReturn(msg);
        List<ExecutionMessage> result = queueDispatcherService.poll("workerId",5,now);
        Assert.assertEquals(2,result.size());
        Assert.assertEquals("id1",result.get(0).getMsgId());
        Assert.assertEquals("id2",result.get(1).getMsgId());
    }
}
