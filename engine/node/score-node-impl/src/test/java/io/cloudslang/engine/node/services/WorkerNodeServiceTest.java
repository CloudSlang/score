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


package io.cloudslang.engine.node.services;

import io.cloudslang.engine.data.SimpleHiloIdentifierGenerator;
import io.cloudslang.engine.node.entities.WorkerNode;
import io.cloudslang.engine.node.repositories.WorkerNodeRepository;
import io.cloudslang.engine.versioning.services.VersionService;
import io.cloudslang.score.api.nodes.WorkerStatus;
import liquibase.integration.spring.SpringLiquibase;
import org.apache.commons.dbcp.BasicDataSource;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;


import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by IntelliJ IDEA.
 * User:
 * Date: 15/11/12
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = WorkerNodeServiceTest.Configurator.class)
@Transactional
@Rollback
// this test depend on repo - should mock it!
public class WorkerNodeServiceTest {
    private static final boolean SHOW_SQL = false;
    private static final String versionId = "123";

    @Autowired
    private WorkerNodeService workerNodeService;

    @Autowired
    private WorkerLockService workerLockService;

    @Autowired
    private WorkerNodeRepository workerNodeRepository;

    @Autowired
    private VersionService versionService;

    @Before
    public void initNodes() {
        workerNodeService.create("H1", "H1", "amit.levin", "c:/dir", "AliasH1");

        workerNodeService.create("H2", "H2", "dima.rassin", "c:/dir", "AliasH2");
    }

    @After
    public void reset() {
        Mockito.reset(versionService, workerLockService);
    }

    @Test
    public void keepAlive() throws Exception {
        when(versionService.getCurrentVersion(anyString())).thenReturn(5L);

        WorkerNode worker = workerNodeService.readByUUID("H1");
        Date origDate = worker.getAckTime();
        Assert.assertNull(origDate);
        workerNodeService.keepAlive("H1");
        workerNodeRepository.flush();
        worker = workerNodeService.readByUUID("H1");
        Assert.assertNotNull(worker.getAckTime());
        Assert.assertEquals(5, worker.getAckVersion());

    }

    @Test
    public void createNode() throws Exception {
        workerNodeService.create("H3", "H3", "amit.levin", "c:/dir", "AliasH3");
        verify(workerLockService).create("H3");
        WorkerNode worker = workerNodeService.readByUUID("H3");
        Assert.assertNotNull(worker);
    }

    @Test
    public void login() throws Exception {
        workerNodeService.create("H3", "H3", "dima.rassin", "c:/dir", "AliasH3");
        WorkerNode worker = workerNodeService.readByUUID("H3");
        Assert.assertEquals(WorkerStatus.FAILED, worker.getStatus());
        workerNodeService.up("H3", "version", versionId, false);
        worker = workerNodeService.readByUUID("H3");
        Assert.assertEquals(WorkerStatus.RUNNING, worker.getStatus());
    }

    @Test
    public void readByUUID() throws Exception {
        WorkerNode worker = workerNodeService.readByUUID("H1");
        Assert.assertNotNull(worker);
    }

    @Test
    public void readAllWorkers() throws Exception {
        List<WorkerNode> workers = workerNodeService.readAllWorkers();
        Assert.assertEquals(2, workers.size());
    }

    @Test
    public void readAllNotDeletedWorkers() {
        workerNodeService.create("H3", "H3", "dima.rassin", "c:/dir", "AliasH3");
        List<WorkerNode> workers = workerNodeService.readAllNotDeletedWorkers();
        Assert.assertEquals(3, workers.size());
        workerNodeService.updateWorkerToDeleted("H3");
        workers = workerNodeService.readAllNotDeletedWorkers();
        Assert.assertEquals(2, workers.size());
    }

