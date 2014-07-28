package com.hp.oo.engine.node.services;

import com.google.common.collect.Multimap;
import com.hp.oo.engine.node.entities.WorkerNode;
import com.hp.oo.engine.node.repositories.WorkerNodeRepository;
import com.hp.oo.engine.versioning.services.VersionService;
import com.hp.oo.enginefacade.Worker;
import com.hp.score.engine.data.SimpleHiloIdentifierGenerator;
import junit.framework.Assert;
import liquibase.integration.spring.SpringLiquibase;
import org.apache.commons.dbcp.BasicDataSource;
import org.hibernate.ejb.HibernatePersistence;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
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
import org.springframework.security.authentication.encoding.MessageDigestPasswordEncoder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by IntelliJ IDEA.
 * User: Amit Levin
 * Date: 15/11/12
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@Transactional @TransactionConfiguration(defaultRollback=true)
public class WorkerNodeServiceTest {
	private static final boolean SHOW_SQL = false;

	@Autowired
	private WorkerNodeService workerNodeService;

    @Autowired
    private WorkerLockService workerLockService;

	@Autowired
	private UserDetailsService userDetailsService;

	@Autowired
	private WorkerNodeRepository workerNodeRepository;

    @Autowired
    private VersionService versionService;

	@Before
	public void initNodes() {
		workerNodeService.create("H1", "H1", "amit.levin", "c:/dir");

		workerNodeService.create("H2", "H2", "dima.rassin", "c:/dir");
	}

    @After
    public void reset(){
        Mockito.reset(versionService,workerLockService);
    }
	@Test
	public void keepAlive() throws Exception {
        when(versionService.getCurrentVersion(anyString())).thenReturn(5L);

		WorkerNode worker = workerNodeService.readByUUID("H1");
		Date origDate = worker.getAckTime();
		Assert.assertNull(origDate);
		workerNodeService.keepAlive("H1");
		workerNodeRepository.flush();
		worker = workerNodeService.readByUUID("H1");
		Assert.assertNotNull(worker.getAckTime());
        Assert.assertEquals(5, worker.getAckVersion());

	}

	@Test
	public void createNode() throws Exception {
		workerNodeService.create("H3", "H3", "amit.levin", "c:/dir");
        verify(workerLockService).create("H3");
		WorkerNode worker = workerNodeService.readByUUID("H3");
		Assert.assertNotNull(worker);
		workerNodeService.delete("H3");
        verify(workerLockService).delete("H3");
	}

	@Test(expected = IllegalStateException.class)
	public void deleteNode() throws Exception {
		workerNodeService.create("H3", "H3", "dima.rassin", "c:/dir");
        verify(workerLockService).create("H3");
		WorkerNode worker = workerNodeService.readByUUID("H3");
		Assert.assertNotNull(worker);
		workerNodeService.delete("H3");
        verify(workerLockService).delete("H3");
		workerNodeService.readByUUID("H3");
	}

	@Test
	public void login() throws Exception {
		workerNodeService.create("H3", "H3", "dima.rassin", "c:/dir");
		WorkerNode worker = workerNodeService.readByUUID("H3");
		Assert.assertEquals(Worker.Status.FAILED, worker.getStatus());
		workerNodeService.up("H3");
		worker = workerNodeService.readByUUID("H3");
		Assert.assertEquals(Worker.Status.RUNNING, worker.getStatus());

		workerNodeService.delete("H3");
	}

	@Test
	public void readByUUID() throws Exception {
		WorkerNode worker = workerNodeService.readByUUID("H1");
		Assert.assertNotNull(worker);
	}

	@Test
	public void readAllWorkers() throws Exception {
		List<WorkerNode> workers = workerNodeService.readAllWorkers();
		Assert.assertEquals(2, workers.size());
	}

    @Test
    public void readAllNotDeletedWorkers() {
        workerNodeService.create("H3", "H3", "dima.rassin", "c:/dir");
        List<WorkerNode> workers = workerNodeService.readAllNotDeletedWorkers();
        Assert.assertEquals(3, workers.size());
        workerNodeService.updateWorkerToDeleted("H3");
        workers = workerNodeService.readAllNotDeletedWorkers();
        Assert.assertEquals(2, workers.size());
        workerNodeService.delete("H3");
    }

    @Test
    public void cantDeleteRunningWorker() {
        workerNodeService.create("H3", "H3", "dima.rassin", "c:/dir");
        WorkerNode worker = workerNodeService.readByUUID("H3");
        Assert.assertEquals(Worker.Status.FAILED, worker.getStatus());
        workerNodeService.up("H3");
        boolean exceptionHappened = false;
        try {
            workerNodeService.updateWorkerToDeleted("H3");
        }
        catch (IllegalArgumentException e) {
            exceptionHappened = true;
        }
        Assert.assertTrue(exceptionHappened);
        workerNodeService.delete("H3");
    }


