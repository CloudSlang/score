/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/

package org.openscore.worker.management.services;

/**
 * @author rabinovi
 * @since 29/01/2015
 * This service is responsible for providing monitor information
 */
public interface WorkerExecutionMonitorService {
    /**
     * Provides overall monitor info on score and sends on event bus, scheduled for relatively long period - minutes.
     */
    void collectMonitorInformation();

    /**
     * Picks monitor info at some relatively short time - seconds.
     * Collected info will be included in the overall monitor info
     */
    void executeScheduledWorkerMonitors();
}
