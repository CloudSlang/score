package com.hp.oo.engine.node.repositories;

import com.hp.oo.engine.node.entities.WorkerNode;
import com.hp.oo.engine.versioning.services.VersionService;
import com.hp.oo.enginefacade.Worker;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

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
	@ImportResource({"classpath:/META-INF/spring/nodeSystemTestContext.xml"})
	static class Configurator {
        @Bean
        public VersionService versionService() {
            VersionService versionService = mock(VersionService.class);
            when(versionService.getCurrentVersion(anyString())).thenReturn(1L);
            return versionService;
        }

    }
}
