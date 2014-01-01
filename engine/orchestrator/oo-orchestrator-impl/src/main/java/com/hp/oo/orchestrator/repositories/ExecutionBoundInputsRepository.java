package com.hp.oo.orchestrator.repositories;

import com.hp.oo.orchestrator.entities.ExecutionBoundInputEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * User: kravtsov
 * Date: 20/12/12
 * Time: 15:34
 */
public interface ExecutionBoundInputsRepository extends JpaRepository<ExecutionBoundInputEntity, Long> {
    ExecutionBoundInputEntity findByInputNameAndDomainTermNameAndValue(String inputName, String domainTermName, String value);
    ExecutionBoundInputEntity findByInputNameAndValue(String inputName, String value);
    ExecutionBoundInputEntity findByDomainTermNameAndValue(String domainTermName, String value);
}