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

package io.cloudslang.schema;

import io.cloudslang.engine.data.SimpleHiloIdentifierGenerator;
import io.cloudslang.engine.node.services.WorkerNodeService;
import io.cloudslang.engine.queue.entities.ExecutionMessage;
import io.cloudslang.engine.queue.services.QueueDispatcherService;
import io.cloudslang.score.api.ExecutionPlan;
import io.cloudslang.score.api.ExecutionStep;
import io.cloudslang.score.api.Score;
import io.cloudslang.score.api.TriggeringProperties;
import io.cloudslang.score.events.EventBus;
import io.cloudslang.score.events.FastEventBus;
import io.cloudslang.worker.execution.reflection.ReflectionAdapter;
import io.cloudslang.worker.execution.services.ExecutionServiceImpl;
import io.cloudslang.worker.execution.services.RobotAvailabilityService;
import io.cloudslang.worker.management.WorkerConfigurationService;
import liquibase.integration.spring.SpringLiquibase;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.ImportResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.List;
import java.util.Properties;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Date: 1/21/14
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = EngineTest.Context.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class EngineTest {

    private static final int WORKER_FREE_MEMORY = 200000000; //bytes

    @Autowired
    private Score score;

    @Autowired
    private WorkerNodeService workerNodeService;

    @Autowired
    private QueueDispatcherService dispatcherService;

    @Test
    public void baseEngineTest() throws InterruptedException {
        // register worker
        workerNodeService.create("uuid", "password", "host", "dir");
        workerNodeService.activate("uuid");
        workerNodeService.up("uuid", "", "");

        ExecutionPlan executionPlan = createExecutionPlan();
        TriggeringProperties triggeringProperties = TriggeringProperties.create(executionPlan);
        score.trigger(triggeringProperties);
        Thread.sleep(300);
        List<ExecutionMessage> messages = dispatcherService.poll("uuid", 10, WORKER_FREE_MEMORY);

        assertThat(messages).hasSize(1);
    }

    private ExecutionPlan createExecutionPlan() {
        return new ExecutionPlan()
                .setFlowUuid("flowUUID")
                .addStep(new ExecutionStep(1L))
                .setBeginStep(1L);
    }

    @Configuration
    @EnableTransactionManagement
    @ImportResource("META-INF/spring/schema/schemaEngineTestContext.xml")
    static class Context {
        @Bean
        DataSource dataSource() {
            return new EmbeddedDatabaseBuilder()
                    .setType(EmbeddedDatabaseType.H2)
                    .build();
        }

        @Bean
        JdbcTemplate jdbcTemplate(DataSource dataSource) {
            return new JdbcTemplate(dataSource);
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
        Properties jpaProperties() {
            return new Properties() {{
                setProperty("hibernate.format_sql", "true");
                setProperty("hibernate.hbm2ddl.auto", "create");
                setProperty("hibernate.cache.use_query_cache", "false");
                setProperty("hibernate.generate_statistics", "false");
                setProperty("hibernate.cache.use_second_level_cache", "false");
                setProperty("hibernate.order_updates", "true");
                setProperty("hibernate.order_inserts", "true");
            }};
        }

        @Bean
        JpaVendorAdapter jpaVendorAdapter() {
            HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
            adapter.setShowSql(false);
            adapter.setGenerateDdl(true);
            return adapter;
        }

        @Bean(name = "entityManagerFactory")
        @DependsOn("liquibase")
        LocalContainerEntityManagerFactoryBean emf(JpaVendorAdapter jpaVendorAdapter, Properties jpaProperties) {
            LocalContainerEntityManagerFactoryBean fb = new LocalContainerEntityManagerFactoryBean();
            fb.setDataSource(dataSource());
            fb.setJpaProperties(jpaProperties);
            fb.setPersistenceProviderClass(HibernatePersistenceProvider.class);
            fb.setPackagesToScan("io.cloudslang");
            fb.setJpaVendorAdapter(jpaVendorAdapter);
            return fb;
        }

        @Bean
        PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
            return new JpaTransactionManager(emf);
        }

        @Bean
        TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
            return new TransactionTemplate(transactionManager);
        }

        @Bean
        EventBus eventBus() {
            return mock(EventBus.class);
        }

        @Bean
        FastEventBus fastEventBus() {
            return mock(FastEventBus.class);
        }

        @Bean
        public ExecutionServiceImpl executionService() {
            return new ExecutionServiceImpl();
        }

        @Bean
        public ReflectionAdapter getReflectionAdapter() {
            return mock(ReflectionAdapter.class);
        }

        @Bean
        public WorkerConfigurationService getWorkerConfigurationService() {
            return mock(WorkerConfigurationService.class);
        }

        @Bean
        public RobotAvailabilityService robotAvailabilityService() {
            return mock(RobotAvailabilityService.class);
        }
    }

}
