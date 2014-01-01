package com.hp.oo.engine.queue.repositories;

import com.hp.oo.engine.node.services.WorkerNodeService;
import com.hp.oo.engine.queue.entities.ExecStatus;
import com.hp.oo.engine.queue.entities.ExecutionMessage;
import com.hp.oo.engine.queue.entities.Payload;
import com.hp.oo.engine.versioning.services.VersionService;
import com.hp.oo.partitions.services.PartitionTemplate;
import com.hp.score.engine.data.IdentityGenerator;
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
import java.util.List;

import static org.mockito.Mockito.when;

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
    public void testPollMessagesWithoutAckEmptyResult(){
        List<ExecutionMessage> msg = new ArrayList<>();
        msg.add(generateMessage("group1","msg1"));
        executionQueueRepository.insertExecutionQueue(msg,1L);

        List<ExecutionMessage> result = executionQueueRepository.pollMessagesWithoutAck(100,0);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.isEmpty());
    }

    private ExecutionMessage generateMessage(String groupName,String msgId) {
        byte[] payloadData;
        payloadData = "This is just a test".getBytes();
        Payload payload = new Payload(false, false, payloadData);
        return new ExecutionMessage(-1, ExecutionMessage.EMPTY_WORKER, groupName, msgId , ExecStatus.SENT, payload, 1);
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
        IdentityGenerator<Long> identifierGenerator(){
            return new IdentityGenerator<Long>() {
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

        @Bean(name="OO_EXECUTION_STATES")
        PartitionTemplate statePartitionTemplate(){
            PartitionTemplate partitionTemplate = Mockito.mock(PartitionTemplate.class);
            when(partitionTemplate.activeTable()).thenReturn("OO_EXECUTION_STATES_1");
            return  partitionTemplate;
        }

        @Bean(name="OO_EXECUTION_QUEUES")
        PartitionTemplate queuePartitionTemplate(){
            return Mockito.mock(PartitionTemplate.class);
        }

        @Bean
        VersionService queueVersionService(){
            return Mockito.mock(VersionService.class);
        }


    }
}
