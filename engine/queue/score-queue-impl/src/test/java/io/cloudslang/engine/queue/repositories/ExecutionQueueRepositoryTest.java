/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package io.cloudslang.engine.queue.repositories;

import io.cloudslang.engine.data.IdentityGenerator;
import io.cloudslang.engine.node.services.WorkerNodeService;
import io.cloudslang.engine.queue.entities.ExecStatus;
import io.cloudslang.engine.queue.entities.ExecutionMessage;
import io.cloudslang.engine.queue.entities.Payload;
import io.cloudslang.engine.versioning.services.VersionService;
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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * User: wahnonm
 * Date: 29/10/13
 * Time: 16:03
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@Transactional
@TransactionConfiguration(defaultRollback = true)
public class ExecutionQueueRepositoryTest {

    @Autowired
    private ExecutionQueueRepository executionQueueRepository;


    @Test
    public void testInsert(){
        List<ExecutionMessage> msg = new ArrayList<>();
        msg.add(generateMessage("group1","msg1"));
        executionQueueRepository.insertExecutionQueue(msg,1L);

        List<ExecutionMessage> result = executionQueueRepository.pollMessagesWithoutAck(100,2);
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());

        ExecutionMessage resultMsg = result.get(0);
        Assert.assertEquals(ExecStatus.SENT,resultMsg.getStatus());
        Assert.assertEquals("group1",resultMsg.getWorkerGroup());
    }

    @Test
    public void testPollMessagesWithoutAckWithVersion(){
        List<ExecutionMessage> msg = new ArrayList<>();
        msg.add(generateMessage("group1","msg1"));
        executionQueueRepository.insertExecutionQueue(msg,1L);

        msg.clear();
        msg.add(generateMessage("group2","msg2"));
        executionQueueRepository.insertExecutionQueue(msg,4L);

        List<ExecutionMessage> result = executionQueueRepository.pollMessagesWithoutAck(100,3);
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());

        ExecutionMessage resultMsg = result.get(0);
        Assert.assertEquals(ExecStatus.SENT,resultMsg.getStatus());
        Assert.assertEquals("group1",resultMsg.getWorkerGroup());
    }

    @Test
    public void testGetFinishedExecStateIds(){
        List<ExecutionMessage> msg = new ArrayList<>();
        msg.add(generateFinishedMessage(1L));
        msg.add(generateFinishedMessage(1L));
        msg.add(generateFinishedMessage(2L));
        msg.add(generateFinishedMessage(3L));
        executionQueueRepository.insertExecutionQueue(msg,1L);

        Set<Long> result = executionQueueRepository.getFinishedExecStateIds();
        Assert.assertNotNull(result);
        Assert.assertEquals(3, result.size());
    }

    @Test
    public void testCountMessagesWithoutAckWithVersionForWorker(){
        List<ExecutionMessage> msg = new ArrayList<>();
        msg.add(generateMessageForWorker("group1","msg1",ExecutionMessage.EMPTY_WORKER));
        msg.add(generateMessageForWorker("group2","msg2","uuid2"));
        msg.add(generateMessageForWorker("group3","msg3",ExecutionMessage.EMPTY_WORKER));
        executionQueueRepository.insertExecutionQueue(msg,1L);

        msg.clear();
        msg.add(generateMessageForWorker("group2","msg2",ExecutionMessage.EMPTY_WORKER));

        executionQueueRepository.insertExecutionQueue(msg,4L);

        Integer result = executionQueueRepository.countMessagesWithoutAckForWorker(100,3,ExecutionMessage.EMPTY_WORKER);
        Assert.assertEquals(result.intValue(),2);

        result = executionQueueRepository.countMessagesWithoutAckForWorker(100,3,"uuid2");
        Assert.assertEquals(result.intValue(),1);
        result = executionQueueRepository.countMessagesWithoutAckForWorker(100,3,"uuid3");
        Assert.assertEquals(result.intValue(),0);
    }

    @Test
    public void testPollMessagesWithoutAckEmptyResult(){
        List<ExecutionMessage> msg = new ArrayList<>();
        msg.add(generateMessage("group1","msg1"));
        executionQueueRepository.insertExecutionQueue(msg,1L);

        List<ExecutionMessage> result = executionQueueRepository.pollMessagesWithoutAck(100,0);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void testPollForRecovery(){
        List<ExecutionMessage> msg = new ArrayList<>();
        ExecutionMessage execMsg = generateMessage("group1","msg1");
        execMsg.setWorkerId("worker1");
        execMsg.setStatus(ExecStatus.IN_PROGRESS);
        execMsg.incMsgSeqId();
        msg.add(execMsg);
        executionQueueRepository.insertExecutionStates(msg);
        executionQueueRepository.insertExecutionQueue(msg,1L);
        List<ExecutionMessage> result = executionQueueRepository.poll("worker1",10,ExecStatus.IN_PROGRESS);

        Assert.assertNotNull(result);
        Assert.assertFalse(result.isEmpty());
    }

    @Test
    public void testPollForRecoveryDuplicateMsg(){

        //insert to states table
        List<ExecutionMessage> msg = new ArrayList<>();
        ExecutionMessage execMsg = generateMessage("group1","msg1");
        execMsg.setWorkerId("worker1");
        execMsg.setStatus(ExecStatus.IN_PROGRESS);
        msg.add(execMsg);
        executionQueueRepository.insertExecutionStates(msg);
        executionQueueRepository.insertExecutionQueue(msg,1L);


        List<ExecutionMessage> result = executionQueueRepository.poll("worker1",10,ExecStatus.IN_PROGRESS);


        Assert.assertNotNull(result);
        Assert.assertFalse(result.isEmpty());
        Assert.assertEquals("should find only 1 msg result!, since it is the same msg, no duplication",1,result.size());
    }

    @Test
    public void testPollForRecoveryDuplicateMsg2(){

        //insert to states table
        List<ExecutionMessage> msg = new ArrayList<>();
        ExecutionMessage execMsg = generateMessage("group1","msg1");
        execMsg.setWorkerId("worker1");
        execMsg.setStatus(ExecStatus.IN_PROGRESS);
        msg.add(execMsg);
        executionQueueRepository.insertExecutionStates(msg);
        executionQueueRepository.insertExecutionQueue(msg,1L);

        List<ExecutionMessage> result = executionQueueRepository.poll("worker1",10,ExecStatus.IN_PROGRESS);


        Assert.assertNotNull(result);
        Assert.assertFalse(result.isEmpty());
        Assert.assertEquals("should find only 1 msg result!, since the second msg has higher msg seq id",1,result.size());
    }

    @Test
    public void testPoll(){
        List<ExecutionMessage> msg = new ArrayList<>();
        ExecutionMessage execMsg = generateMessage("group1","msg1");
        execMsg.setWorkerId("worker1");
        execMsg.setStatus(ExecStatus.IN_PROGRESS);
        msg.add(execMsg);
        executionQueueRepository.insertExecutionQueue(msg,1L);
        executionQueueRepository.insertExecutionStates(msg);
        List<ExecutionMessage> result = executionQueueRepository.poll(new Date(0), "worker1", 10, ExecStatus.IN_PROGRESS);

        Assert.assertNotNull(result);
        Assert.assertFalse(result.isEmpty());
    }

    private ExecutionMessage generateMessage(String groupName,String msgId) {
        byte[] payloadData;
        payloadData = "This is just a test".getBytes();
        Payload payload = new Payload(false, false, payloadData);
        return new ExecutionMessage(-1, ExecutionMessage.EMPTY_WORKER, groupName, msgId , ExecStatus.SENT, payload, 1);
    }

    private ExecutionMessage generateFinishedMessage(long execStateId) {
        byte[] payloadData;
        payloadData = "This is just a test".getBytes();
        Payload payload = new Payload(false, false, payloadData);
        return new ExecutionMessage(execStateId, ExecutionMessage.EMPTY_WORKER, "group", "123" , ExecStatus.FINISHED, payload, 1);
    }


    private ExecutionMessage generateMessageForWorker(String groupName,String msgId, String workerUuid) {
        byte[] payloadData;
        payloadData = "This is just a test".getBytes();
        Payload payload = new Payload(false, false, payloadData);
        return new ExecutionMessage(-1, workerUuid, groupName, msgId , ExecStatus.SENT, payload, 1);
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


    }
}
