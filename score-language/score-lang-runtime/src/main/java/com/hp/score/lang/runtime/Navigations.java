package com.hp.score.lang.runtime;

import com.hp.oo.sdk.content.annotations.Param;
import com.hp.score.lang.ExecutionRuntimeServices;

/**
 * User: stoneo
 * Date: 22/10/2014
 * Time: 15:32
 */
public class Navigations {

    public Long navigate(String subFlowId,
                                @Param("runEnv") RunEnvironment runEnv,
                                ExecutionRuntimeServices executionRuntimeServices,
                                Long RUNNING_EXECUTION_PLAN_ID,
                                Long nextStepId){
        if(subFlowId != null){
            ParentFlowStack stack = runEnv.getParentFlowStack();
            stack.pushParentFlowData(new ParentFlowData(RUNNING_EXECUTION_PLAN_ID, nextStepId));

            Long subFlowRunningExecutionPlanId = executionRuntimeServices.getSubFlowRunningExecutionPlan(subFlowId);
//            executionRuntimeServices.requestToChangeExecutionPlan(subFlowRunningExecutionPlanId);
            return executionRuntimeServices.getSubFlowBeginStep(subFlowId);
        }

        if (nextStepId == null && !runEnv.getParentFlowStack().isEmpty()) {
            ParentFlowData parentFlowData = runEnv.getParentFlowStack().popParentFlowData();
//            executionRuntimeServices.requestToChangeExecutionPlan(parentFlowData.getRunningExecutionPlanId());
            return parentFlowData.getPosition();
        }

        return nextStepId;
    }
}
