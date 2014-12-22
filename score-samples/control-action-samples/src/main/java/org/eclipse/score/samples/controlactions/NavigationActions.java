/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package org.eclipse.score.samples.controlactions;

/**
 * User: stoneo
 * Date: 20/07/2014
 * Time: 17:36
 */
public class NavigationActions {

    private static long nextStep = 0;

    public long nextStepNavigation(){
        nextStep++;
        return nextStep;
    }

    public long simpleNavigation(Long nextStepId){
        return nextStepId;
    }
}
