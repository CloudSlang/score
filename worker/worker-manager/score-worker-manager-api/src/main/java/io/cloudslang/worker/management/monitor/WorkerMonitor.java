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

package io.cloudslang.worker.management.monitor;

import io.cloudslang.worker.management.services.WorkerMonitorInfoEnum;

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
