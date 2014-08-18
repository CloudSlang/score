package org.score.samples.controlactions;

import com.hp.oo.enginefacade.execution.EndBranchDataContainer;
import com.hp.score.lang.ExecutionRuntimeServices;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: wahnonm
 * Date: 14/08/14
 * Time: 10:36
 */
public class BranchActions {

    public void split(ExecutionRuntimeServices executionRuntimeServices){
        executionRuntimeServices.addBranch(1L, "1", new HashMap<String, Serializable>());
    }

    public void join(ExecutionRuntimeServices executionRuntimeServices,Map<String, Serializable> executionContext){
        List<EndBranchDataContainer> branches = executionRuntimeServices.getFinishedChildBranchesData();
        for (EndBranchDataContainer branch : branches) {
            Map<String,Serializable> branchContext  = branch.getContexts();
            executionContext.putAll(branchContext);
        }
    }

    public void parallelSplit(ExecutionRuntimeServices executionRuntimeServices){
        executionRuntimeServices.addBranch(1L, "1", new HashMap<String, Serializable>());
        executionRuntimeServices.addBranch(1L, "1", new HashMap<String, Serializable>());
    }
}
