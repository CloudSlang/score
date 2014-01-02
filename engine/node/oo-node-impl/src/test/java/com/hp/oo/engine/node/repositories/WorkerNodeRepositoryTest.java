package com.hp.oo.engine.node.repositories;

import com.hp.oo.engine.node.entities.WorkerNode;
import com.hp.oo.engine.versioning.services.VersionService;
import com.hp.oo.enginefacade.Worker;
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
import org.springframework.context.annotation.ImportResource;
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

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by IntelliJ IDEA.
 * User: froelica
 * Date: 9/8/13
 * Time: 10:25 AM
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@Transactional
@TransactionConfiguration(defaultRollback = true)
public class WorkerNodeRepositoryTest {
	private static final boolean SHOW_SQL = false;

	@Autowired
	private WorkerNodeRepository workerNodeRepository;

	@Test
	public void findGroupsTest() {
		WorkerNode worker = new WorkerNode();
		worker.setUuid("some faked uuid");
		worker.setHostName("worker host name");
		worker.setInstallPath("faked installation path");
		worker.setPassword("faked password");
		worker.setStatus(Worker.Status.RUNNING);
		worker.setActive(true);
		worker.setGroups(Arrays.asList("group1", "group2", "group3"));
		workerNodeRepository.saveAndFlush(worker);

		List<String> expected = Arrays.asList("group1", "group2");
		List<String> result = workerNodeRepository.findGroups(expected);
		Assert.assertEquals(expected, result);
	}

	@Configuration
	@EnableJpaRepositories("com.hp.oo.engine.node.repositories")
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
		FactoryBean<EntityManagerFactory> emf(JpaVendorAdapter jpaVendorAdapter) {
			LocalContainerEntityManagerFactoryBean fb = new LocalContainerEntityManagerFactoryBean();
			fb.setJpaProperties(hibernateProperties());
			fb.setDataSource(dataSource());
			fb.setPersistenceProviderClass(HibernatePersistenceProvider.class);
			fb.setPackagesToScan("com.hp.oo.engine.node");
			fb.setJpaVendorAdapter(jpaVendorAdapter);
			return fb;
		}

		@Bean
		PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
			return new JpaTransactionManager(emf);
		}

		@Bean
		public VersionService versionService() {
			VersionService versionService = mock(VersionService.class);
			when(versionService.getCurrentVersion(anyString())).thenReturn(1L);
			return versionService;
		}
	}
}