    @Test
    public void deleteRunningWorkerTest() {
        workerNodeService.create("H3", "H3", "dima.rassin", "c:/dir", "AliasH3");
        WorkerNode worker = workerNodeService.readByUUID("H3");
        Assert.assertEquals(WorkerStatus.FAILED, worker.getStatus());
        workerNodeService.up("H3", "version", versionId, false);
        Assert.assertEquals(WorkerStatus.RUNNING, worker.getStatus());
        workerNodeService.updateWorkerToDeleted("H3");
        Assert.assertEquals(WorkerStatus.IN_RECOVERY, worker.getStatus());
        Assert.assertEquals(false, worker.isActive());
        Assert.assertEquals(true, worker.isDeleted());
    }

    @Test
    public void restoreDeletedWorker() {
        workerNodeService.create("H3", "H3", "tirla.alin", "m:/y/imaginary/path", "AliasH3");
        WorkerNode worker = workerNodeService.readByUUID("H3");
        worker.setActive(false);
        worker.setDeleted(true);
        worker.setStatus(WorkerStatus.IN_RECOVERY);

        workerNodeService.updateWorkerToNotDeleted("H3");

        Assert.assertEquals(WorkerStatus.IN_RECOVERY, worker.getStatus());
        Assert.assertEquals(false, worker.isActive());
        Assert.assertEquals(false, worker.isDeleted());
    }

    @Test
    public void readNonRespondingWorkers() throws Exception {
        List<String> workers = workerNodeService.readNonRespondingWorkers();
        Assert.assertEquals(0, workers.size());

        workerNodeService.create("H3", "H3", "dima.rassin", "c:/dir", "AliasH3");
        workers = workerNodeService.readNonRespondingWorkers();
        Assert.assertEquals(0, workers.size());

        // only activate workers can be non responding
        workerNodeService.activate("H3");
        workers = workerNodeService.readNonRespondingWorkers();
        Assert.assertEquals(0, workers.size());//still it not "non responding" because it yet to login(first login)

        // after login version is current system version.
        workerNodeService.up("H3", "version", versionId, false);
        workers = workerNodeService.readNonRespondingWorkers();
        Assert.assertEquals(0, workers.size());

        //when the worker version is too far from the system version its NonResponding
        when(versionService.getCurrentVersion(anyString())).thenReturn(100L);
        workers = workerNodeService.readNonRespondingWorkers();
        Assert.assertEquals(3, workers.size());

        //after up the worker version will be aligned with current system version.
        workerNodeService.up("H3", "version", versionId, false);
        workers = workerNodeService.readNonRespondingWorkers();
        Assert.assertEquals(2, workers.size());
        Assert.assertFalse(workers.contains("H3"));

    }

    @Test
    public void readWorkersByActivation() throws Exception {

        workerNodeService.create("H3", "H3", "dima.rassin", "c:/dir", "AliasH3");
        List<WorkerNode> workers = workerNodeService.readWorkersByActivation(true);
        Assert.assertEquals(0, workers.size());
        workers = workerNodeService.readWorkersByActivation(false);
        Assert.assertEquals(3, workers.size());

        // activate worker
        workerNodeService.activate("H3");
        workers = workerNodeService.readWorkersByActivation(true);
        Assert.assertEquals(1, workers.size());
        workers = workerNodeService.readWorkersByActivation(false);
        Assert.assertEquals(2, workers.size());

        // deactivate worker
        workerNodeService.deactivate("H3");
        workers = workerNodeService.readWorkersByActivation(true);
        Assert.assertEquals(0, workers.size());
        workers = workerNodeService.readWorkersByActivation(false);
        Assert.assertEquals(3, workers.size());

    }

    @Test
    public void updateEnvironmentParams() throws Exception {
        workerNodeService.create("H3", "H3", "dima.rassin", "c:/dir", "AliasH3");
        workerNodeService.updateEnvironmentParams("H3", "Window", "7.0", "4");
        WorkerNode worker = workerNodeService.readByUUID("H3");
        Assert.assertEquals("Window", worker.getOs());
        Assert.assertEquals("7.0", worker.getJvm());
        Assert.assertEquals("4", worker.getDotNetVersion());

    }

