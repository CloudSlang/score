package com.hp.oo.broker.services;

import com.hp.oo.broker.entities.BranchContextHolder;

/**
 * User: stoneo
 * Date: 20/07/2014
 * Time: 11:08
 */
public interface ParallelPersistenceService {

    java.util.List<BranchContextHolder> readBranchContextById(java.lang.String s);

    BranchContextHolder createBranchContext(BranchContextHolder branchContextHolder);

    void deleteParallelRecordsById(java.lang.String s);

    int countBranchContextBySplitId(java.lang.String s);
}
