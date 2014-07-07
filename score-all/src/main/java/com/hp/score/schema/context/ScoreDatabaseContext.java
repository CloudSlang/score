package com.hp.score.schema.context;

import com.hp.score.engine.data.HiloFactoryBean;
import com.hp.score.engine.data.IdentityGenerator;
import com.hp.score.engine.data.SimpleHiloIdentifierGenerator;
import org.hibernate.ejb.HibernatePersistence;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * User: maromg
 * Date: 01/07/2014
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
        HibernateJpaVendorAdapter jpaVendorAdapter = new HibernateJpaVendorAdapter();
        jpaVendorAdapter.setGenerateDdl(true);
        jpaVendorAdapter.setShowSql(true);
        return jpaVendorAdapter;
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
        emf.setPackagesToScan("com.hp.score");
        return emf;
    }

    @Bean
    JpaTransactionManager jpaTransactionManager() {
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
    FactoryBean<IdentityGenerator> identityGenerator() {
        return new HiloFactoryBean();
    }
}
