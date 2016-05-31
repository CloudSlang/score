/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/


package io.cloudslang.orchestrator.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class MergedConfigurationDataContainer implements Serializable{
    private volatile List<Long> cancelledExecutions;
    private volatile Set<String> pausedExecutions;
    private volatile List<String> workerGroups;

    public MergedConfigurationDataContainer() {
    }



    public List<Long> getCancelledExecutions() {
        return cancelledExecutions;
    }

    public void setCancelledExecutions(List<Long> cancelledExecutions) {
        this.cancelledExecutions = cancelledExecutions;
    }

    public Set<String> getPausedExecutions() {
        return pausedExecutions;
    }

    public void setPausedExecutions(Set<String> pausedExecutions) {
        this.pausedExecutions = pausedExecutions;
    }

    public List<String> getWorkerGroups() {
        return workerGroups;
    }

    public void setWorkerGroups(List<String> workerGroups) {
        this.workerGroups = workerGroups;
    }

}
