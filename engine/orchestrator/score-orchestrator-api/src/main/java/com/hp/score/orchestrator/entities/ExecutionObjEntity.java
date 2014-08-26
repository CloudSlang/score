package com.hp.score.orchestrator.entities;

import com.hp.oo.internal.sdk.execution.Execution;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Lob;

/**
 * Created by IntelliJ IDEA.
 * User: Amit Levin
 * Date: 28/11/13
 */
@Embeddable
public class ExecutionObjEntity {


    @Lob
    @Column(name = "EXECUTION_OBJECT")
    private Execution executionObj;


    public Execution getExecutionObj() {
        return executionObj;
    }

    public void setExecutionObj(Execution executionObj) {
        this.executionObj = executionObj;
    }

    public ExecutionObjEntity(){

    }

    public ExecutionObjEntity(Execution executionObj){
           this.executionObj = executionObj;
    }



}
