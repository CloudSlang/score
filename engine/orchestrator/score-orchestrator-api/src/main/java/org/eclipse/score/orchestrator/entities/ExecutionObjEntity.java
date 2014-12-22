/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package org.eclipse.score.orchestrator.entities;

import org.eclipse.score.facade.entities.Execution;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Lob;

/**
 * Created by IntelliJ IDEA.
 * User:
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
