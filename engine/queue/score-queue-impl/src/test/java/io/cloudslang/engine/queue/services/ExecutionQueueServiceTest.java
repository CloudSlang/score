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

package io.cloudslang.engine.queue.services;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import io.cloudslang.engine.data.IdentityGenerator;
import io.cloudslang.engine.data.SimpleHiloIdentifierGenerator;
import io.cloudslang.engine.node.services.WorkerNodeService;
import io.cloudslang.engine.queue.entities.ExecStatus;
import io.cloudslang.engine.queue.entities.ExecutionMessage;
import io.cloudslang.engine.queue.entities.ExecutionMessageConverter;
import io.cloudslang.engine.queue.entities.Payload;
import io.cloudslang.engine.queue.repositories.ExecutionQueueRepository;
import io.cloudslang.engine.queue.repositories.ExecutionQueueRepositoryImpl;
import io.cloudslang.engine.queue.services.assigner.ExecutionAssignerService;
import io.cloudslang.engine.queue.services.assigner.ExecutionAssignerServiceImpl;
import io.cloudslang.engine.versioning.services.VersionService;
import io.cloudslang.orchestrator.services.EngineVersionService;
import junit.framework.Assert;
import io.cloudslang.orchestrator.services.ExecutionStateService;
import liquibase.integration.spring.SpringLiquibase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

