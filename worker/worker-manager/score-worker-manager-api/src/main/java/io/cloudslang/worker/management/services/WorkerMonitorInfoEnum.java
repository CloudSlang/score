/*
 * Copyright © 2014-2017 EntIT Software LLC, a Micro Focus company (L.P.)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.cloudslang.worker.management.services;

public enum WorkerMonitorInfoEnum {
    WORKER_ID,
    MONITOR_START_TIME,
    MONITOR_END_TIME,

    INBUFFER_CAPACITY,
    INBUFFER_SIZE_AVERAGE,

    OUTBUFFER_CAPACITY,
    OUTBUFFER_SIZE_AVERAGE,

    RUNNING_TASKS_AVERAGE,
    EXECUTION_THREADS_AMOUNT,

    FREE_MEMORY,
    MAX_MEMORY,
    TOTAL_MEMORY
}
