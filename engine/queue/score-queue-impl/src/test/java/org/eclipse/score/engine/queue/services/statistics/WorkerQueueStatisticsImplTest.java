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
package org.eclipse.score.engine.queue.services.statistics;

import org.eclipse.score.engine.queue.entities.ExecStatus;
import org.eclipse.score.engine.queue.entities.ExecutionMessage;
import org.eclipse.score.engine.queue.services.QueueListener;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

/**
 * User: wahnonm
 * Date: 11/08/13
 * Time: 11:07
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class WorkerQueueStatisticsImplTest {

    @InjectMocks
    private QueueListener queueStatistics = new WorkerQueueStatisticsImpl();

    @Configuration
    static class EmptyConfig {}

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testOnEnqueue() throws Exception {
        List<ExecutionMessage> msgList = new ArrayList<>();

        queueStatistics.onEnqueue(msgList,0);

        Assert.assertEquals(0, ((WorkerQueueStatistics) queueStatistics).getFinalCounter()) ;
        Assert.assertEquals(0,((WorkerQueueStatistics) queueStatistics).getFinishedCounter()) ;

        ExecutionMessage finishMsg = new ExecutionMessage();
        finishMsg.setStatus(ExecStatus.FINISHED);

        ExecutionMessage finalMsg = new ExecutionMessage();
        finalMsg.setStatus(ExecStatus.TERMINATED);

        msgList.add(finalMsg) ;
        msgList.add(finishMsg) ;

        queueStatistics.onEnqueue(msgList,0);

        Assert.assertEquals(1, ((WorkerQueueStatistics) queueStatistics).getFinalCounter()) ;
        Assert.assertEquals(1,((WorkerQueueStatistics) queueStatistics).getFinishedCounter()) ;
    }
}
