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
package io.cloudslang.worker.monitor.service;

import io.cloudslang.worker.monitor.DiskUsagePerProcess;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

@ManagedResource(description = "Worker Metrics API")
public class WorkerMetricsMBean {

    DiskUsagePerProcess diskUsagePerProcess;

    public WorkerMetricsMBean(){
        diskUsagePerProcess = new DiskUsagePerProcess();
    }

    @ManagedAttribute(description = "Current Disk Usage")
    public long getDiskUsage()
    {

        return diskUsagePerProcess.getCurrentValue();
    }

    /*@ManagedAttribute(description = "Current Out-Buffer Size")
    public int getOutBufferSize(){
        return outBuffer.getSize();
    }
    @ManagedAttribute(description = "Out-Buffer Capacity")
    public int getOutBufferCapacity(){
        return outBuffer.getCapacity();
    }
    @ManagedAttribute(description = "Worker UUID")
    public String getWorkerUuid(){
        return workerManager.getWorkerUuid();
    }*/

}