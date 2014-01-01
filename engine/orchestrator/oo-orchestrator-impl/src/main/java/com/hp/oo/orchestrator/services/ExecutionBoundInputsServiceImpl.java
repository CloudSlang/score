package com.hp.oo.orchestrator.services;

import com.hp.oo.orchestrator.entities.ExecutionBoundInputEntity;
import com.hp.oo.orchestrator.repositories.ExecutionBoundInputsRepository;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public final class ExecutionBoundInputsServiceImpl implements ExecutionBoundInputsService {

    private final Logger logger = Logger.getLogger(getClass());

    @Autowired
    private ExecutionBoundInputsRepository repository;

    @Override
    @Transactional
    public void createBoundInput(String executionId, String inputName, String domainTermName, String value) {
        Validate.notNull(executionId, "Execution Id cannot be null");
        Validate.notNull(inputName, "Input Name cannot be null");

        HashSet<String> executionIds = new HashSet<>();
        executionIds.add(executionId);
        createInputInner(executionIds, inputName, domainTermName, value);
    }

    @Override
    @Transactional
    public void createBoundInputs(List<ExecutionBoundInputEntity> executionBoundInputs) {
        for (ExecutionBoundInputEntity boundInput : executionBoundInputs) {
            Validate.notEmpty(boundInput.getExecutionIds(), "Execution ids cannot be empty");
            Validate.notNull(boundInput.getInputName(), "Input name cannot be null");

            createInputInner(boundInput.getExecutionIds(), boundInput.getInputName(), boundInput.getDomainTermName(), boundInput.getValue());
        }
    }

    private void createInputInner(Set<String> executionIds, String inputName, String domainTermName, String value) {
        ExecutionBoundInputEntity entity = repository.findByInputNameAndDomainTermNameAndValue(inputName, domainTermName, value);

        if (entity == null) {
            if (logger.isDebugEnabled()) logger.debug("Creating a bound input for a new combination of inputName: "+inputName + " domainTermName: "+domainTermName+" value: "+value);
            entity = new ExecutionBoundInputEntity();
            entity.setInputName(inputName);
            entity.setDomainTermName(domainTermName);
            entity.setValue(value);
        } else {
            if (logger.isDebugEnabled()) logger.debug("Creating a bound input for for an existing combination of inputName: "+inputName + " domainTermName: "+domainTermName+" value: "+value);
        }

        Set<String> mergedExecutionIds = new HashSet<>();
        mergedExecutionIds.addAll(entity.getExecutionIds());
        mergedExecutionIds.addAll(executionIds);
        entity.setExecutionIds(mergedExecutionIds);

        repository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> readExecutionIdsByInputNameAndValue(String inputName, String value) {
        Validate.notNull(inputName, "Input name cannot be null");
        List<String> filteredExecutionIds = new ArrayList<>();

        ExecutionBoundInputEntity foundInput = repository.findByInputNameAndValue(inputName, value);

        if (foundInput != null) {
            filteredExecutionIds = new ArrayList<>(foundInput.getExecutionIds());
        }

        return filteredExecutionIds;
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> readExecutionIdsByDomainTermNameAndValue(String domainTermName, String value) {
        List<String> filteredExecutionIds = new ArrayList<>();

        ExecutionBoundInputEntity foundInput = repository.findByDomainTermNameAndValue(domainTermName, value);

        if (foundInput != null) {
            filteredExecutionIds = new ArrayList<>(foundInput.getExecutionIds());
        }

        return filteredExecutionIds;
    }

    private ExecutionBoundInputEntity createBoundInputEntity(String inputName, String domainTermName, String value) {
        ExecutionBoundInputEntity res = new ExecutionBoundInputEntity();

        res.setInputName(inputName);
        res.setDomainTermName(domainTermName);
        res.setValue(value);
        res.setExecutionIds(new HashSet<String>());

        return res;
    }

}
