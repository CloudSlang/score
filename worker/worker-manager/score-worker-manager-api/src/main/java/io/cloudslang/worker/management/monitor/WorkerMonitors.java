/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/

package io.cloudslang.worker.management.monitor;

import io.cloudslang.worker.management.services.WorkerMonitorInfoEnum;

import java.io.Serializable;
import java.util.Map;

/**
 * @author rabinovi
 * @since 29/01/2015
 * Gathers information from all monitors in the score
 */
public interface WorkerMonitors {
    /**
     * @return map of information gathered from all monitors on the system
     */
    Map<WorkerMonitorInfoEnum, Serializable> getMonitorInfo();
}
