package com.hp.score.samples;

import com.hp.score.api.ControlActionMetadata;
import com.hp.score.api.ExecutionPlan;
import com.hp.score.api.ExecutionStep;
import com.hp.score.api.Score;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.Serializable;
import java.util.HashMap;

/**
 * User: stoneo
 * Date: 22/07/2014
 * Time: 14:42
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/META-INF/spring/samples/schemaAllTestContext.xml")
@Ignore
public class StandAloneTest {

    @Autowired
    private Score score;

    @Test
    public void baseStandAloneTest() {
        ExecutionPlan executionPlan = createExecutionPlan();
        score.trigger(executionPlan);
    }

    private static ExecutionPlan createExecutionPlan() {
        ExecutionPlan executionPlan = new ExecutionPlan();

        executionPlan.setFlowUuid("1");

        executionPlan.setBeginStep(0L);

        ExecutionStep executionStep = new ExecutionStep(0L);
        executionStep.setAction(new ControlActionMetadata("com.hp.score.samples.controlactions.ConsoleControlActions", "echoHelloScore"));
        executionStep.setActionData(new HashMap<String, Serializable>());
        executionStep.setNavigation(new ControlActionMetadata("com.hp.score.samples.controlactions.NavigationActions", "nextStepNavigation"));
        executionStep.setNavigationData(new HashMap<String, Serializable>());

        executionPlan.addStep(executionStep);

        ExecutionStep executionStep2 = new ExecutionStep(1L);
        executionStep2.setAction(new ControlActionMetadata("com.hp.score.samples.controlactions.ConsoleControlActions", "echoHelloScore"));
        executionStep2.setActionData(new HashMap<String, Serializable>());

        executionPlan.addStep(executionStep2);

        return executionPlan;
    }

//    @Configuration
//    @EnableTransactionManagement
//    @ImportResource("META-INF/spring/schema/schemaAllTestContext.xml")
//    static class Context {
//        @Bean
//        DataSource dataSource() {
//            return new EmbeddedDatabaseBuilder()
//                    .setType(EmbeddedDatabaseType.H2)
//                    .build();
//        }
//
//        @Bean
//        JdbcTemplate jdbcTemplate(DataSource dataSource) {
//            return new JdbcTemplate(dataSource);
//        }
//
//        @Bean
//        SpringLiquibase liquibase(DataSource dataSource) {
//            SpringLiquibase liquibase = new SpringLiquibase();
//            liquibase.setDataSource(dataSource);
//            liquibase.setChangeLog("classpath:/META-INF/database/test.changes.xml");
//            SimpleHiloIdentifierGenerator.setDataSource(dataSource);
//            return liquibase;
//        }
//
//        @Bean
//        Properties jpaProperties() {
//            return new Properties() {{
//                setProperty("hibernate.format_sql", "true");
//                setProperty("hibernate.hbm2ddl.auto", "create");
//                setProperty("hibernate.cache.use_query_cache", "false");
//                setProperty("hibernate.generate_statistics", "false");
//                setProperty("hibernate.cache.use_second_level_cache", "false");
//                setProperty("hibernate.order_updates", "true");
//                setProperty("hibernate.order_inserts", "true");
//            }};
//        }
//
//        @Bean
//        JpaVendorAdapter jpaVendorAdapter() {
//            HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
//            adapter.setShowSql(false);
//            adapter.setGenerateDdl(true);
//            return adapter;
//        }
//
//        @Bean(name = "entityManagerFactory")
//        @DependsOn("liquibase")
//        FactoryBean<EntityManagerFactory> emf(JpaVendorAdapter jpaVendorAdapter, Properties jpaProperties) {
//            LocalContainerEntityManagerFactoryBean fb = new LocalContainerEntityManagerFactoryBean();
//            fb.setDataSource(dataSource());
//            fb.setJpaProperties(jpaProperties);
//            fb.setPersistenceProviderClass(HibernatePersistence.class);
//            fb.setPackagesToScan("com.hp.oo", "com.hp.score");
//            fb.setJpaVendorAdapter(jpaVendorAdapter);
//            return fb;
//        }
//
//        @Bean
//        PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
//            return new JpaTransactionManager(emf);
//        }
//
//        @Bean
//        TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
//            return new TransactionTemplate(transactionManager);
//        }
//
//        @Bean
//            // required by executionInterruptsService
//        ObjectMapper objectMapper() {
//            return new ObjectMapper();
//        }
//
//        @Bean
//        PauseResumeService pauseResumeService() {
//            return mock(PauseResumeService.class);
//        }
//
//        @Bean
//        WorkerDbSupportServiceImpl workerDbSupportService() {
//            return  new WorkerDbSupportServiceImpl();
//        }
//
//        @Bean
//        ParallelPersistenceService parallelPersistenceService() {
//            return  mock(ParallelPersistenceService.class);
//        }
//
//        @Bean
//        WorkerConfigurationService workerConfigurationService() {
//            return new StubWorkerConfigurationService();
//        }
//
//        @Bean
//        RuntimeValueService runtimeValueService(){
//            return new StubRuntimeValueService();
//        }
//    }
}
