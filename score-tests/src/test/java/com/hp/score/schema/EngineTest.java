package com.hp.score.schema;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.oo.broker.services.ParallelPersistenceService;
import com.hp.oo.engine.node.services.WorkerLockService;
import com.hp.oo.engine.node.services.WorkerNodeService;
import com.hp.oo.engine.queue.entities.ExecutionMessage;
import com.hp.oo.engine.queue.services.QueueDispatcherService;
import com.hp.oo.engine.queue.services.recovery.MessageRecoveryService;
import com.hp.oo.orchestrator.services.PauseResumeService;
import com.hp.oo.orchestrator.services.WorkerDbSupportServiceImpl;
import com.hp.score.api.ExecutionPlan;
import com.hp.score.api.ExecutionStep;
import com.hp.score.api.Score;
import com.hp.score.api.TriggeringProperties;
import com.hp.score.engine.data.SimpleHiloIdentifierGenerator;
import com.hp.score.events.EventBus;
import liquibase.integration.spring.SpringLiquibase;
import org.hibernate.ejb.HibernatePersistence;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.FactoryBean;
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
import java.util.Date;
import java.util.List;
import java.util.Properties;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Date: 1/21/14
 *
 * @author Dima Rassin
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = EngineTest.Context.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class EngineTest {

    @Autowired
    private Score score;

    @Autowired
    private WorkerNodeService workerNodeService;

    @Autowired
    private QueueDispatcherService dispatcherService;

    @Test
    public void baseEngineTest() {
        // register worker
        workerNodeService.create("uuid", "password", "host", "dir");
        workerNodeService.activate("uuid");
        workerNodeService.up("uuid");

        ExecutionPlan executionPlan = createExecutionPlan();
        TriggeringProperties triggeringProperties = TriggeringProperties.create(executionPlan);
        score.trigger(triggeringProperties);

        List<ExecutionMessage> messages = dispatcherService.poll("uuid", 10, new Date(0));

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
        FactoryBean<EntityManagerFactory> emf(JpaVendorAdapter jpaVendorAdapter, Properties jpaProperties) {
            LocalContainerEntityManagerFactoryBean fb = new LocalContainerEntityManagerFactoryBean();
            fb.setDataSource(dataSource());
            fb.setJpaProperties(jpaProperties);
            fb.setPersistenceProviderClass(HibernatePersistence.class);
            fb.setPackagesToScan("com.hp.oo", "com.hp.score");
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
            // required by executionInterruptsService
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }

        @Bean
        PauseResumeService pauseResumeService() {
            return mock(PauseResumeService.class);
        }

        @Bean
        WorkerDbSupportServiceImpl workerDbSupportService() {
            return  new WorkerDbSupportServiceImpl();
        }

        @Bean
        ParallelPersistenceService parallelPersistenceService() {
            return  mock(ParallelPersistenceService.class);
        }

        @Bean
        WorkerLockService workerLockService() {
            return mock(WorkerLockService.class);
        }

        @Bean
        MessageRecoveryService messageRecoveryService(){
            return mock(MessageRecoveryService.class);
        }

		@Bean
		EventBus eventBus() {
			return mock(EventBus.class);
		}
	}
}