    @Test
    public void updateStatus() {
        workerNodeService.create("H3", "H3", "dima.rassin", "c:/dir", "AliasH3");
        WorkerNode worker = workerNodeService.readByUUID("H3");
        Assert.assertEquals(WorkerStatus.FAILED, worker.getStatus());

        workerNodeService.updateStatus("H3", WorkerStatus.RUNNING);
        worker = workerNodeService.readByUUID("H3");
        Assert.assertEquals(WorkerStatus.RUNNING, worker.getStatus());
    }

    @Test
    public void updateBulkNumber() {
        workerNodeService.create("H3", "H3", "dima.rassin", "c:/dir", "AliasH3");

        workerNodeService.updateBulkNumber("H3", versionId);

        WorkerNode worker = workerNodeService.readByUUID("H3");
        Assert.assertEquals(versionId, worker.getBulkNumber());
    }

    @Test
    public void readAllWorkerGroups() {
        List<String> groups = workerNodeService.readAllWorkerGroups();
        Assert.assertEquals(WorkerNode.DEFAULT_WORKER_GROUPS.length, groups.size());

        workerNodeService.create("H3", "H3", "dima.rassin", "c:/dir", "AliasH3");
        workerNodeService.updateWorkerGroups("H3", "group 1", "group 2");
        workerNodeService.updateWorkerGroups("H1", "group 1");
        groups = workerNodeService.readAllWorkerGroups();
        Assert.assertEquals(WorkerNode.DEFAULT_WORKER_GROUPS.length + 2, groups.size());

        workerNodeService.updateWorkerGroups("H3");
        WorkerNode workerNode = workerNodeService.readByUUID("H3");
        Assert.assertTrue(workerNode.getGroups().isEmpty());
    }

    @Test
    public void checkIfGroupsExist() {
        workerNodeService.updateWorkerGroups("H1", "group 1", "group 2");
        workerNodeService.updateWorkerGroups("H2", "group 1");

        List<String> result = workerNodeService.readWorkerGroups(Arrays.asList("group 1"));
        Assert.assertEquals(Arrays.asList("group 1"), result);
    }

    @Test
    public void addWorkerGroup() {
        WorkerNode workerNode = workerNodeService.readByUUID("H1");
        int groupSize = workerNode.getGroups().size();

        workerNodeService.addGroupToWorker("H1", "aaa");
        workerNode = workerNodeService.readByUUID("H1");

        Assert.assertEquals(groupSize + 1, workerNode.getGroups().size());
    }

    @Test
    public void addWorkerInDuplicateGroups() {
        List<String> groups = workerNodeService.readAllWorkerGroups();
        Assert.assertEquals(WorkerNode.DEFAULT_WORKER_GROUPS.length, groups.size());

        List<String> list;
        HashSet<String> expected;

        workerNodeService.create("PLM", "PLM", "dan.filip", "c:/plm", "AliasP");

        workerNodeService.updateWorkerGroups("PLM", "c1", "c2", "c2", "c3");
        list = workerNodeService.readWorkerGroups("PLM");

        expected = new HashSet<>(Arrays.asList("c2", "c1", "c3"));
        org.junit.Assert.assertTrue("worker groups contain duplicates?", new HashSet<>(list).equals(expected));

        // test null is not allowed + duplicates
        workerNodeService.updateWorkerGroups("PLM", null, "1", null, "1", "1");
        list = workerNodeService.readWorkerGroups("PLM");

        expected = new HashSet<>(Arrays.asList("1"));
        org.junit.Assert.assertTrue("worker groups contain duplicates?", new HashSet<>(list).equals(expected));

        // test add worker in the same group twice
        workerNodeService.addGroupToWorker("PLM", "g1");
        workerNodeService.addGroupToWorker("PLM", "g1");

        list = workerNodeService.readWorkerGroups("PLM");
        expected = new HashSet<>(Arrays.asList("1", "g1"));
        org.junit.Assert.assertTrue("worker groups contain duplicates?", new HashSet<>(list).equals(expected));
    }

