package com.hp.score.stubs;

import com.hp.oo.broker.entities.BranchContextHolder;
import com.hp.oo.broker.services.ParallelPersistenceService;

import java.util.List;

/**
 * User: stoneo
 * Date: 20/07/2014
 * Time: 14:46
 */
public class StubParallelPersistenceServiceImpl implements ParallelPersistenceService {
    @Override
    public List<BranchContextHolder> readBranchContextById(String s) {
        return null;
    }

    @Override
    public BranchContextHolder createBranchContext(BranchContextHolder branchContextHolder) {
        return null;
    }

    @Override
    public void deleteParallelRecordsById(String s) {

    }

    @Override
    public int countBranchContextBySplitId(String s) {
        return 0;
    }
}
