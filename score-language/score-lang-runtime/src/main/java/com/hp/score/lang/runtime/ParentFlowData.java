package com.hp.score.lang.runtime;

import java.io.Serializable;

/**
 * User: stoneo
 * Date: 22/10/2014
 * Time: 15:38
 */
public class ParentFlowData implements Serializable{

    private final Long runningExecutionPlanId;

    private final Long position;


    public ParentFlowData(Long runningExecutionPlanId, Long position) {
        this.runningExecutionPlanId = runningExecutionPlanId;
        this.position = position;
    }

    public Long getRunningExecutionPlanId() {
        return runningExecutionPlanId;
    }

    public Long getPosition() {
        return position;
    }
}
