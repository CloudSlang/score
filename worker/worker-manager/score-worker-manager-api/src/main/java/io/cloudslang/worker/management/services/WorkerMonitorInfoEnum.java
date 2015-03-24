/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/

package io.cloudslang.worker.management.services;

public enum WorkerMonitorInfoEnum {
    WROKER_ID,
    MONITOR_START_TIME,
    MONITOR_END_TIME,

    INBUFFER_CAPACITY,
    INBUFFER_SIZE_AVERAGE,

    OUTBUFFER_CAPACITY,
    OUTBUDDER_SIZE_AVERAGE,

    RUNNING_TASKS_AVERAGE,
    EXECUTION_THREADS_AMOUNT,

    FREE_MOMORY,
    MAX_MOMORY,
    TOTAL_MEMORY
}