	@Test
	public void readNonRespondingWorkers() throws Exception {
		List<String> workers = workerNodeService.readNonRespondingWorkers();
		Assert.assertEquals(0, workers.size());

		workerNodeService.create("H3", "H3", "dima.rassin", "c:/dir");
		workers = workerNodeService.readNonRespondingWorkers();
		Assert.assertEquals(0, workers.size());

		// only activate workers can be non responding
		workerNodeService.activate("H3");
		workers = workerNodeService.readNonRespondingWorkers();
		Assert.assertEquals(0, workers.size());//still it not "non responding" because it yet to login(first login)

		// after login version is current system version.
		workerNodeService.up("H3");
		workers = workerNodeService.readNonRespondingWorkers();
		Assert.assertEquals(0, workers.size());

        //when the worker version is too far from the system version its NonResponding
        when(versionService.getCurrentVersion(anyString())).thenReturn(100L);
        workers = workerNodeService.readNonRespondingWorkers();
        Assert.assertEquals(3, workers.size());

        //after up the worker version will be aligned with current system version.
        workerNodeService.up("H3");
        workers = workerNodeService.readNonRespondingWorkers();
        Assert.assertEquals(2, workers.size());
        Assert.assertFalse(workers.contains("H3"));

		workerNodeService.delete("H3");

	}

	@Test
	public void readWorkersByActivation() throws Exception {

		workerNodeService.create("H3", "H3", "dima.rassin", "c:/dir");
		List<WorkerNode> workers = workerNodeService.readWorkersByActivation(true);
		Assert.assertEquals(0, workers.size());
		workers = workerNodeService.readWorkersByActivation(false);
		Assert.assertEquals(3, workers.size());

		// activate worker
		workerNodeService.activate("H3");
		workers = workerNodeService.readWorkersByActivation(true);
		Assert.assertEquals(1, workers.size());
		workers = workerNodeService.readWorkersByActivation(false);
		Assert.assertEquals(2, workers.size());

		// deactivate worker
		workerNodeService.deactivate("H3");
		workers = workerNodeService.readWorkersByActivation(true);
		Assert.assertEquals(0, workers.size());
		workers = workerNodeService.readWorkersByActivation(false);
		Assert.assertEquals(3, workers.size());

		workerNodeService.delete("H3");
	}

	@Test
	public void updateEnvironmentParams() throws Exception {
		workerNodeService.create("H3", "H3", "dima.rassin", "c:/dir");
		workerNodeService.updateEnvironmentParams("H3", "Window", "7.0", "4");
		WorkerNode worker = workerNodeService.readByUUID("H3");
		Assert.assertEquals("Window", worker.getOs());
		Assert.assertEquals("7.0", worker.getJvm());
		Assert.assertEquals("4", worker.getDotNetVersion());

		workerNodeService.delete("H3");
	}

	@Test
	public void updateDescription() throws Exception {
		workerNodeService.create("H3", "H3", "dima.rassin", "c:/dir");
		WorkerNode worker = workerNodeService.readByUUID("H3");
		Assert.assertEquals("H3", worker.getDescription());

		workerNodeService.updateDescription("H3", "My worker");
		worker = workerNodeService.readByUUID("H3");
		Assert.assertEquals("My worker", worker.getDescription());

		workerNodeService.delete("H3");
	}

	@Test
	public void updateStatus() {
		workerNodeService.create("H3", "H3", "dima.rassin", "c:/dir");
		WorkerNode worker = workerNodeService.readByUUID("H3");
		Assert.assertEquals(Worker.Status.FAILED, worker.getStatus());

		workerNodeService.updateStatus("H3",Worker.Status.RUNNING);
		worker = workerNodeService.readByUUID("H3");
		Assert.assertEquals(Worker.Status.RUNNING, worker.getStatus());

		workerNodeService.delete("H3");
	}

    @Test
    public void updateBulkNumber() {
        workerNodeService.create("H3", "H3", "dima.rassin", "c:/dir");

        workerNodeService.updateBulkNumber("H3", "123");

        WorkerNode worker = workerNodeService.readByUUID("H3");
        Assert.assertEquals("123", worker.getBulkNumber());

        workerNodeService.delete("H3");
    }

