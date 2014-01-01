package com.hp.oo.orchestrator.services;

import com.hp.oo.orchestrator.entities.ExecutionBoundInputEntity;
import java.util.List;

public interface ExecutionBoundInputsService {
    /**
     * Create a bound input entity and persist it to the DB
     * @param executionId the executionId the input was recorded under
     * @param inputName the name of the input
     * @param domainTermName the name of the domain term
     * @param value the bounded value of this input
     */
    void createBoundInput(String executionId, String inputName, String domainTermName, String value);


    /**
     * Create a list of bound inputs
     * @param executionBoundInputs a collection of bound input entities
     */
    void createBoundInputs(List<ExecutionBoundInputEntity> executionBoundInputs);

    /**
     * Filters all bound inputs by 2 params
     * @param inputName the name of the input
     * @param value the value of the input
     * @return a list of execution id's that were filtered
     */
    List<String> readExecutionIdsByInputNameAndValue(String inputName, String value);

    /**
     * Filters all bound inputs by 2 params
     * @param domainTermName the name of the domain term
     * @param value the value of the input
     * @return a list of execution id's that were filtered
     */
    List<String> readExecutionIdsByDomainTermNameAndValue(String domainTermName, String value);
}
