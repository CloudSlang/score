package com.hp.score.schema.context;

import com.hp.score.engine.data.SimpleHiloIdentifierGenerator;
import org.hibernate.ejb.HibernatePersistence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * User: maromg
 * Date: 01/07/2014
 *
 * This context is used for the case when score does not receive any outside beans related
 * to the database. In which case score itself will create the datasource (H2) and the schema and
 * anything else related to hibernate, bean management and transaction management
 */
public class ScoreDatabaseContext {

    @Autowired
    private ApplicationContext applicationContext;

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
        return props;
    }

    @Bean
    JpaVendorAdapter jpaVendorAdapter() {
        return new HibernateJpaVendorAdapter();
    }

    @Bean
    @DependsOn("liquibase")
    LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        //Init the IdentityManager
        SimpleHiloIdentifierGenerator.setDataSource(applicationContext.getBean(DataSource.class));

        //Now create the bean
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(applicationContext.getBean(DataSource.class));
        emf.setJpaProperties(jpaProperties());
        emf.setJpaVendorAdapter(jpaVendorAdapter());
        emf.setPersistenceProviderClass(HibernatePersistence.class);
        //todo: remove scanning of oo package once we move all the entities to score package
        emf.setPackagesToScan("com.hp.score","com.hp.oo");
        return emf;
    }

    @Bean
    JpaTransactionManager transactionManager() {
        JpaTransactionManager jpaTransactionManager = new JpaTransactionManager();
        jpaTransactionManager.setEntityManagerFactory(entityManagerFactory().getNativeEntityManagerFactory());
        return jpaTransactionManager;
    }

    @Bean
    JdbcTemplate jdbcTemplate() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate();
        jdbcTemplate.setDataSource(applicationContext.getBean(DataSource.class));
        return jdbcTemplate;
    }

    @Bean
    TransactionTemplate transactionTemplate() {
        TransactionTemplate transactionTemplate = new TransactionTemplate();
        transactionTemplate.setTransactionManager(transactionManager());
        return transactionTemplate;
    }
}
