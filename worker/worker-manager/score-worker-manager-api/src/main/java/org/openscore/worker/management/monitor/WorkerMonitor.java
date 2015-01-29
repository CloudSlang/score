/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/

package org.openscore.worker.management.monitor;

import org.openscore.worker.management.services.WorkerMonitorInfoEnum;

import java.io.Serializable;
import java.util.Map;

/**
 * @author rabinovi
 * @since 29/01/2015
 * Monitor which will be send with overall monitor info
 */
public interface WorkerMonitor {
    /**
     * @param monitorInfo monitor should fills provided map with gathered information
     */
    void captureMonitorInfo(Map<WorkerMonitorInfoEnum, Serializable> monitorInfo);
}
