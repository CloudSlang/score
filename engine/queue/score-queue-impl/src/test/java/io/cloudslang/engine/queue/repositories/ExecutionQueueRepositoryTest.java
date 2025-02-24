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

package io.cloudslang.engine.queue.repositories;

import io.cloudslang.engine.data.IdentityGenerator;
import io.cloudslang.engine.node.services.WorkerNodeService;
import io.cloudslang.engine.queue.QueueTestsUtils;
import io.cloudslang.engine.queue.entities.ExecStatus;
import io.cloudslang.engine.queue.entities.ExecutionMessage;
import io.cloudslang.engine.queue.entities.Payload;
import io.cloudslang.engine.versioning.services.VersionService;
import io.cloudslang.orchestrator.services.ExecutionStateService;
import junit.framework.Assert;
import liquibase.integration.spring.SpringLiquibase;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static io.cloudslang.engine.queue.entities.ExecutionMessage.EMPTY_WORKER;
import static java.util.Comparator.comparingInt;

/**
 * User: wahnonm
 * Date: 29/10/13
 * Time: 16:03
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@Transactional
@Rollback
public class ExecutionQueueRepositoryTest {

    private static final int WORKER_POLLING_MEMORY = 10000000;

    @Autowired
    private ExecutionQueueRepository executionQueueRepository;

    @Autowired
    private ExecutionStateService executionStateService;

    @Test
    public void testInsert(){
        List<ExecutionMessage> msg = new ArrayList<>();
        msg.add(QueueTestsUtils.generateMessage("group1","msg1", 1));
        executionQueueRepository.insertExecutionQueue(msg,1L);

        List<ExecutionMessage> result = executionQueueRepository.pollMessagesWithoutAck(100,2);
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());

        ExecutionMessage resultMsg = result.get(0);
        Assert.assertEquals(ExecStatus.SENT,resultMsg.getStatus());
        Assert.assertEquals("group1",resultMsg.getWorkerGroup());
    }

    @Test(expected = RuntimeException.class)
    public void testInsertFailureDueToUniqueConstraint(){
        List<ExecutionMessage> msg = new ArrayList<>();
        msg.add(QueueTestsUtils.generateMessage("group1","msg1", 1));
        msg.add(QueueTestsUtils.generateMessage("group1","msg1", 1));
        executionQueueRepository.insertExecutionQueue(msg,1L);
    }

    @Test
    public void testPollMessagesWithoutAckWithVersion(){
        List<ExecutionMessage> msg = new ArrayList<>();
        msg.add(QueueTestsUtils.generateMessage(1, "group1", "msg1", 1));
        executionQueueRepository.insertExecutionQueue(msg, 1L);

        msg.clear();
        msg.add(QueueTestsUtils.generateMessage(2, "group2","msg2", 1));
        executionQueueRepository.insertExecutionQueue(msg,4L);

        List<ExecutionMessage> result = executionQueueRepository.pollMessagesWithoutAck(100,3);
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());

        ExecutionMessage resultMsg = result.get(0);
        Assert.assertEquals(ExecStatus.SENT, resultMsg.getStatus());
        Assert.assertEquals("group1",resultMsg.getWorkerGroup());
    }

    @Test
    public void testGetFinishedExecStateIds(){
        List<ExecutionMessage> msg = new ArrayList<>();
        msg.add(generateFinishedMessage(1L, 1));
        msg.add(generateFinishedMessage(1L, 2));
        msg.add(generateFinishedMessage(2L, 3));
        msg.add(generateFinishedMessage(3L, 4));
        executionQueueRepository.insertExecutionQueue(msg, 1L);

        Set<Long> result = executionQueueRepository.getFinishedExecStateIds();
        Assert.assertNotNull(result);
        Assert.assertEquals(3, result.size());
    }

    @Test
    public void testCountMessagesWithoutAckWithVersionForWorker(){
        List<ExecutionMessage> msg = new ArrayList<>();
        msg.add(generateMessageForWorker(1, "group1", "msg1", EMPTY_WORKER, 1));
        msg.add(generateMessageForWorker(2, "group2", "msg2", "uuid2", 1));
        msg.add(generateMessageForWorker(3, "group3","msg3", EMPTY_WORKER, 1));
        executionQueueRepository.insertExecutionQueue(msg,1L);
        executionQueueRepository.insertExecutionStates(msg);

        msg.clear();
        msg.add(generateMessageForWorker(4, "group2","msg2", EMPTY_WORKER, 1));

        executionQueueRepository.insertExecutionQueue(msg,4L);
        executionQueueRepository.insertExecutionStates(msg);

        Integer result = executionQueueRepository.countMessagesWithoutAckForWorker(100,3, EMPTY_WORKER);
        Assert.assertEquals(result.intValue(),2);

        result = executionQueueRepository.countMessagesWithoutAckForWorker(100,3,"uuid2");
        Assert.assertEquals(result.intValue(),1);
        result = executionQueueRepository.countMessagesWithoutAckForWorker(100,3,"uuid3");
        Assert.assertEquals(result.intValue(), 0);
    }

    @Test
    public void testPollMessagesWithoutAckEmptyResult(){
        List<ExecutionMessage> msg = new ArrayList<>();
        msg.add(QueueTestsUtils.generateMessage("group1","msg1", 1));
        executionQueueRepository.insertExecutionQueue(msg, 1L);

        List<ExecutionMessage> result = executionQueueRepository.pollMessagesWithoutAck(100,0);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void testPollForRecovery(){
        List<ExecutionMessage> msg = new ArrayList<>();
        ExecutionMessage execMsg = QueueTestsUtils.generateMessage("group1","msg1", 1);
        execMsg.setWorkerId("worker1");
        execMsg.setStatus(ExecStatus.IN_PROGRESS);
        execMsg.incMsgSeqId();
        msg.add(execMsg);
        executionQueueRepository.insertExecutionStates(msg);
        executionQueueRepository.insertExecutionQueue(msg,1L);
        List<ExecutionMessage> result = executionQueueRepository.pollRecovery("worker1", 10, ExecStatus.IN_PROGRESS);

        Assert.assertNotNull(result);
        Assert.assertFalse(result.isEmpty());
    }

    @Test
    public void testPollForRecoveryDuplicateMsg(){

        //insert to states table
        List<ExecutionMessage> msg = new ArrayList<>();
        ExecutionMessage execMsg = QueueTestsUtils.generateMessage("group1","msg1", 1);
        execMsg.setWorkerId("worker1");
        execMsg.setStatus(ExecStatus.IN_PROGRESS);
        msg.add(execMsg);
        executionQueueRepository.insertExecutionStates(msg);
        executionQueueRepository.insertExecutionQueue(msg,1L);


        List<ExecutionMessage> result = executionQueueRepository.pollRecovery("worker1", 10, ExecStatus.IN_PROGRESS);


        Assert.assertNotNull(result);
        Assert.assertFalse(result.isEmpty());
        Assert.assertEquals("should find only 1 msg result!, since it is the same msg, no duplication",1,result.size());
    }

    @Test
    public void testPollForRecoveryDuplicateMsg2(){

        //insert to states table
        List<ExecutionMessage> msg = new ArrayList<>();
        ExecutionMessage execMsg = QueueTestsUtils.generateMessage("group1","msg1", 1);
        execMsg.setWorkerId("worker1");
        execMsg.setStatus(ExecStatus.IN_PROGRESS);
        msg.add(execMsg);
        executionQueueRepository.insertExecutionStates(msg);
        executionQueueRepository.insertExecutionQueue(msg,1L);

        List<ExecutionMessage> result = executionQueueRepository.pollRecovery("worker1", 10, ExecStatus.IN_PROGRESS);


        Assert.assertNotNull(result);
        Assert.assertFalse(result.isEmpty());
        Assert.assertEquals("should find only 1 msg result!, since the second msg has higher msg seq id", 1, result.size());
    }

    @Test
    public void testPoll(){
        List<ExecutionMessage> msg = new ArrayList<>();
        ExecutionMessage execMsg = QueueTestsUtils.generateMessage("group1","msg1", 1);
        execMsg.setWorkerId("worker1");
        execMsg.setStatus(ExecStatus.IN_PROGRESS);
        msg.add(execMsg);
        executionQueueRepository.insertExecutionQueue(msg,1L);
        executionQueueRepository.insertExecutionStates(msg);
        List<ExecutionMessage> result = executionQueueRepository.poll("worker1", 10,
                WORKER_POLLING_MEMORY, ExecStatus.IN_PROGRESS);

        Assert.assertNotNull(result);
        Assert.assertFalse(result.isEmpty());
    }

    @Test
    public void testPollLargeMessage() {

        int mb = 2;

        ExecutionMessage msg = QueueTestsUtils.generateMessage(1, "group1","msg1", 1);
        msg.setWorkerId("worker1");
        msg.setStatus(ExecStatus.IN_PROGRESS);

        ExecutionMessage largeMessage = QueueTestsUtils.generateLargeMessage(2, "group1","msg2", 2, QueueTestsUtils.getMB(mb));
        largeMessage.setWorkerId("worker1");
        largeMessage.setStatus(ExecStatus.IN_PROGRESS);

        List<ExecutionMessage> messages = new ArrayList<>();
        messages.add(msg);
        messages.add(largeMessage);

        executionQueueRepository.insertExecutionQueue(messages,1L);
        executionQueueRepository.insertExecutionStates(messages);

        long workerFreeMem = QueueTestsUtils.getMB(mb - 1);
        List<ExecutionMessage> result = executionQueueRepository.poll("worker1", 10, workerFreeMem, ExecStatus.IN_PROGRESS);

        Assert.assertNotNull(result);
        Assert.assertFalse(result.isEmpty());

        ExecutionMessage resultMsg = result.get(0);
        Assert.assertEquals(ExecStatus.IN_PROGRESS, resultMsg.getStatus());
        Assert.assertEquals(msg.getWorkerGroup(), resultMsg.getWorkerGroup());
        Assert.assertEquals(msg.getMsgId(), resultMsg.getMsgId());
        Assert.assertEquals(msg.getMsgSeqId(), resultMsg.getMsgSeqId());
    }

    @Test
    public void testFindOldMessages() throws InterruptedException {
        List<ExecutionMessage> messages = new ArrayList<>();

        String msgId = "22";
        int msg_seq_id = 1;
        ExecutionMessage m1 = QueueTestsUtils.generateMessage("group1", msgId, msg_seq_id++, "worker1", ExecStatus.ASSIGNED);
        ExecutionMessage m2 = QueueTestsUtils.generateMessage("group1", msgId, msg_seq_id++, "worker1", ExecStatus.ASSIGNED);

        messages.add(m1);
        messages.add(m2);

        executionQueueRepository.insertExecutionQueue(messages,1L);

        // cannot find 1 sec old messages
        List<ExecutionMessage> oldMessages = executionQueueRepository.findOldMessages(System.currentTimeMillis() - 1000);
        Assert.assertEquals(0, oldMessages.size());

        // find previously inserted messages
        oldMessages = executionQueueRepository.findOldMessages(System.currentTimeMillis());
        Assert.assertEquals(2, oldMessages.size());

        Assert.assertEquals(messages.size(), oldMessages.size());

        Collections.sort(oldMessages, comparingInt(ExecutionMessage::getMsgSeqId));

        compareExecutionMessages(m1, oldMessages.get(0));
        compareExecutionMessages(m2, oldMessages.get(1));
    }

    private void compareExecutionMessages(ExecutionMessage m, ExecutionMessage om) {
        Assert.assertEquals(m.getExecStateId(), om.getExecStateId());
        Assert.assertEquals(m.getMsgSeqId(), om.getMsgSeqId());
        Assert.assertEquals(m.getWorkerId(), om.getWorkerId());
        Assert.assertEquals(m.getStatus(), om.getStatus());
    }

    @Test
    public void testGetBusyWorkersBusyWorker(){
        List<ExecutionMessage> msg = new ArrayList<>();
        ExecutionMessage execMsg = QueueTestsUtils.generateMessage("group1","msg1", 1);
        execMsg.setWorkerId("worker1");
        execMsg.setStatus(ExecStatus.IN_PROGRESS);
        execMsg.incMsgSeqId();
        msg.add(execMsg);
        executionQueueRepository.insertExecutionStates(msg);
        executionQueueRepository.insertExecutionQueue(msg,1L);
        List<String> busyWorkers = executionQueueRepository.getBusyWorkers(ExecStatus.ASSIGNED);
        Assert.assertNotNull(busyWorkers);
    }

    private ExecutionMessage generateFinishedMessage(long execStateId, int msg_seq_id) {
        byte[] payloadData;
        payloadData = "This is just a test".getBytes();
        Payload payload = new Payload(payloadData);
        return new ExecutionMessage(execStateId, EMPTY_WORKER, "group", "123" , ExecStatus.FINISHED, payload, msg_seq_id);
    }


    private ExecutionMessage generateMessageForWorker(long exec_state_id, String groupName,String msgId, String workerUuid, int msg_seq_id) {
        byte[] payloadData;
        payloadData = "This is just a test".getBytes();
        Payload payload = new Payload(payloadData);
        return new ExecutionMessage(exec_state_id, workerUuid, groupName, msgId , ExecStatus.SENT, payload, msg_seq_id);
    }

    @Configuration
    @EnableTransactionManagement
    static class Configurator {
	    @Autowired
	    private DataSource dataSource;

        @Bean
        DataSource dataSource(){
            BasicDataSource ds = new BasicDataSource();
            ds.setDriverClassName("org.h2.Driver");
            ds.setUrl("jdbc:h2:mem:test");
            ds.setUsername("sa");
            ds.setPassword("sa");
            ds.setDefaultAutoCommit(false);
            return new TransactionAwareDataSourceProxy(ds);
        }

	    @Bean
	    SpringLiquibase liquibase(){
		    SpringLiquibase liquibase = new SpringLiquibase();
		    liquibase.setDataSource(dataSource);
		    liquibase.setChangeLog("classpath:/META-INF/database/test.changes.xml");
		    return liquibase;
	    }

        @Bean(name="transactionManager")
        PlatformTransactionManager txManager() {
            return new DataSourceTransactionManager(dataSource());
        }

        @Bean
        IdentityGenerator identifierGenerator(){
            return new IdentityGenerator() {
                long id = 1;
                @Override
                public synchronized Long next() {
                    return id++;
                }

                @Override
                public List<Long> bulk(int bulkSize) {
                    return null;
                }
            };
        }

        @Bean
        ExecutionQueueRepository executionQueueRepository(){
            return new ExecutionQueueRepositoryImpl();
        }

        @Bean
        WorkerNodeService workerNodeService(){
            return Mockito.mock(WorkerNodeService.class);
        }

        @Bean
        VersionService queueVersionService(){
            return Mockito.mock(VersionService.class);
        }

        @Bean
        ExecutionStateService executionStateService() { return Mockito.mock(ExecutionStateService.class); }
    }
}
