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

package io.cloudslang.engine.queue.services.cleaner;

import io.cloudslang.engine.data.IdentityGenerator;
import io.cloudslang.engine.data.SimpleHiloIdentifierGenerator;
import io.cloudslang.engine.node.services.WorkerNodeService;
import io.cloudslang.engine.queue.entities.ExecStatus;
import io.cloudslang.engine.queue.entities.ExecutionMessage;
import io.cloudslang.engine.queue.entities.ExecutionMessageConverter;
import io.cloudslang.engine.queue.entities.Payload;
import io.cloudslang.engine.queue.repositories.ExecutionQueueRepository;
import io.cloudslang.engine.queue.repositories.ExecutionQueueRepositoryImpl;
import io.cloudslang.engine.queue.services.BusyWorkersService;
import io.cloudslang.engine.queue.services.ExecutionQueueService;
import io.cloudslang.engine.queue.services.ExecutionQueueServiceImpl;
import io.cloudslang.engine.queue.services.assigner.ExecutionAssignerService;
import io.cloudslang.engine.queue.services.assigner.ExecutionAssignerServiceImpl;
import io.cloudslang.engine.versioning.services.VersionService;
import io.cloudslang.orchestrator.services.EngineVersionService;
import io.cloudslang.orchestrator.services.ExecutionStateService;
import junit.framework.Assert;
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
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * User: A
 * Date: 04/12/13
 * Time: 11:13
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class QueueCleanerServiceTest {

	@Autowired
	public ExecutionQueueService executionQueueService;

	@Autowired
	public QueueCleanerService queueCleanerService;

	@Autowired
	private BusyWorkersService busyWorkersService;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private ExecutionStateService executionStateService;

	@Autowired
	private ExecutionQueueRepository executionQueueRepository;


	@Before
	public void before() {
		jdbcTemplate.execute("delete from OO_EXECUTION_QUEUES");
		jdbcTemplate.execute("delete from OO_EXECUTION_STATES");
	}

	@Test
	public void cleanTest() throws Exception {
		List<ExecutionMessage> msgs = new ArrayList<>();
		ExecutionMessage message15 = generateMessage(1, "group1", "1", ExecStatus.IN_PROGRESS, 1);
		ExecutionMessage message16 = generateMessage(1, "group1", "1", ExecStatus.FINISHED, 2);

		ExecutionMessage message25 = generateMessage(2, "group1", "2", ExecStatus.IN_PROGRESS, 1);
		ExecutionMessage message26 = generateMessage(2, "group1", "2", ExecStatus.FINISHED, 2);
		when(busyWorkersService.isWorkerBusy("myWorker")).thenReturn(true);
		msgs.clear();
		msgs.add(message15);
		executionQueueService.enqueue(msgs);

		Set<Long> ids = queueCleanerService.getNonLatestFinishedExecStateIds();
		Assert.assertEquals(0, ids.size()); // TODO fix tests

		msgs.clear();
		msgs.add(message16);
		executionQueueService.enqueue(msgs);

		executionQueueService.pollRecovery("myWorker", 100, ExecStatus.IN_PROGRESS, ExecStatus.FINISHED);

		ids = queueCleanerService.getNonLatestFinishedExecStateIds();
		Assert.assertEquals(0, ids.size());

		msgs.clear();
		msgs.add(message26);
		executionQueueService.enqueue(msgs);

		ids = queueCleanerService.getNonLatestFinishedExecStateIds();
		Assert.assertEquals(0, ids.size());

		msgs.clear();
		msgs.add(message25);
		executionQueueService.enqueue(msgs);

		ids = queueCleanerService.getNonLatestFinishedExecStateIds();
		Assert.assertEquals(0, ids.size());

		queueCleanerService.cleanFinishedSteps(ids);

		ids = queueCleanerService.getNonLatestFinishedExecStateIds();
		Assert.assertEquals(0, ids.size());
	}

	private ExecutionMessage generateMessage(long execStateId, String groupName, String msgId, ExecStatus status, int msg_seq_id) {
		byte[] payloadData;
		payloadData = "This is just a test".getBytes();
		Payload payload = new Payload(payloadData);
		return new ExecutionMessage(execStateId, "myWorker", groupName, msgId, status, payload, msg_seq_id);
	}


	@Configuration
	@EnableTransactionManagement
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
		IdentityGenerator identifierGenerator() {
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
		QueueCleanerService queueCleanerService() {
			return new QueueCleanerServiceImpl();
		}

		@Bean
		WorkerNodeService workerNodeService() {
			return mock(WorkerNodeService.class);
		}

		@Bean
		public BusyWorkersService busyWorkersService() {
			return mock(BusyWorkersService.class);
		}

		@Bean
		VersionService queueVersionService() {
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
			return mock(EngineVersionService.class);
		}

		@Bean
		ExecutionStateService executionStateService() { return mock(ExecutionStateService.class); }
	}
}
