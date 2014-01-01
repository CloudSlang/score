package com.hp.oo.orchestrator.repositories;

import com.hp.oo.orchestrator.entities.ExecutionBoundInputEntity;
import com.hp.score.engine.data.DataBaseDetector;
import com.hp.score.engine.data.SqlUtils;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@SuppressWarnings({"SpringContextConfigurationInspection"})
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@Transactional
public class ExecutionBoundInputsRepositoryTest {

	@Autowired
	private ExecutionBoundInputsRepository repository;


	@Before
	public void cleanRepository() {
		repository.deleteAll();
	}

	/*
		This repository maps entries to another table in a one -> many fashion,
		this test validates that the mapping actually works
	 */
	@Test
	public void findByInputNameAndDomainTermNameAndValueTest() {
		String[] executionIds = {"111-111", "222-222"};
		String inputName = "inputName";
		String domainTermName = "domainTermName";
		String value = "value";

		ExecutionBoundInputEntity boundInput = createBoundInput(Arrays.asList(executionIds), inputName, domainTermName, value);

		repository.save(boundInput);

		ExecutionBoundInputEntity res = repository.findByInputNameAndDomainTermNameAndValue(inputName, domainTermName, value);

		Assert.assertEquals(inputName, res.getInputName());
		Assert.assertEquals(domainTermName, res.getDomainTermName());
		Assert.assertEquals(value, res.getValue());
		Assert.assertEquals(new HashSet<>(Arrays.asList(executionIds)), res.getExecutionIds());

		res = repository.findByInputNameAndDomainTermNameAndValue("doestExist", "doestExist", "doestExist");
		Assert.assertNull(res);
	}

	/*
		Makes sure the table constraints are in working order
	 */
	@Test(expected = DataIntegrityViolationException.class)
	public void ExecutionBoundInputsUniqueConstraintsTest() {
		String[] executionIds1 = {"111-666", "222-222"};
		String[] executionIds2 = {"111-111", "222-222"};
		String inputName = "inputNameXX";
		String domainTermName = "domainTermNameXX";
		String value = "valueXX";

		ExecutionBoundInputEntity boundInput1 = createBoundInput(Arrays.asList(executionIds1), inputName, domainTermName, value);
		ExecutionBoundInputEntity boundInput2 = createBoundInput(Arrays.asList(executionIds2), inputName, domainTermName, value);
		repository.save(boundInput1);
		repository.save(boundInput2);

		// this should throw an exception now, since we entered invalid data
		repository.flush();
	}

	private ExecutionBoundInputEntity createBoundInput(List<String> executionIds, String inputName, String domainTermName, String value) {
		ExecutionBoundInputEntity res = new ExecutionBoundInputEntity();
		res.setInputName(inputName);
		res.setDomainTermName(domainTermName);
		res.setValue(value);
		res.setExecutionIds(new HashSet<>(executionIds));

		return res;
	}


	@Configuration
	@EnableJpaRepositories
	@EnableTransactionManagement
	@ImportResource("META-INF/spring/orchestratorEmfContext.xml")
	static class Configurator {
		@Bean
		SqlUtils sqlUtils() {
			return new SqlUtils();
		}

		@Bean
		DataBaseDetector dataBaseDetector() {
			return new DataBaseDetector();
		}
	}
}