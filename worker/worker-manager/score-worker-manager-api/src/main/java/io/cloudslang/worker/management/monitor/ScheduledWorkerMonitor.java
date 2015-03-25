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

/**
 * @author rabinovi
 * @since 29/01/2015
 * Monitor scheduled for a short period of time
 */
public interface ScheduledWorkerMonitor extends WorkerMonitor{
    /**
     * Executes scheduled monitor
     */
    void executeScheduled();
}
