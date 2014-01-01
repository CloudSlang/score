package com.hp.oo.orchestrator.repositories;

import com.hp.oo.orchestrator.entities.ExecutionInterrupts;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: hajyhia
 * Date: 2/24/13
 * Time: 5:59 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ExecutionInterruptsRepository extends JpaRepository<ExecutionInterrupts, Long> {
     public ExecutionInterrupts findByExecutionIdAndType(String executionId, String type);
     public List<ExecutionInterrupts> findByExecutionId(String executionId);
}
