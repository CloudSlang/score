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
import io.cloudslang.engine.queue.QueueTestsUtils;
import io.cloudslang.engine.queue.entities.ExecStatus;
import io.cloudslang.engine.queue.entities.ExecutionMessage;
import io.cloudslang.engine.queue.entities.ExecutionMessageConverter;
import io.cloudslang.engine.queue.repositories.ExecutionQueueRepository;
import io.cloudslang.engine.queue.repositories.ExecutionQueueRepositoryImpl;
import io.cloudslang.engine.queue.services.assigner.ExecutionAssignerService;
import io.cloudslang.engine.queue.services.assigner.ExecutionAssignerServiceImpl;
import io.cloudslang.engine.versioning.services.VersionService;
import io.cloudslang.orchestrator.services.CancelExecutionService;
import io.cloudslang.orchestrator.services.EngineVersionService;
import liquibase.integration.spring.SpringLiquibase;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
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
import java.util.List;

import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings({"SpringContextConfigurationInspection"})
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = LargeMessageMonitorServiceTest.Configurator.class)
public class LargeMessageMonitorServiceTest {

    @Autowired
    private CancelExecutionService cancelExecutionService;

    @Autowired
    private LargeMessagesMonitorService largeMessagesMonitorService;

    @Autowired
    private ExecutionQueueRepository executionQueueRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EngineVersionService engineVersionService;

    @Autowired
    private WorkerNodeService workerNodeService;

    @Before
    public void before() {
        jdbcTemplate.execute("delete from OO_EXECUTION_QUEUES");
        jdbcTemplate.execute("delete from OO_EXECUTION_STATES");

        reset(cancelExecutionService);

        System.setProperty("queue.message.time.on.worker", String.valueOf(1));
        System.setProperty("queue.message.expiration.time", String.valueOf(5));
    }

    @After
    public void after() {
        System.setProperty("queue.message.time.on.worker", String.valueOf(LargeMessagesMonitorService.DEFAULT_TIME_ON_WORKER));
        System.setProperty("queue.message.expiration.time", String.valueOf(LargeMessagesMonitorService.DEFAULT_EXPIRATION_TIME));
    }

    @Test
    public void testMonitorMessageReassigned() {

        int mb = 2;
        long workerFreeMem = QueueTestsUtils.getMB(mb - 1);
        String worker = "worker1";
        String workerGroup = "group1";

        Multimap<String, String> groupWorkersMap = ArrayListMultimap.create();
        groupWorkersMap.put(workerGroup, worker);

        Mockito.when(workerNodeService.readGroupWorkersMapActiveAndRunningAndVersion(engineVersionService.getEngineVersionId())).thenReturn(groupWorkersMap);

        ExecutionMessage msg1 = QueueTestsUtils.generateLargeMessage(1, workerGroup,"11", 1, QueueTestsUtils.getMB(mb));
        msg1.setWorkerId(worker);
        msg1.setStatus(ExecStatus.ASSIGNED);

        QueueTestsUtils.insertMessagesInQueue(executionQueueRepository, msg1);

        List<ExecutionMessage> allMsgs = findExecutionMessages(worker, workerFreeMem, ExecStatus.ASSIGNED);
        Assert.assertEquals(1, allMsgs.size());

        waitOverReassignTime();

        largeMessagesMonitorService.monitor();          // clears the worker

        Assert.assertEquals(0, findExecutionMessages(worker, workerFreeMem, ExecStatus.ASSIGNED).size());

        List<ExecutionMessage> unassignedMsg = findExecutionMessages(ExecutionMessage.EMPTY_WORKER, workerFreeMem, ExecStatus.PENDING);
        Assert.assertEquals(1, unassignedMsg.size());

        ExecutionMessage message = unassignedMsg.get(0);
        Assert.assertEquals(1, message.getExecStateId());
        Assert.assertEquals("11", message.getMsgId());
    }

    @Test
    public void testMonitorMessageCanceled() {

        int mb = 2;
        long workerFreeMem = QueueTestsUtils.getMB(mb - 1);
        String worker = "worker1";
        String workerGroup = "group1";

        Multimap<String, String> groupWorkersMap = ArrayListMultimap.create();
        groupWorkersMap.put(workerGroup, worker);

        Mockito.when(workerNodeService.readGroupWorkersMapActiveAndRunningAndVersion(engineVersionService.getEngineVersionId())).thenReturn(groupWorkersMap);

        ExecutionMessage msg1 = QueueTestsUtils.generateLargeMessage(1, workerGroup,"11", 1, QueueTestsUtils.getMB(mb));
        msg1.setWorkerId(worker);
        msg1.setStatus(ExecStatus.ASSIGNED);

        QueueTestsUtils.insertMessagesInQueue(executionQueueRepository, msg1);

        List<ExecutionMessage> allMsgs = findExecutionMessages(worker, workerFreeMem, ExecStatus.ASSIGNED);
        Assert.assertEquals(1, allMsgs.size());

        waitOverExpirationTime();

        largeMessagesMonitorService.monitor();

        long executionId = executionQueueRepository.getMessageRunningExecutionId(msg1);

        verify(cancelExecutionService,times(1)).requestCancelExecution(executionId);
    }

    private void waitOverReassignTime() {
        try {
            Thread.sleep((largeMessagesMonitorService.getMessageTimeOnWorker() + 1) * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void waitOverExpirationTime() {
        try {
            Thread.sleep((largeMessagesMonitorService.getMessageExpirationTime() + 1) * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private List<ExecutionMessage> findExecutionMessages(String worker, long workerFreeMem, ExecStatus... statuses) {
        return executionQueueRepository.poll(worker, 10, 10 * workerFreeMem, statuses);
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
        LargeMessagesMonitorService largeMessagesMonitorService() {
            return new LargeMessagesMonitorServiceImpl();
        }

        @Bean
        CancelExecutionService cancelExecutionService() {

            return mock(CancelExecutionService.class);
        }

        @Bean
        ExecutionQueueRepository executionQueueRepository(){
            return new ExecutionQueueRepositoryImpl();
        }

        @Bean
        ExecutionAssignerService executionAssignerService() {
            return new ExecutionAssignerServiceImpl();
        }

        @Bean
        public WorkerNodeService workerNodeService() {
            return Mockito.mock(WorkerNodeService.class);
        }

        @Bean
        public ExecutionQueueService executionQueueService() {
            return new ExecutionQueueServiceImpl();
        }

        @Bean
        public ExecutionMessageConverter executionMessageConverter() {
            return Mockito.mock(ExecutionMessageConverter.class);
        }

        @Bean
        EngineVersionService engineVersionService(){
            EngineVersionService mock =  mock(EngineVersionService.class);

            when(mock.getEngineVersionId()).thenReturn("");

            return mock;
        }

        @Bean
        public BusyWorkersService busyWorkersService() {
            return mock(BusyWorkersService.class);
        }

        @Bean
        public VersionService queueVersionService() {
            return mock(VersionService.class);
        }
    }
}