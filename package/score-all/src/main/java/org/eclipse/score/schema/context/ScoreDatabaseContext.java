/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package org.eclipse.score.schema.context;

import org.openscore.engine.data.SimpleHiloIdentifierGenerator;
import org.hibernate.ejb.HibernatePersistence;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;

/**
 * User:
 * Date: 01/07/2014
 *
 * This context is used for the case when score does not receive any outside beans related
 * to the database. In which case score itself will create the datasource (H2) and the schema and
 * anything else related to hibernate, bean management and transaction management
 */
public class ScoreDatabaseContext {

    @Bean
    Properties jpaProperties() {
        Properties props = new Properties();
        props.setProperty("hibernate.format_sql", Boolean.TRUE.toString());
        props.setProperty("hibernate.hbm2ddl.auto", System.getProperty("hibernate.hbm2ddl.auto", "validate"));
        props.setProperty("hibernate.cache.use_query_cache", Boolean.FALSE.toString());
        props.setProperty("hibernate.generate_statistics", Boolean.FALSE.toString());
        props.setProperty("hibernate.cache.use_second_level_cache", Boolean.FALSE.toString());
        props.setProperty("hibernate.order_updates", Boolean.TRUE.toString());
        props.setProperty("hibernate.order_inserts", Boolean.TRUE.toString());
        props.setProperty("hibernate.dialect_resolvers", "ScoreDialectResolver");
        return props;
    }

    @Bean
    JpaVendorAdapter jpaVendorAdapter() {
        return new HibernateJpaVendorAdapter();
    }

    @Bean
    @DependsOn("liquibase")
	LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
		//Init the IdentityManager
		SimpleHiloIdentifierGenerator.setDataSource(dataSource);

        //Now create the bean
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
		emf.setDataSource(dataSource);
		emf.setJpaProperties(jpaProperties());
        emf.setJpaVendorAdapter(jpaVendorAdapter());
        emf.setPersistenceProviderClass(HibernatePersistence.class);
        emf.setPackagesToScan("org.eclipse.score");
        return emf;
    }

    @Bean
	JpaTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
		JpaTransactionManager jpaTransactionManager = new JpaTransactionManager();
		jpaTransactionManager.setEntityManagerFactory(entityManagerFactory);
		return jpaTransactionManager;
    }

    @Bean
	JdbcTemplate jdbcTemplate(DataSource dataSource) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate();
		jdbcTemplate.setDataSource(dataSource);
		return jdbcTemplate;
    }

    @Bean
	TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
		TransactionTemplate transactionTemplate = new TransactionTemplate();
		transactionTemplate.setTransactionManager(transactionManager);
		return transactionTemplate;
    }
}
