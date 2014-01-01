package com.hp.oo.engine.queue.services;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.hp.oo.engine.node.services.WorkerNodeService;
import com.hp.oo.engine.queue.entities.ExecStatus;
import com.hp.oo.engine.queue.entities.ExecutionMessage;
import com.hp.oo.engine.queue.entities.Payload;
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
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Amit Levin
 * Date: 21/11/12
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class ExecutionQueueServiceTest {

	@Autowired
	public ExecutionQueueService executionQueueService;

	@Autowired
	public WorkerNodeService workerNodeService;

    @Autowired
	@Qualifier("OO_EXECUTION_STATES")
	private PartitionTemplate statePartitionTemplate;

	@Autowired
	@Qualifier("OO_EXECUTION_QUEUES")
	private PartitionTemplate queuePartitionTemplate;

	@Autowired
	private JdbcTemplate jdbcTemplate;

    @Autowired
    private VersionService versionService;

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
        Mockito.reset(workerNodeService);
	}



	@Test
	public void enqueueTest() throws Exception {
        Multimap<String, String> groupWorkerMap = ArrayListMultimap.create();
        groupWorkerMap.put("group1", "worker3");
        groupWorkerMap.put("group2", "worker3");
        Mockito.when(workerNodeService.readGroupWorkersMapActiveAndRunning()).thenReturn(groupWorkerMap);



		ExecutionMessage message1 = generateMessage("group1","11");
		ExecutionMessage message2 = generateMessage("group2","12");
		List<ExecutionMessage> msgs = new ArrayList<>();
		msgs.add(message1);
		msgs.add(message2);
		executionQueueService.enqueue(msgs);


		List<ExecutionMessage> msgInQueue, msgFromQueue;
		msgInQueue = executionQueueService.poll("worker3", 100, ExecStatus.ASSIGNED);
		Assert.assertEquals(2, msgInQueue.size());
		Assert.assertNotNull(msgInQueue.get(0).getPayload().getData());

		msgInQueue = updateMessages(msgInQueue, ExecStatus.ASSIGNED, "worker1");
		executionQueueService.enqueue(msgInQueue);
		msgFromQueue = executionQueueService.poll("worker1", 100, ExecStatus.ASSIGNED);
		Assert.assertEquals(2, msgFromQueue.size());
		msgFromQueue = executionQueueService.poll("worker2", 100, ExecStatus.ASSIGNED);
		Assert.assertEquals(0, msgFromQueue.size());


		msgInQueue = updateMessages(msgInQueue, ExecStatus.SENT, "worker1");
		executionQueueService.enqueue(msgInQueue);
		msgFromQueue = executionQueueService.poll("worker1", 100, ExecStatus.SENT);
		Assert.assertEquals(ExecStatus.SENT, msgFromQueue.get(0).getStatus());
		Assert.assertEquals(2, msgFromQueue.size());

		msgInQueue = updateMessages(msgInQueue, ExecStatus.IN_PROGRESS, "worker1");
		executionQueueService.enqueue(msgInQueue);
		msgFromQueue = executionQueueService.poll("worker1", 100, ExecStatus.IN_PROGRESS);
		Assert.assertEquals(ExecStatus.IN_PROGRESS, msgFromQueue.get(0).getStatus());
		Assert.assertEquals(2, msgFromQueue.size());

		msgInQueue = updateMessages(msgInQueue, ExecStatus.FINISHED, "worker1");
		executionQueueService.enqueue(msgInQueue);
		msgFromQueue = executionQueueService.poll("worker1", 100, ExecStatus.FINISHED);
		Assert.assertEquals(ExecStatus.FINISHED, msgFromQueue.get(0).getStatus());
		Assert.assertEquals(2, msgFromQueue.size());

	}

    @Test
	public void pollWithoutAckTest() throws Exception {
        Multimap<String, String> groupWorkerMap = ArrayListMultimap.create();
        groupWorkerMap.put("group1", "worker1");
        groupWorkerMap.put("group1", "worker2");
        Mockito.when(workerNodeService.readGroupWorkersMapActiveAndRunning()).thenReturn(groupWorkerMap);

        Mockito.when(versionService.getCurrentVersion(Mockito.anyString())).thenReturn(0L);

        List<ExecutionMessage> msgInQueue = executionQueueService.pollMessagesWithoutAck(100,0);
		Assert.assertEquals(0, msgInQueue.size());

	    ExecutionMessage message1 = generateMessage("group1","5");
        message1.setWorkerId("worker1");
	    message1.setStatus(ExecStatus.SENT);

	    msgInQueue.clear();
	    msgInQueue.add(message1);

	    executionQueueService.enqueue(msgInQueue);

        //now we set current system version(100) to be mush higher then msg version (0)
	    msgInQueue = executionQueueService.pollMessagesWithoutAck(100,100);
	    Assert.assertEquals(1, msgInQueue.size());

	}

    @Test
    public void pollWithoutAckTestInProgressState() throws Exception {
        Multimap<String, String> groupWorkerMap = ArrayListMultimap.create();
        groupWorkerMap.put("group1", "worker1");
        groupWorkerMap.put("group1", "worker2");
        Mockito.when(workerNodeService.readGroupWorkersMapActiveAndRunning()).thenReturn(groupWorkerMap);

        Mockito.when(versionService.getCurrentVersion(Mockito.anyString())).thenReturn(0L);

        List<ExecutionMessage> msgInQueue = executionQueueService.pollMessagesWithoutAck(100,0);
        Assert.assertEquals(0, msgInQueue.size());

        ExecutionMessage message1 = generateMessage("group1","5");
        message1.setWorkerId("worker1");
        message1.setStatus(ExecStatus.IN_PROGRESS);

        msgInQueue.clear();
        msgInQueue.add(message1);

        executionQueueService.enqueue(msgInQueue);

        //now we set current system version(100) to be mush higher then msg version (0)
        msgInQueue = executionQueueService.pollMessagesWithoutAck(100,100);
        Assert.assertEquals("since we sent a msg in IN_PROGRESS status, pollMessagesWithoutAck should not find it",0, msgInQueue.size());
    }

    @Test
    public void pollWithoutAckTestMixMsg() throws Exception {
        Multimap<String, String> groupWorkerMap = ArrayListMultimap.create();
        groupWorkerMap.put("group1", "worker1");
        groupWorkerMap.put("group1", "worker2");
        Mockito.when(workerNodeService.readGroupWorkersMapActiveAndRunning()).thenReturn(groupWorkerMap);

        List<ExecutionMessage> msgInQueue = executionQueueService.pollMessagesWithoutAck(100,0);
        Assert.assertEquals(0, msgInQueue.size());

        ExecutionMessage message1 = generateMessage("group1","5"); //this msg will get 0 version
        message1.setWorkerId("worker1");
        message1.setStatus(ExecStatus.SENT);

        msgInQueue.clear();
        msgInQueue.add(message1);

        Mockito.when(versionService.getCurrentVersion(Mockito.anyString())).thenReturn(0L);

        executionQueueService.enqueue(msgInQueue);

        ExecutionMessage message2 = generateMessage("group1","5");   //this msg will get 100 version
        message2.setWorkerId("worker2");
        message2.setStatus(ExecStatus.SENT);

        msgInQueue.clear();
        msgInQueue.add(message2);
        Mockito.when(versionService.getCurrentVersion(Mockito.anyString())).thenReturn(100L);

        executionQueueService.enqueue(msgInQueue);

        msgInQueue = executionQueueService.pollMessagesWithoutAck(100,100);
        Assert.assertEquals("only one msg should be with version to far from system version",1, msgInQueue.size());
        Assert.assertEquals("worker1",msgInQueue.get(0).getWorkerId());
    }


	@Test
	public void getNumOfEvents() throws Exception {
		executionQueueService.getNumOfEvents(ExecStatus.SENT,"W1");
	}


	@Test
	public void readPayloadByIds(){
        Multimap<String, String> groupWorkerMap = ArrayListMultimap.create();;
              groupWorkerMap.put("group1","worker1");
              groupWorkerMap.put("group1","worker2");
              groupWorkerMap.put("group2","worker1");
              groupWorkerMap.put("group2","worker2");
              Mockito.reset(workerNodeService);
              Mockito.when(workerNodeService.readGroupWorkersMapActiveAndRunning()).thenReturn(groupWorkerMap);

		ExecutionMessage message1 = generateMessage("group1","6");
		ExecutionMessage message2 = generateMessage("group2","6");
		List<ExecutionMessage> msgs = new ArrayList<>();
		msgs.add(message1);
		msgs.add(message2);
		executionQueueService.enqueue(msgs);

		Map<Long, Payload> payloadMap = executionQueueService.readPayloadByExecutionIds(message1.getExecStateId(), message2.getExecStateId());

		Assert.assertEquals(2, payloadMap.size());
		Assert.assertTrue(payloadMap.containsKey(message1.getExecStateId()));
		Assert.assertTrue(payloadMap.containsKey(message2.getExecStateId()));
		Assert.assertEquals(message1.getPayload(), payloadMap.get(message1.getExecStateId()));
		Assert.assertEquals(message2.getPayload(), payloadMap.get(message2.getExecStateId()));
	}

	private ExecutionMessage generateMessage(String groupName,String msgId) {
		byte[] payloadData;
		payloadData = "This is just a test".getBytes();
		Payload payload = new Payload(false, false, payloadData);
		return new ExecutionMessage(-1, ExecutionMessage.EMPTY_WORKER, groupName, msgId , ExecStatus.PENDING, payload, 1);
	}

	private List<ExecutionMessage> updateMessages(List<ExecutionMessage> messages, ExecStatus newStatus, String workerName) {
		for (ExecutionMessage msg : messages) {
			msg.setStatus(newStatus);
			msg.setWorkerId(workerName);
			msg.incMsgSeqId();
            msg.setPayload(null);
		}
		return messages;
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
