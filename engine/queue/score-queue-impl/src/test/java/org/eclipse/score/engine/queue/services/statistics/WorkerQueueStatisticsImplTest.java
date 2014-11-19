/*
 * Licensed to Hewlett-Packard Development Company, L.P. under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
*/
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