	@Test
	public void readAllWorkerGroups() {
		List<String> groups = workerNodeService.readAllWorkerGroups();
		Assert.assertEquals(WorkerNode.DEFAULT_WORKER_GROUPS.length, groups.size());

		workerNodeService.create("H3", "H3", "dima.rassin", "c:/dir");
		workerNodeService.updateWorkerGroups("H3", "group 1", "group 2");
		workerNodeService.updateWorkerGroups("H1", "group 1");
		groups = workerNodeService.readAllWorkerGroups();
		Assert.assertEquals(WorkerNode.DEFAULT_WORKER_GROUPS.length+2, groups.size());

		workerNodeService.updateWorkerGroups("H3");
		WorkerNode workerNode = workerNodeService.readByUUID("H3");
		Assert.assertTrue(workerNode.getGroups().isEmpty());
	}

	@Test
	public void readWorkersByGroup() {

		workerNodeService.create("H3", "H3", "dima.rassin", "c:/dir");
		workerNodeService.updateWorkerGroups("H3", "group 1", "group 2");
		workerNodeService.updateWorkerGroups("H1", "group 1");

		// H3 active, H1 deactive
		workerNodeService.activate("H3");
		List<WorkerNode> workers = workerNodeService.readWorkersByGroup("group 1", true);
		Assert.assertEquals(1, workers.size());
		workers = workerNodeService.readWorkersByGroup("group 2", true);
		Assert.assertEquals(1, workers.size());
		
		// test active and deactive groups.
		workers = workerNodeService.readWorkersByGroup("group 1", false);
		Assert.assertEquals(2, workers.size());
		workers = workerNodeService.readWorkersByGroup("group 2", false);
		Assert.assertEquals(1, workers.size());
	}

	@Test
	public void readGroupWorkersMap() throws Exception {

		workerNodeService.create("H3", "H3", "dima.rassin", "c:/dir");
		workerNodeService.updateWorkerGroups("H3", "group 1", "group 2");
		workerNodeService.updateWorkerGroups("H1", "group 1");

		// H3 active, H1 deactive
		workerNodeService.activate("H3");
		Multimap<String, String> groupWorkersMap = workerNodeService.readGroupWorkersMap(true);
		Collection<String> workerNames = groupWorkersMap.get("group 1");
		Assert.assertEquals(1, workerNames.size());
		workerNames = groupWorkersMap.get("group 2");
		Assert.assertEquals(1, workerNames.size());


		groupWorkersMap = workerNodeService.readGroupWorkersMap(false);
		workerNames = groupWorkersMap.get("group 1");
		Assert.assertEquals(2, workerNames.size());
		workerNames = groupWorkersMap.get("group 2");
		Assert.assertEquals(1, workerNames.size());


		workerNodeService.delete("H3");
	}

	@Test
	public void loadUserDetails() throws Exception {
		try {
            userDetailsService.loadUserByUsername("H3");
			Assert.assertTrue(true);
		} catch(UsernameNotFoundException e){

		}

		workerNodeService.create("H3", "H3", "dima.rassin", "c:/dir");
		UserDetails userDetails = userDetailsService.loadUserByUsername("H3");
		Assert.assertEquals("H3",userDetails.getUsername());
		Assert.assertEquals(4,userDetails.getAuthorities().size());
		
		workerNodeService.delete("H3");
	}

	@Test
	public void checkIfGroupsExist() {
		workerNodeService.updateWorkerGroups("H1", "group 1", "group 2");
		workerNodeService.updateWorkerGroups("H2", "group 1");

		List<String> result = workerNodeService.readWorkerGroups(Arrays.asList("group 1"));
		Assert.assertEquals(Arrays.asList("group 1"), result);
	}

	@Test
	public void addWorkerGroup() {
		WorkerNode workerNode = workerNodeService.readByUUID("H1");
		int groupSize = workerNode.getGroups().size();

		workerNodeService.addGroupToWorker("H1", "aaa");
		workerNode = workerNodeService.readByUUID("H1");

		Assert.assertEquals(groupSize+1, workerNode.getGroups().size());
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
			fb.setPersistenceProviderClass(HibernatePersistence.class);
			fb.setPackagesToScan("com.hp.oo.engine.node");
			fb.setJpaVendorAdapter(jpaVendorAdapter);
			return fb;
		}

		@Bean
		PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
			return new JpaTransactionManager(emf);
		}

		@Bean
		VersionService versionService() {
			VersionService versionService = mock(VersionService.class);
			when(versionService.getCurrentVersion(anyString())).thenReturn(1L);
			return versionService;
		}

		@Bean
		WorkerNodeService workerNodeService(){
			return new WorkerNodeServiceImpl();
		}

        @Bean
        WorkerLockService workerLockService() {
            return mock(WorkerLockService.class);
        }

		@Bean
		MessageDigestPasswordEncoder encoder(){
			return new MessageDigestPasswordEncoder("sha-256");
		}
	}
}
