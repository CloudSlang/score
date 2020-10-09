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
package io.cloudslang.worker.monitor;

import com.google.common.collect.Maps;
import io.cloudslang.worker.monitor.service.MetricKeyValue;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class HeapSize implements WorkerPerfMetric{


    public Map<MetricKeyValue, Serializable> measure() {

        // Get current size of heap in bytes
        long heapSize = Runtime.getRuntime().totalMemory();
        System.out.println("heapsize = "+heapSize);
        // Get maximum size of heap in bytes. The heap cannot grow beyond this size.
        long heapMaxSize = Runtime.getRuntime().maxMemory();
        System.out.println("heapmaxsize = "+heapMaxSize);

        double heap_percent=((double)heapSize/(double)heapMaxSize)*100;
        System.out.println("heappercent = "+heap_percent);

        Map<MetricKeyValue, Serializable> inBufferMap = Maps.newHashMap();
        inBufferMap.put(MetricKeyValue.HEAP_SIZE,heap_percent);

        return inBufferMap;
    }

   /* public static void main(String[] args) {
        HeapSize h=new HeapSize();

        Map<String, Double> hm= new HashMap<String, Double>();
        hm=h.measure();
    }*/
}
