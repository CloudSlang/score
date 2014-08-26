package com.hp.score.orchestrator.repositories;

import com.hp.score.orchestrator.entities.FinishedBranch;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created with IntelliJ IDEA.
 * User: kravtsov
 * Date: 07/11/13
 * Time: 14:16
 */
public interface FinishedBranchRepository extends JpaRepository<FinishedBranch, Long> {
}
