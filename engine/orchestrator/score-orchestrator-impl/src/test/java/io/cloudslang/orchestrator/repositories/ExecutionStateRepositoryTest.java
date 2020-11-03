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

package io.cloudslang.orchestrator.repositories;

import io.cloudslang.engine.data.SimpleHiloIdentifierGenerator;
import io.cloudslang.orchestrator.entities.ExecutionState;
import io.cloudslang.score.facade.execution.ExecutionStatus;
import liquibase.integration.spring.SpringLiquibase;
import org.apache.commons.dbcp.BasicDataSource;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.domain.PageRequest;
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
import java.util.*;

import static org.fest.assertions.Assertions.assertThat;

/**
 * User:
 * Date: 21/05/2014
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class ExecutionStateRepositoryTest{

    @Autowired
    private ExecutionStateRepository executionStateRepository;

    @Before
    public void cleanRepository() {
        executionStateRepository.deleteAll();
    }

    @Test
    public void testFindExecutionIdByStatuses() {
        ExecutionState canceledExecutionState = createExecutionState(ExecutionStatus.CANCELED);
        ExecutionState completedExecutionState = createExecutionState(ExecutionStatus.COMPLETED);
        createExecutionState(ExecutionStatus.PENDING_CANCEL);

        List<Long> executionStates = executionStateRepository.findExecutionIdByStatuses(Arrays.asList(ExecutionStatus.CANCELED, ExecutionStatus.COMPLETED));

        assertThat(executionStates).containsExactly(canceledExecutionState.getExecutionId(), completedExecutionState.getExecutionId());
    }


    @Test
    public void findByStatusInAndUpdateTimeLessThanEqual() {
        ExecutionState canceledExecutionState = createExecutionState(ExecutionStatus.CANCELED);
        ExecutionState completedExecutionState = createExecutionState(ExecutionStatus.COMPLETED);
        createExecutionState(ExecutionStatus.PENDING_CANCEL);

        List<Long> executionIds = executionStateRepository.findByStatusInAndUpdateTimeLessThanEqual(Arrays.asList(ExecutionStatus.CANCELED, ExecutionStatus.COMPLETED), new Date().getTime(), PageRequest.of(0, 100));

        assertThat(executionIds).containsExactly(canceledExecutionState.getExecutionId(), completedExecutionState.getExecutionId());
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
        DataSource dataSource() {
            BasicDataSource ds = new BasicDataSource();
            ds.setDriverClassName("org.h2.Driver");
            ds.setUrl("jdbc:h2:mem:test");
            ds.setUsername("sa");
            ds.setPassword("sa");
            ds.setDefaultAutoCommit(false);
            return ds;
        }

        @Bean(name = "entityManagerFactory")
        @DependsOn({"liquibase", "dataSource"})
        LocalContainerEntityManagerFactoryBean emf(JpaVendorAdapter jpaVendorAdapter) {
            SimpleHiloIdentifierGenerator.setDataSource(dataSource());
            LocalContainerEntityManagerFactoryBean fb = new LocalContainerEntityManagerFactoryBean();
            fb.setJpaProperties(hibernateProperties());
            fb.setDataSource(dataSource());
            fb.setPersistenceProviderClass(HibernatePersistenceProvider.class);
            fb.setPackagesToScan("io.cloudslang");
            fb.setJpaVendorAdapter(jpaVendorAdapter);
            return fb;
        }

        @Bean
        Properties hibernateProperties() {
            return new Properties() {{
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
        SpringLiquibase liquibase() {
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
