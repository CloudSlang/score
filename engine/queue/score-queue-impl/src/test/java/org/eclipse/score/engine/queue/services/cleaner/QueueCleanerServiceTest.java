/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package org.eclipse.score.engine.queue.services.cleaner;

import org.eclipse.score.engine.node.services.WorkerNodeService;
import org.eclipse.score.engine.queue.entities.ExecStatus;
import org.eclipse.score.engine.queue.entities.ExecutionMessage;
import org.eclipse.score.engine.queue.entities.ExecutionMessageConverter;
import org.eclipse.score.engine.queue.entities.Payload;
import org.eclipse.score.engine.queue.repositories.ExecutionQueueRepository;
import org.eclipse.score.engine.queue.repositories.ExecutionQueueRepositoryImpl;
import org.eclipse.score.engine.queue.services.ExecutionQueueService;
import org.eclipse.score.engine.queue.services.ExecutionQueueServiceImpl;
import org.eclipse.score.engine.queue.services.assigner.ExecutionAssignerService;
import org.eclipse.score.engine.queue.services.assigner.ExecutionAssignerServiceImpl;
import org.eclipse.score.engine.versioning.services.VersionService;
import org.eclipse.score.engine.partitions.services.PartitionTemplate;
import org.eclipse.score.engine.data.IdentityGenerator;
import org.eclipse.score.engine.data.SimpleHiloIdentifierGenerator;
import junit.framework.Assert;
import liquibase.integration.spring.SpringLiquibase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.reset;

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
	private JdbcTemplate jdbcTemplate;

	@Autowired
	@Qualifier("OO_EXECUTION_STATES")
	private PartitionTemplate statePartitionTemplate;

	@Autowired
	@Qualifier("OO_EXECUTION_QUEUES")
	private PartitionTemplate queuePartitionTemplate;

	@Before
	public void before() {
		jdbcTemplate.execute("delete from OO_EXECUTION_QUEUES_1");
		jdbcTemplate.execute("delete from OO_EXECUTION_STATES_2");
		jdbcTemplate.execute("delete from OO_EXECUTION_STATES_1");
		// init queuePartitionTemplate
		reset(queuePartitionTemplate);
		when(queuePartitionTemplate.activeTable()).thenReturn("OO_EXECUTION_QUEUES_1");
		when(queuePartitionTemplate.previousTable()).thenReturn("OO_EXECUTION_QUEUES_1");
		when(queuePartitionTemplate.reversedTables()).thenReturn((Arrays.asList(
				"OO_EXECUTION_QUEUES_1",
				"OO_EXECUTION_QUEUES_1")));


		// init statePartitionTemplate
		reset(statePartitionTemplate);
		when(statePartitionTemplate.activeTable()).thenReturn("OO_EXECUTION_STATES_2");
		when(statePartitionTemplate.previousTable()).thenReturn("OO_EXECUTION_STATES_1");
		when(statePartitionTemplate.reversedTables()).thenReturn((Arrays.asList(
				"OO_EXECUTION_STATES_2",
				"OO_EXECUTION_STATES_1")));
		// Mockito.reset(workerNodeService);
	}

	@Test
	public void cleanTest() throws Exception {
		List<ExecutionMessage> msgs = new ArrayList<>();
		ExecutionMessage message15 = generateMessage(1, "group1", "1", ExecStatus.IN_PROGRESS);
		ExecutionMessage message16 = generateMessage(1, "group1", "1", ExecStatus.FINISHED);

		ExecutionMessage message25 = generateMessage(2, "group1", "2", ExecStatus.IN_PROGRESS);
		ExecutionMessage message26 = generateMessage(2, "group1", "2", ExecStatus.FINISHED);

		msgs.clear();
		msgs.add(message15);
		executionQueueService.enqueue(msgs);

		Set<Long> ids = queueCleanerService.getFinishedExecStateIds();
		Assert.assertEquals(0, ids.size());

		msgs.clear();
		msgs.add(message16);
		executionQueueService.enqueue(msgs);

		executionQueueService.poll("myWorker", 100, ExecStatus.IN_PROGRESS, ExecStatus.FINISHED);

		ids = queueCleanerService.getFinishedExecStateIds();
		Assert.assertEquals(1, ids.size());

		msgs.clear();
		msgs.add(message26);
		executionQueueService.enqueue(msgs);

		ids = queueCleanerService.getFinishedExecStateIds();
		Assert.assertEquals(2, ids.size());

		msgs.clear();
		msgs.add(message25);
		executionQueueService.enqueue(msgs);

		ids = queueCleanerService.getFinishedExecStateIds();
		Assert.assertEquals(2, ids.size());

		queueCleanerService.cleanFinishedSteps(ids);

		ids = queueCleanerService.getFinishedExecStateIds();
		Assert.assertEquals(0, ids.size());
	}

	private ExecutionMessage generateMessage(long execStateId, String groupName, String msgId, ExecStatus status) {
		byte[] payloadData;
		payloadData = "This is just a test".getBytes();
		Payload payload = new Payload(false, false, payloadData);
		return new ExecutionMessage(execStateId, "myWorker", groupName, msgId, status, payload, 1);
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
		PartitionTemplate OO_EXECUTION_STATES() {
			return mock(PartitionTemplate.class);
		}

		@Bean
		PartitionTemplate OO_EXECUTION_QUEUES() {
			return mock(PartitionTemplate.class);
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
	}
}
