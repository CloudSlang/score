package com.hp.oo.engine.versioning.repositories;

import com.hp.oo.engine.versioning.entities.VersionCounter;
import com.hp.score.engine.data.SimpleHiloIdentifierGenerator;
import junit.framework.Assert;
import liquibase.integration.spring.SpringLiquibase;
import org.apache.commons.dbcp.BasicDataSource;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.FactoryBean;
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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;
import java.util.Properties;

/**
 * User: wahnonm
 * Date: 31/10/13
 * Time: 16:43
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@Transactional
@TransactionConfiguration(defaultRollback = true)
public class VersionRepositoryTest {

	private static final boolean SHOW_SQL = false;

	@Autowired
	private VersionRepository repository;

	@PersistenceContext
	private EntityManager em;

	@Test
	public void testRecoveryVersionCounter() {
		String counterName = "my counter";
		repository.save(new VersionCounter(counterName));
		repository.flush();

		//MSG_RECOVERY_VERSION is created in liquibase files, making sure it there...
		VersionCounter result = repository.findByCounterName(counterName);
		Assert.assertEquals(0L, result.getVersionCount());

		int resultRowCount = repository.incrementCounterByName(counterName);
		Assert.assertEquals(1, resultRowCount);

		em.refresh(result);  //refresh from first level cache
		result = repository.findByCounterName(counterName);
		Assert.assertEquals(1L, result.getVersionCount());
	}


	@Configuration
	@EnableJpaRepositories("com.hp.oo.engine.versioning.repositories")
	@EnableTransactionManagement
	static class Configurator {
		@Autowired
		private DataSource dataSource;

		@Autowired
		private JpaVendorAdapter jpaVendorAdapter;

		@Autowired
		private EntityManagerFactory emf;

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
		SpringLiquibase liquibase() {
			SpringLiquibase liquibase = new SpringLiquibase();
			liquibase.setDataSource(dataSource);
			liquibase.setChangeLog("classpath:/META-INF/database/test.changes.xml");
			SimpleHiloIdentifierGenerator.setDataSource(dataSource);
			return liquibase;
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
			adapter.setShowSql(SHOW_SQL);
			adapter.setGenerateDdl(true);
			return adapter;
		}

		@Bean(name="entityManagerFactory")
		@DependsOn("liquibase")
		FactoryBean<EntityManagerFactory> emf() {
			LocalContainerEntityManagerFactoryBean fb = new LocalContainerEntityManagerFactoryBean();
			fb.setJpaProperties(hibernateProperties());
			fb.setDataSource(dataSource());
			fb.setPersistenceProviderClass(HibernatePersistenceProvider.class);
			fb.setPackagesToScan("com.hp.oo.engine.versioning");
			fb.setJpaVendorAdapter(jpaVendorAdapter);
			return fb;
		}

		@Bean
		PlatformTransactionManager transactionManager() throws Exception {
			return new JpaTransactionManager(emf);
		}
	}
}