    @Test
    public void updateVersionTest() {
        workerNodeService.create("worker_1", "password", "stamHost", "c:/dir", "AliasW1");
        WorkerNode workerNode = workerNodeService.readByUUID("H1");
        Assert.assertEquals("", workerNode.getVersion());

        workerNodeService.updateVersion("H1", "VERSION", versionId);

        workerNode = workerNodeService.readByUUID("H1");

        Assert.assertEquals("Version not updated!", "VERSION", workerNode.getVersion());
    }

    @Test
    public void updateMigratedPasswordTest() {
        workerNodeService.create("worker_1", "password", "stamHost", "c:/dir", "AliasW1");
        WorkerNode workerNode = workerNodeService.readByUUID("H1");
        Assert.assertNull(workerNode.getMigratedPassword());

        workerNodeService.updateMigratedPassword("H1", "newPassword");

        workerNode = workerNodeService.readByUUID("H1");

        Assert.assertEquals("Version not updated!", "newPassword", workerNode.getMigratedPassword());
    }

    @Configuration
    @EnableJpaRepositories("io.cloudslang.engine.node.repositories")
    @EnableTransactionManagement
    static class Configurator {
        @Bean
        DataSource dataSource() {
            BasicDataSource ds = new BasicDataSource();
            ds.setDriverClassName("org.h2.Driver");
            ds.setUrl("jdbc:h2:mem:test");
            ds.setUsername("sa");
            ds.setPassword("sa");
            ds.setDefaultAutoCommit(false);
            return new TransactionAwareDataSourceProxy(ds);
        }

        @Bean
        SpringLiquibase liquibase(DataSource dataSource) {
            SpringLiquibase liquibase = new SpringLiquibase();
            liquibase.setDataSource(dataSource);
            //liquibase.setChangeLog("classpath:/META-INF/database/test.changes.xml");
            liquibase.setChangeLog("classpath:/META-INF/database/score.changes.xml");
            SimpleHiloIdentifierGenerator.setDataSource(dataSource);
            return liquibase;
        }


        @Bean
        Properties hibernateProperties() {
            return new Properties() {{
                setProperty("hibernate.format_sql", "true");
                setProperty("hibernate.hbm2ddl.auto", "validate");
                setProperty("hibernate.cache.use_query_cache", "false");
                setProperty("hibernate.generate_statistics", "false");
                setProperty("hibernate.cache.use_second_level_cache", "false");
                setProperty("hibernate.order_updates", "true");
                setProperty("hibernate.order_inserts", "true");
                setProperty("hibernate.dialect_resolvers", "io.cloudslang.engine.dialects.ScoreDialectResolver");
            }};
        }

        @Bean
        JpaVendorAdapter jpaVendorAdapter() {
            HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
            adapter.setShowSql(SHOW_SQL);
            adapter.setGenerateDdl(true);
            return adapter;
        }

        @Bean(name = "entityManagerFactory")
        @DependsOn("liquibase")
        LocalContainerEntityManagerFactoryBean emf(JpaVendorAdapter jpaVendorAdapter) {
            LocalContainerEntityManagerFactoryBean fb = new LocalContainerEntityManagerFactoryBean();
            fb.setJpaProperties(hibernateProperties());
            fb.setDataSource(dataSource());
            fb.setPersistenceProviderClass(HibernatePersistenceProvider.class);
            fb.setPackagesToScan("io.cloudslang.engine.node");
            fb.setJpaVendorAdapter(jpaVendorAdapter);
            return fb;
        }

        @Bean
        PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
            return new JpaTransactionManager(emf);
        }

        @Bean
        VersionService versionService() {
            VersionService versionService = mock(VersionService.class);
            when(versionService.getCurrentVersion(anyString())).thenReturn(1L);
            return versionService;
        }

        @Bean
        WorkerNodeService workerNodeService() {
            return new WorkerNodeServiceImpl();
        }

        @Bean
        WorkerLockService workerLockService() {
            return mock(WorkerLockService.class);
        }

        @Bean
        QueueConfigurationDataService queueConfigurationDataService() {
            return mock(QueueConfigurationDataService.class);
        }

    }
}
