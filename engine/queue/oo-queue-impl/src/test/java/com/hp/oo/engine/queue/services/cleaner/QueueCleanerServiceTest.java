package com.hp.oo.engine.queue.services.cleaner;

import com.hp.oo.engine.node.services.WorkerNodeService;
import com.hp.oo.engine.queue.entities.ExecStatus;
import com.hp.oo.engine.queue.entities.ExecutionMessage;
import com.hp.oo.engine.queue.entities.Payload;
import com.hp.oo.engine.queue.services.ExecutionQueueService;
import com.hp.oo.engine.versioning.services.VersionService;
import com.hp.oo.partitions.services.PartitionTemplate;
import com.hp.score.engine.data.IdentityGenerator;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: A
 * Date: 04/12/13
 * Time: 11:13
 * To change this template use File | Settings | File Templates.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class QueueCleanerServiceTest {


    @Autowired
    public ExecutionQueueService executionQueueService;

    @Autowired
    public QueueCleanerService queueCleanerService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    @Qualifier("OO_EXECUTION_STATES")
    private PartitionTemplate statePartitionTemplate;

    @Autowired
    @Qualifier("OO_EXECUTION_QUEUES")
    private PartitionTemplate queuePartitionTemplate;

    @Before
    public void before(){
        jdbcTemplate.execute("delete from OO_EXECUTION_QUEUES_1");
        jdbcTemplate.execute("delete from OO_EXECUTION_STATES_2");
        jdbcTemplate.execute("delete from OO_EXECUTION_STATES_1");
        // init queuePartitionTemplate
        Mockito.reset(queuePartitionTemplate);
        Mockito.when(queuePartitionTemplate.activeTable()).thenReturn("OO_EXECUTION_QUEUES_1");
        Mockito.when(queuePartitionTemplate.previousTable()).thenReturn("OO_EXECUTION_QUEUES_1");
        Mockito.when(queuePartitionTemplate.reversedTables()).thenReturn((Arrays.asList(
                "OO_EXECUTION_QUEUES_1",
                "OO_EXECUTION_QUEUES_1")));


        // init statePartitionTemplate
        Mockito.reset(statePartitionTemplate);
        Mockito.when(statePartitionTemplate.activeTable()).thenReturn("OO_EXECUTION_STATES_2");
        Mockito.when(statePartitionTemplate.previousTable()).thenReturn("OO_EXECUTION_STATES_1");
        Mockito.when(statePartitionTemplate.reversedTables()).thenReturn((Arrays.asList(
                "OO_EXECUTION_STATES_2",
                "OO_EXECUTION_STATES_1")));
       // Mockito.reset(workerNodeService);
    }

    @Test
    public void cleanTest() throws Exception{
        List<ExecutionMessage> msgs = new ArrayList<>();
        ExecutionMessage message15 = generateMessage(1,"group1","1",ExecStatus.IN_PROGRESS);
        ExecutionMessage message16 = generateMessage(1,"group1","1",ExecStatus.FINISHED);

        ExecutionMessage message25 = generateMessage(2,"group1","2",ExecStatus.IN_PROGRESS);
        ExecutionMessage message26 = generateMessage(2,"group1","2",ExecStatus.FINISHED);

        msgs.clear();
        msgs.add(message15);
        executionQueueService.enqueue(msgs);

        Set<Long> ids = queueCleanerService.getFinishedExecStateIds();
        Assert.assertEquals(0,ids.size());

        msgs.clear();
        msgs.add(message16);
        executionQueueService.enqueue(msgs);

        List<ExecutionMessage> exeMsgs = executionQueueService.poll("myWorker",100,ExecStatus.IN_PROGRESS,ExecStatus.FINISHED);

        ids = queueCleanerService.getFinishedExecStateIds();
        Assert.assertEquals(1,ids.size());

        msgs.clear();
        msgs.add(message26);
        executionQueueService.enqueue(msgs);

        ids = queueCleanerService.getFinishedExecStateIds();
        Assert.assertEquals(2,ids.size());

        msgs.clear();
        msgs.add(message25);
        executionQueueService.enqueue(msgs);

        ids = queueCleanerService.getFinishedExecStateIds();
        Assert.assertEquals(2,ids.size());

        queueCleanerService.cleanFinishedSteps(ids);

        ids = queueCleanerService.getFinishedExecStateIds();
        Assert.assertEquals(0,ids.size());
    }

    private ExecutionMessage generateMessage(long execStateId,String groupName,String msgId, ExecStatus status) {
        byte[] payloadData;
        payloadData = "This is just a test".getBytes();
        Payload payload = new Payload(false, false, payloadData);
        return new ExecutionMessage(execStateId, "myWorker", groupName, msgId , status, payload, 1);
    }


    @Configuration
    @ImportResource({"classpath:/META-INF/spring/queueSystemTestContext.xml"})
    static class Configurator{

        @Bean
        public IdentityGenerator<Long> identifierGenerator(){
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
        public QueueCleanerService queueCleanerService(){
            return new  QueueCleanerServiceImpl();
        }

        @Bean
        public WorkerNodeService workerNodeService(){
            return Mockito.mock(WorkerNodeService.class);
        }

        @Bean(name="OO_EXECUTION_STATES")
        public PartitionTemplate statePartitionTemplate(){
            return Mockito.mock(PartitionTemplate.class);
        }

        @Bean(name="OO_EXECUTION_QUEUES")
        public PartitionTemplate queuePartitionTemplate(){
            return Mockito.mock(PartitionTemplate.class);
        }

        @Bean
        public VersionService queueVersionService(){
            return Mockito.mock(VersionService.class);
        }
    }
}
