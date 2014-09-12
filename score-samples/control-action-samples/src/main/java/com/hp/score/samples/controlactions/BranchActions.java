package com.hp.score.samples.controlactions;

import com.hp.score.api.EndBranchDataContainer;
import com.hp.score.lang.ExecutionRuntimeServices;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: wahnonm
 * Date: 14/08/14
 * Time: 10:36
 */
public class BranchActions {

    public static final String STEP_POSITION = "stepPosition";
    public static final String EXECUTION_PLAN_ID = "executionPlanId";
	public static final String BRANCH_RESULTS = "branchResults";
	public static final String BRANCH_CONTEXTS = "branchContexts";
	public static final String PARALLEL_EXECUTION_PLAN_IDS = "parallelExecutionPlanIds";

    public void split(ExecutionRuntimeServices executionRuntimeServices, Long stepPosition, String executionPlanId){
        executionRuntimeServices.addBranch(stepPosition, executionPlanId, new HashMap<String, Serializable>());
	}

    public void splitWithContext(ExecutionRuntimeServices executionRuntimeServices,
								 Map<String, Serializable> executionContext,
								 String flowUuid,
								 Map<String, Serializable> context,
								 List<String> inputKeysFromParentContext){
		Map<String, Serializable> initialContext = new HashMap<>();
		initialContext.putAll(context);

		if (inputKeysFromParentContext != null) {
			for (String inputKey : inputKeysFromParentContext) {
				initialContext.put(inputKey, executionContext.get(inputKey));
			}
		}

		executionRuntimeServices.addBranch(0L, flowUuid, initialContext);
    }

    public void join(ExecutionRuntimeServices executionRuntimeServices,Map<String, Serializable> executionContext){
        List<EndBranchDataContainer> branches = executionRuntimeServices.getFinishedChildBranchesData();
        for (EndBranchDataContainer branch : branches) {
            Map<String,Serializable> branchContext  = branch.getContexts();
            executionContext.putAll(branchContext);
        }
    }
	public void joinBranches(ExecutionRuntimeServices executionRuntimeServices, Map<String, Serializable> executionContext){
		List<EndBranchDataContainer> branches = executionRuntimeServices.getFinishedChildBranchesData();
		List<Map<String, Serializable>> branchResults = new ArrayList<>();

		for (EndBranchDataContainer branch : branches) {
			Map<String,Serializable> branchContext  = branch.getContexts();
			branchResults.add(branchContext);
		}
		executionContext.put(BRANCH_RESULTS, (Serializable) branchResults);
	}


    public void parallelSplit(ExecutionRuntimeServices executionRuntimeServices, Long stepPosition, String executionPlanId){
        executionRuntimeServices.addBranch(stepPosition, executionPlanId, new HashMap<String, Serializable>());
        executionRuntimeServices.addBranch(stepPosition, executionPlanId, new HashMap<String, Serializable>());
    }

	public void multiInstanceWithContext(ExecutionRuntimeServices executionRuntimeServices, Long stepPosition, String executionPlanId, Map<String, Serializable> executionContext){

		@SuppressWarnings("unchecked") List<Map<String, Serializable>> branchContexts =  (List<Map<String, Serializable>>) executionContext.get(BRANCH_CONTEXTS);
		for(Map<String, Serializable> currentBranchContext : branchContexts){
			executionRuntimeServices.addBranch(stepPosition, executionPlanId, currentBranchContext);
		}
	}
	public void parallelSplitWithContext(ExecutionRuntimeServices executionRuntimeServices, Long stepPosition, List<String> parallelExecutionPlanIds, Map<String, Serializable> executionContext){

		@SuppressWarnings("unchecked") List<Map<String, Serializable>> branchContexts =  (List<Map<String, Serializable>>) executionContext.get(BRANCH_CONTEXTS);
		for(int i = 0; i < parallelExecutionPlanIds.size(); ++i){
			executionRuntimeServices.addBranch(stepPosition, parallelExecutionPlanIds.get(i), branchContexts.get(i));
		}
	}

}
