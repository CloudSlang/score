package com.hp.oo.partitions.services;

import com.hp.oo.partitions.entities.PartitionGroup;
import com.hp.oo.partitions.repositories.PartitionGroupRepository;
import com.hp.score.engine.data.SimpleHiloIdentifierGenerator;
import org.apache.commons.dbcp.BasicDataSource;
import org.hibernate.ejb.HibernatePersistence;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.MethodInvokingFactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

import static org.fest.assertions.Assertions.assertThat;


/**
 * Date: 4/23/12
 *
 * @author Dima Rassin
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@Transactional
@TransactionConfiguration(defaultRollback = true)
public class PartitionTemplateWithEmfTest {
	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private PartitionService service;

	private static final String tableName = "my_table";

	@Test @DirtiesContext
	public void startFirstTime() {
		applicationContext.getBean(tableName,PartitionTemplate.class);
		PartitionGroup partitionGroup = service.readPartitionGroup(tableName);
		assertThat(partitionGroup).isNotNull();
		assertThat(partitionGroup.getName()).isEqualTo(tableName);
	}

	@Test @DirtiesContext
	public void startSecondTime() { // the partition group is already exist
		service.createPartitionGroup(tableName,Integer.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE);
		PartitionGroup partitionGroupOrig = service.readPartitionGroup(tableName);

		applicationContext.getBean(tableName,PartitionTemplate.class);
		PartitionGroup partitionGroup = service.readPartitionGroup(tableName);
		assertThat(partitionGroup).isEqualTo(partitionGroupOrig);
	}


	@Test @DirtiesContext
	public void iteratorTest() {
		service.createPartitionGroup(tableName,Integer.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE);
		PartitionGroup partitionGroupOrig = service.readPartitionGroup(tableName);

		applicationContext.getBean(tableName,PartitionTemplate.class);
		PartitionGroup partitionGroup = service.readPartitionGroup(tableName);
		assertThat(partitionGroup).isEqualTo(partitionGroupOrig);
	}

    @Test @DirtiesContext
    public void updatePartitionGroup() {
        service.createPartitionGroup(tableName,Integer.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE);

        final String newGroupName = "newName";
        final int newGroupSize = 10;
        final int newTimeThreshold = 20;
        final int newSizeThreshold = 30;

        service.updatePartitionGroup(newGroupName, newGroupSize, newTimeThreshold, newSizeThreshold);

        applicationContext.getBean(tableName,PartitionTemplate.class);
        PartitionGroup partitionGroup = service.readPartitionGroup(tableName);
        assertThat(partitionGroup.getName().equals(newGroupName));
        assertThat(partitionGroup.getGroupSize() == newGroupSize);
        assertThat(partitionGroup.getSizeThreshold() == newTimeThreshold);
        assertThat(partitionGroup.getSizeThreshold() == newSizeThreshold);
    }

	@Configuration
	static class Configurator {
		@Autowired
		private JpaRepositoryFactory repositoryFactory;

		@Bean PartitionGroupRepository repository() {
			return repositoryFactory.getRepository(PartitionGroupRepository.class);
		}

		@Bean PartitionService service() {
			return new PartitionServiceImpl();
		}

		@Bean(name=tableName) @Lazy PartitionTemplate template(){
			return new PartitionTemplateImpl();
		}

		@Configuration
		@EnableTransactionManagement
		static class EMFContext{
			@Autowired EntityManagerFactory emf;
			@PersistenceContext	EntityManager entityManager;


			@Bean Properties hibernateProperties(){
				Properties properties = new Properties();
				try {
					properties.load(new StringReader(
						"hibernate.format_sql=true" + "\n" +
						"hibernate.cache.provider_class=org.hibernate.cache.NoCacheProvider" + "\n" +
						"hibernate.cache.use_query_cache=false" + "\n" +
						"hibernate.generate_statistics=false" + "\n" +
						"hibernate.cache.use_second_level_cache=false" + "\n" +
						"hibernate.jdbc.fetch_size=100" + "\n" +
						"hibernate.jdbc.batch_size=30" + "\n" +
						"hibernate.order_updates=true" + "\n" +
						"hibernate.order_inserts=true" + "\n" +
						"hibernate.default_batch_fetch_size=20" + "\n" +
						"hibernate.hbm2ddl.auto=validate" + "\n" +
						"hibernate.dialect_resolvers=org.hibernate.service.jdbc.dialect.internal.StandardDialectResolver"
					));
				} catch (IOException e) {throw new RuntimeException(e);}
				return properties;
			}

			@Bean DataSource dataSource(){
				BasicDataSource ds = new BasicDataSource();
				ds.setDriverClassName("org.h2.Driver");
				ds.setUrl("jdbc:h2:mem:test");
				ds.setUsername("sa");
				ds.setPassword("sa");
				ds.setDefaultAutoCommit(false);
				return new TransactionAwareDataSourceProxy(ds);
			}

            @Bean(name="initHiloGenerator") MethodInvokingFactoryBean initHiloGenerator(){
                MethodInvokingFactoryBean methodInvokingFactoryBean = new MethodInvokingFactoryBean();
                methodInvokingFactoryBean.setTargetClass(SimpleHiloIdentifierGenerator.class);
                methodInvokingFactoryBean.setTargetMethod("setDataSource");
                methodInvokingFactoryBean.setArguments(new Object[]{dataSource()});
                return methodInvokingFactoryBean;
            }

			@Bean JpaVendorAdapter jpaVendorAdapter(){
				HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
				adapter.setShowSql(false);
				adapter.setGenerateDdl(true);
				return adapter;
			}

			@Bean @DependsOn({"liquibase","initHiloGenerator"}) FactoryBean<EntityManagerFactory> emf(){
				LocalContainerEntityManagerFactoryBean fb = new LocalContainerEntityManagerFactoryBean();
				fb.setJpaProperties(hibernateProperties());
				fb.setDataSource(dataSource());
				fb.setPersistenceProviderClass(HibernatePersistence.class);
				fb.setPackagesToScan("com.hp.oo.partitions");
				fb.setJpaVendorAdapter(jpaVendorAdapter());
				return fb;
			}

			@Bean PlatformTransactionManager transactionManager() throws Exception {
				return new JpaTransactionManager(emf);
			}

			@Bean TransactionTemplate createTransactionTemplate() throws Exception {
				return new TransactionTemplate(transactionManager());
			}

			@Bean JdbcTemplate jdbcTemplate(){
				return new JdbcTemplate(dataSource());
			}

			@Bean JpaRepositoryFactory artifactRepositoryFactory() throws Exception {
				return new JpaRepositoryFactory(entityManager);
			}

            @Bean public PartitionUtils partitionUtils() {
                return new PartitionUtils();
            }
		}
	}
}
