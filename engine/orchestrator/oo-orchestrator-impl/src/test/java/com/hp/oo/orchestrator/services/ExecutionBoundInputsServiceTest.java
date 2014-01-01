package com.hp.oo.orchestrator.services;

import com.hp.oo.orchestrator.entities.ExecutionBoundInputEntity;
import com.hp.oo.orchestrator.repositories.ExecutionBoundInputsRepository;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.*;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class ExecutionBoundInputsServiceTest {

    @Autowired
    private ExecutionBoundInputsService service;

    @Autowired
    private ExecutionBoundInputsRepository repository;

    @Before
    public void resetMocks() {
        reset(repository);
    }

    /*
        This test makes sure that the service tries to fetch existing entities from the repository
        and merge the execution ids with them,
        the scenario it simulates is when the input/term/value combo already exists for a given set of executions
     */
    @Test
    public void createBoundInputWithExistingComboTest() {
        String value = "value";
        String domainTermName = "domainTermName";
        String inputName = "inputName";

        String[] dbExecutions = {"111", "222", "333"};
        String[] serviceExecutions = {"111", "222", "333", "444"};

        ExecutionBoundInputEntity dbEntity = createExecutionBoundInputEntity(new HashSet<>(Arrays.asList(dbExecutions)), inputName, domainTermName, value);
        ExecutionBoundInputEntity serviceEntity = createExecutionBoundInputEntity(new HashSet<>(Arrays.asList(serviceExecutions)), inputName, domainTermName, value);

        // mock it
        when(repository.findByInputNameAndDomainTermNameAndValue(inputName, domainTermName, value)).thenReturn(dbEntity);

        // now try to create an input with an already existent combination, but with a new execution id
        service.createBoundInput("444", inputName, domainTermName, value);

        // verify that the execution id's were successfully merged
        verify(repository).save(serviceEntity);
    }

    /*
        This test simulates a scenario where the input/term/value combo doesn't exist yet
    */
    @Test
    public void createBoundInputWithNewInputTest() {
        String value = "value";
        String domainTermName = "domainTermName";
        String inputName = "inputName";

        String[] serviceExecutions = {"444"};

        ExecutionBoundInputEntity serviceEntity = createExecutionBoundInputEntity(new HashSet<>(Arrays.asList(serviceExecutions)), inputName, domainTermName, value);

        // mock it
        when(repository.findByInputNameAndDomainTermNameAndValue(inputName, domainTermName, value)).thenReturn(null);

        // now try to create an input with an already existent combination, but with a new execution id
        service.createBoundInput("444", inputName, domainTermName, value);

        // verify that the execution id's were successfully merged
        verify(repository).save(serviceEntity);
    }

    /*
        This test makes sure that the service tries to fetch existing entities from the repository
        and merge the execution ids with them,
        the scenario it simulates is when the input/term/value combo already exists for a given set of executions
    */
    @Test
    public void createBoundInputsWithExistingComboTest() {
        String value = "value";
        String domainTermName = "domainTermName";
        String inputName = "inputName";

        String[] dbExecutions = {"111", "222", "333"};
        String[] serviceExecutions = {"111", "222", "333", "444"};

        ExecutionBoundInputEntity dbEntity = createExecutionBoundInputEntity(new HashSet<>(Arrays.asList(dbExecutions)), inputName, domainTermName, value);
        ExecutionBoundInputEntity serviceEntity = createExecutionBoundInputEntity(new HashSet<>(Arrays.asList(serviceExecutions)), inputName, domainTermName, value);

        // mock it
        when(repository.findByInputNameAndDomainTermNameAndValue(inputName, domainTermName, value)).thenReturn(dbEntity);

        // now try to create an input with an already existent combination, but with a new execution id
        service.createBoundInputs(Arrays.asList(createExecutionBoundInputEntity(new HashSet<>(Arrays.asList("444")), inputName, domainTermName, value)));

        // verify that the execution id's were successfully merged
        verify(repository).save(serviceEntity);
    }

    /*
        This test simulates a scenario where the input/term/value combo doesn't exist yet
   */
    @Test
    public void createBoundInputsWithNewInputTest() {
        String value = "value";
        String domainTermName = "domainTermName";
        String inputName = "inputName";

        String[] serviceExecutions = {"444"};

        ExecutionBoundInputEntity serviceEntity = createExecutionBoundInputEntity(new HashSet<>(Arrays.asList(serviceExecutions)), inputName, domainTermName, value);

        // mock it
        when(repository.findByInputNameAndDomainTermNameAndValue(inputName, domainTermName, value)).thenReturn(null);

        // now try to create an input with an already existent combination, but with a new execution id
        service.createBoundInputs(Arrays.asList(createExecutionBoundInputEntity(new HashSet<>(Arrays.asList("444")), inputName, domainTermName, value)));

        // verify that the execution id's were successfully merged
        verify(repository).save(serviceEntity);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createBoundInputWithInvalidInputName() {
        service.createBoundInput("executionId", null, null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createBoundInputsWithInvalidInputName() {
        ExecutionBoundInputEntity entity = createExecutionBoundInputEntity(new HashSet<String>(), null, null, null);
        service.createBoundInputs(Arrays.asList(entity));
    }

    @Test(expected = IllegalArgumentException.class)
    public void createBoundInputWithInvalidExecutionId() {
        service.createBoundInput(null, "stepName", null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createBoundInputsWithInvalidExecutionId() {
        ExecutionBoundInputEntity entity = createExecutionBoundInputEntity(null, "inputName", null, null);
        service.createBoundInputs(Arrays.asList(entity));
    }

    @Test
    public void readExecutionIdsByDomainTermNameAndValueWhenNotFoundTest() {
        String value = "value";
        String domainTermName = "domainTermName";

        // what happens when nothing is found
        when(repository.findByDomainTermNameAndValue(domainTermName, value)).thenReturn(null);
        List<String> executionIds = service.readExecutionIdsByDomainTermNameAndValue(domainTermName, value);
        Assert.assertTrue("expected an empty list", executionIds.isEmpty());
    }

    @Test
    public void readExecutionIdsByDomainTermNameAndValueWhenFoundTest() {
        String value = "value";
        String domainTermName = "domainTermName";
        String inputName = "inputName";
        String[] executions = {"444", "333"};

        // and when something is found
        ExecutionBoundInputEntity entity = createExecutionBoundInputEntity(new HashSet<>(Arrays.asList(executions)), inputName, domainTermName, value);
        when(repository.findByDomainTermNameAndValue(domainTermName, value)).thenReturn(entity);
        List<String> executionIds = service.readExecutionIdsByDomainTermNameAndValue(domainTermName, value);
        Assert.assertEquals(Arrays.asList(executions), executionIds);
    }

    @Test
    public void readExecutionIdsByInputNameAndValueWhenNotFoundTest() {
        String value = "value";
        String inputName = "inputName";

        // what happens when nothing is found
        when(repository.findByInputNameAndValue(inputName, value)).thenReturn(null);
        List<String> executionIds = service.readExecutionIdsByInputNameAndValue(inputName, value);
        Assert.assertTrue("expected an empty list", executionIds.isEmpty());
    }

    @Test
    public void readExecutionIdsByInputNameAndValueWhenFoundTest() {
        String value = "value";
        String domainTermName = "domainTermName";
        String inputName = "inputName";
        String[] executions = {"444", "333"};

        // and when something is found
        ExecutionBoundInputEntity entity = createExecutionBoundInputEntity(new HashSet<>(Arrays.asList(executions)), inputName, domainTermName, value);
        when(repository.findByInputNameAndValue(inputName, value)).thenReturn(entity);
        List<String> executionIds = service.readExecutionIdsByInputNameAndValue(inputName, value);
        Assert.assertEquals(Arrays.asList(executions), executionIds);
    }

    @Test(expected = IllegalArgumentException.class)
    public void readExecutionIdsByInputNameWithInvalidInputNameTest() {
        service.readExecutionIdsByInputNameAndValue(null, null);
    }

    private ExecutionBoundInputEntity createExecutionBoundInputEntity(Set<String> executionIds, String inputName, String domainTerm, String value) {
        ExecutionBoundInputEntity res = new ExecutionBoundInputEntity();

        res.setExecutionIds(executionIds);
        res.setInputName(inputName);
        res.setDomainTermName(domainTerm);
        res.setValue(value);

        return res;
    }

    @Configuration
    static class Configurator {
        @Bean
        public ExecutionBoundInputsService ExecutionBoundInputsService() {
            return new ExecutionBoundInputsServiceImpl();
        }

        @Bean
        public ExecutionBoundInputsRepository executionSummaryRepository() {
            return Mockito.mock(ExecutionBoundInputsRepository.class);
        }
    }
}