/**
 * Created by IntelliJ IDEA.
 * User:
 * Date: 21/11/12
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class ExecutionQueueServiceTest {

	@Autowired
	public ExecutionQueueService executionQueueService;

	@Autowired
	private ExecutionStateService executionStateService;

	@Autowired
	public WorkerNodeService workerNodeService;

	@Autowired
	private BusyWorkersService busyWorkersService;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private VersionService versionService;

	@Autowired
	private EngineVersionService engineVersionService;

	@Before
	public void before() {
		jdbcTemplate.execute("delete from OO_EXECUTION_QUEUES");
		jdbcTemplate.execute("delete from OO_EXECUTION_STATES");

		reset(workerNodeService, engineVersionService);
	}


	@Test
	public void enqueueTest() throws Exception {
		Multimap<String, String> groupWorkerMap = ArrayListMultimap.create();
		groupWorkerMap.put("group1", "worker3");
		groupWorkerMap.put("group2", "worker3");
		when(workerNodeService.readGroupWorkersMapActiveAndRunningAndVersion(engineVersionService.getEngineVersionId())).thenReturn(groupWorkerMap);


		ExecutionMessage message1 = generateMessage("group1", "11");
		ExecutionMessage message2 = generateMessage("group2", "12");
		List<ExecutionMessage> msgs = new ArrayList<>();
		msgs.add(message1);
		msgs.add(message2);
		executionQueueService.enqueue(msgs);


		List<ExecutionMessage> msgInQueue, msgFromQueue;
		msgInQueue = executionQueueService.pollRecovery("worker3", 100, ExecStatus.ASSIGNED);
		Assert.assertEquals(2, msgInQueue.size());
		Assert.assertNotNull(msgInQueue.get(0).getPayload().getData());

		msgInQueue = updateMessages(msgInQueue, ExecStatus.ASSIGNED, "worker1");
		executionQueueService.enqueue(msgInQueue);
		msgFromQueue = executionQueueService.pollRecovery("worker1", 100, ExecStatus.ASSIGNED);
		Assert.assertEquals(2, msgFromQueue.size());
		msgFromQueue = executionQueueService.pollRecovery("worker2", 100, ExecStatus.ASSIGNED);
		Assert.assertEquals(0, msgFromQueue.size());


		msgInQueue = updateMessages(msgInQueue, ExecStatus.SENT, "worker1");
		executionQueueService.enqueue(msgInQueue);
		msgFromQueue = executionQueueService.pollRecovery("worker1", 100, ExecStatus.SENT);
		Assert.assertEquals(ExecStatus.SENT, msgFromQueue.get(0).getStatus());
		Assert.assertEquals(2, msgFromQueue.size());

		msgInQueue = updateMessages(msgInQueue, ExecStatus.IN_PROGRESS, "worker1");
		executionQueueService.enqueue(msgInQueue);
		msgFromQueue = executionQueueService.pollRecovery("worker1", 100, ExecStatus.IN_PROGRESS);
		Assert.assertEquals(ExecStatus.IN_PROGRESS, msgFromQueue.get(0).getStatus());
		Assert.assertEquals(2, msgFromQueue.size());

		msgInQueue = updateMessages(msgInQueue, ExecStatus.FINISHED, "worker1");
		executionQueueService.enqueue(msgInQueue);
		msgFromQueue = executionQueueService.pollRecovery("worker1", 100, ExecStatus.FINISHED);
		Assert.assertEquals(ExecStatus.FINISHED, msgFromQueue.get(0).getStatus());
		Assert.assertEquals(2, msgFromQueue.size());

	}

	@Test
	public void pollWithoutAckTest() throws Exception {
		Multimap<String, String> groupWorkerMap = ArrayListMultimap.create();
		groupWorkerMap.put("group1", "worker1");
		groupWorkerMap.put("group1", "worker2");
		when(workerNodeService.readGroupWorkersMapActiveAndRunningAndVersion(engineVersionService.getEngineVersionId())).thenReturn(groupWorkerMap);

		when(versionService.getCurrentVersion(anyString())).thenReturn(0L);

		List<ExecutionMessage> msgInQueue = executionQueueService.pollMessagesWithoutAck(100, 0);
		Assert.assertEquals(0, msgInQueue.size());

		ExecutionMessage message1 = generateMessage("group1", "5");
		message1.setWorkerId("worker1");
		message1.setStatus(ExecStatus.SENT);

		msgInQueue.clear();
		msgInQueue.add(message1);

		executionQueueService.enqueue(msgInQueue);

		//now we set current system version(100) to be mush higher then msg version (0)
		msgInQueue = executionQueueService.pollMessagesWithoutAck(100, 100);
		Assert.assertEquals(1, msgInQueue.size());

	}

	@Test
	public void pollWithoutAckTestInProgressState() throws Exception {
		Multimap<String, String> groupWorkerMap = ArrayListMultimap.create();
		groupWorkerMap.put("group1", "worker1");
		groupWorkerMap.put("group1", "worker2");
		when(workerNodeService.readGroupWorkersMapActiveAndRunningAndVersion(engineVersionService.getEngineVersionId())).thenReturn(groupWorkerMap);

		when(versionService.getCurrentVersion(anyString())).thenReturn(0L);

		List<ExecutionMessage> msgInQueue = executionQueueService.pollMessagesWithoutAck(100, 0);
		Assert.assertEquals(0, msgInQueue.size());

		ExecutionMessage message1 = generateMessage("group1", "5");
		message1.setWorkerId("worker1");
		message1.setStatus(ExecStatus.IN_PROGRESS);

		msgInQueue.clear();
		msgInQueue.add(message1);

		executionQueueService.enqueue(msgInQueue);

		//now we set current system version(100) to be mush higher then msg version (0)
		msgInQueue = executionQueueService.pollMessagesWithoutAck(100, 100);
		Assert.assertEquals("since we sent a msg in IN_PROGRESS status, pollMessagesWithoutAck should not find it", 0, msgInQueue.size());
	}

	@Test
	public void pollWithoutAckTestMixMsg() throws Exception {
		Multimap<String, String> groupWorkerMap = ArrayListMultimap.create();
		groupWorkerMap.put("group1", "worker1");
		groupWorkerMap.put("group1", "worker2");
		when(workerNodeService.readGroupWorkersMapActiveAndRunningAndVersion("")).thenReturn(groupWorkerMap);
		when(busyWorkersService.isWorkerBusy("worker1")).thenReturn(true);
		when(busyWorkersService.isWorkerBusy("worker1")).thenReturn(true);
		List<ExecutionMessage> msgInQueue = executionQueueService.pollMessagesWithoutAck(100, 0);
		Assert.assertEquals(0, msgInQueue.size());

		ExecutionMessage message1 = generateMessage("group1", "5"); //this msg will get 0 version
		message1.setWorkerId("worker1");
		message1.setStatus(ExecStatus.SENT);

		msgInQueue.clear();
		msgInQueue.add(message1);

		when(versionService.getCurrentVersion(anyString())).thenReturn(0L);

		executionQueueService.enqueue(msgInQueue);

		ExecutionMessage message2 = generateMessage("group1", "5");   //this msg will get 100 version
		message2.setWorkerId("worker2");
		message2.setStatus(ExecStatus.SENT);

		msgInQueue.clear();
		msgInQueue.add(message2);
		when(versionService.getCurrentVersion(anyString())).thenReturn(100L);

		executionQueueService.enqueue(msgInQueue);

		msgInQueue = executionQueueService.pollMessagesWithoutAck(100, 100);
		Assert.assertEquals("only one msg should be with version to far from system version", 1, msgInQueue.size());
		Assert.assertEquals("worker1", msgInQueue.get(0).getWorkerId());
	}


	@Test
	public void readPayloadByIds() {
		Multimap<String, String> groupWorkerMap = ArrayListMultimap.create();
		groupWorkerMap.put("group1", "worker1");
		groupWorkerMap.put("group1", "worker2");
		groupWorkerMap.put("group2", "worker1");
		groupWorkerMap.put("group2", "worker2");
		reset(workerNodeService);
		when(workerNodeService.readGroupWorkersMapActiveAndRunningAndVersion(engineVersionService.getEngineVersionId())).thenReturn(groupWorkerMap);

		ExecutionMessage message1 = generateMessage("group1", "6");
		ExecutionMessage message2 = generateMessage("group2", "6");
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

	private ExecutionMessage generateMessage(String groupName, String msgId) {
		byte[] payloadData;
		payloadData = "This is just a test".getBytes();
		Payload payload = new Payload(payloadData);
		return new ExecutionMessage(-1, ExecutionMessage.EMPTY_WORKER, groupName, msgId, ExecStatus.PENDING, payload, 1);
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
	static class Configurator {
		@Bean
		DataSource dataSource() {
			return new EmbeddedDatabaseBuilder()
					.setType(EmbeddedDatabaseType.H2)
					.build();
		}

		@Bean
		SpringLiquibase liquibase(DataSource dataSource) {
			SpringLiquibase liquibase = new SpringLiquibase();
			liquibase.setDataSource(dataSource);
			liquibase.setChangeLog("classpath:/META-INF/database/test.changes.xml");
			SimpleHiloIdentifierGenerator.setDataSource(dataSource);
			return liquibase;
		}

		@Bean
		PlatformTransactionManager transactionManager(DataSource dataSource){
			return new DataSourceTransactionManager(dataSource);
		}

		@Bean
		JdbcTemplate jdbcTemplate(DataSource dataSource){
			return new JdbcTemplate(dataSource);
		}

		@Bean
		public IdentityGenerator identifierGenerator() {
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
		public WorkerNodeService workerNodeService() {
			return mock(WorkerNodeService.class);
		}

		@Bean
		public BusyWorkersService busyWorkersService() {
			return mock(BusyWorkersService.class);
		}

		@Bean
		public VersionService queueVersionService() {
			return mock(VersionService.class);
		}

		@Bean
		ExecutionQueueRepository executionQueueRepository(){
			return new ExecutionQueueRepositoryImpl();
		}

		@Bean
		ExecutionQueueService executionQueueService(){
			return new ExecutionQueueServiceImpl();
		}

		@Bean
		ExecutionAssignerService executionAssignerService(){
			return new ExecutionAssignerServiceImpl();
		}

		@Bean
		ExecutionMessageConverter executionMessageConverter(){
			return new ExecutionMessageConverter();
		}

		@Bean
		EngineVersionService engineVersionService(){
			EngineVersionService mock =  mock(EngineVersionService.class);

			when(mock.getEngineVersionId()).thenReturn("");

			return mock;
		}

		@Bean
		ExecutionStateService executionStateService() { return mock(ExecutionStateService.class);}
	}
}
