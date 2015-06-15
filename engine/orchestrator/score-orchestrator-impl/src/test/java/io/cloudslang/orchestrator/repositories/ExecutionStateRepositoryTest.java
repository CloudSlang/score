/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package io.cloudslang.orchestrator.repositories;

import io.cloudslang.score.facade.execution.ExecutionStatus;
import io.cloudslang.engine.data.LocalMemIncrementGenerator;
import io.cloudslang.orchestrator.entities.ExecutionState;
import liquibase.integration.spring.SpringLiquibase;
import org.apache.commons.dbcp.BasicDataSource;
import org.hibernate.ejb.HibernatePersistence;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.fest.assertions.Assertions.assertThat;

/**
 * User:
 * Date: 21/05/2014
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ExecutionStateRepositoryTest.ExecutionStateRepositoryTestContext.class)
public class ExecutionStateRepositoryTest {

    @Autowired
    private ExecutionStateRepository executionStateRepository;

    @Test
    public void testFindExecutionIdByStatuses() {
        ExecutionState canceledExecutionState = createExecutionState(ExecutionStatus.CANCELED);
        ExecutionState completedExecutionState = createExecutionState(ExecutionStatus.COMPLETED);
        createExecutionState(ExecutionStatus.PENDING_CANCEL);

        List<Long> executionStates = executionStateRepository.findExecutionIdByStatuses(Arrays.asList(ExecutionStatus.CANCELED, ExecutionStatus.COMPLETED));

        assertThat(executionStates).containsExactly(canceledExecutionState.getExecutionId(), completedExecutionState.getExecutionId());
    }

    private ExecutionState createExecutionState(ExecutionStatus status) {
        ExecutionState executionState = new ExecutionState();
        executionState.setStatus(status);
        executionState.setExecutionId(123L);
        executionState.setBranchId(UUID.randomUUID().toString());
        executionStateRepository.saveAndFlush(executionState);
        return executionState;
    }

    @Configuration
    @EnableJpaRepositories("io.cloudslang")
    static class ExecutionStateRepositoryTestContext {

        @Bean
        DataSource dataSource(){
            BasicDataSource ds = new BasicDataSource();
            ds.setDriverClassName("org.h2.Driver");
            ds.setUrl("jdbc:h2:mem:test");
            ds.setUsername("sa");
            ds.setPassword("sa");
            ds.setDefaultAutoCommit(false);
            return ds;
        }

        @Bean(name="entityManagerFactory")
        @DependsOn({"liquibase", "dataSource"})
        FactoryBean<EntityManagerFactory> emf(JpaVendorAdapter jpaVendorAdapter) {
            LocalContainerEntityManagerFactoryBean fb = new LocalContainerEntityManagerFactoryBean();
            fb.setJpaProperties(hibernateProperties());
            fb.setDataSource(dataSource());
            fb.setPersistenceProviderClass(HibernatePersistence.class);
            fb.setPackagesToScan("io.cloudslang");
            fb.setJpaVendorAdapter(jpaVendorAdapter);
            return fb;
        }

        @Bean
        Properties hibernateProperties() {
            return new Properties(){{
                setProperty("hibernate.format_sql", "true");
                setProperty("hibernate.hbm2ddl.auto", "create-drop");
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
            adapter.setShowSql(true);
            adapter.setGenerateDdl(true);
            return adapter;
        }

        @Bean
        SpringLiquibase liquibase(){
            SpringLiquibase liquibase = new SpringLiquibase();
            liquibase.setDataSource(dataSource());
            liquibase.setChangeLog("classpath:/META-INF/database/test-changes.xml");
            return liquibase;
        }

        @Bean
        PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
            return new JpaTransactionManager(emf);
        }

    }
}
