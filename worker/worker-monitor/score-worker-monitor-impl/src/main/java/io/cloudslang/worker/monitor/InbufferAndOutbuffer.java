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
import io.cloudslang.worker.management.services.InBuffer;
import io.cloudslang.worker.management.services.OutboundBuffer;
import io.cloudslang.worker.monitor.service.MetricKeyValue;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.nio.Buffer;
import java.util.HashMap;
import java.util.Map;

public class InbufferAndOutbuffer implements WorkerPerfMetric{

    @Autowired
    private InBuffer inBuffer;

    @Autowired
    private OutboundBuffer outBuffer;



    @Override
    public Map<MetricKeyValue, Serializable> measure() {

        Map<MetricKeyValue, Serializable> BufferMap = Maps.newHashMap();
        System.out.println("inbuffersize = "+inBuffer.getCapacity());
        System.out.println("outbuffersize = "+outBuffer.getCapacity());

        BufferMap.put(MetricKeyValue.IN_BUFFER, inBuffer.getCapacity());
        BufferMap.put(MetricKeyValue.OUT_BUFFER, outBuffer.getCapacity());

        return BufferMap;
    }
}
