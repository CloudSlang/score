/*
 * Licensed to Hewlett-Packard Development Company, L.P. under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
*/
package com.hp.score.samples;

import org.apache.log4j.Logger;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Date: 10/27/2014
 *
 * @author Bonczidai Levente
 */
public class FlowMetadataLoader {
    private final static Logger logger = Logger.getLogger(FlowMetadataLoader.class);

    public static List<FlowMetadata> loadPredefinedFlowsMetadata(String path) {
        List<FlowMetadata> predefinedFlows = new ArrayList<>();

        try {
            InputStream inputStream = FlowMetadataLoader.class.getResourceAsStream(path);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            String document = "";
            while ((line = reader.readLine()) != null) {
                document += line;
                document += "\n";
            }
            reader.close();

            Constructor constructor = new Constructor(FlowMetadataContainer.class);
            TypeDescription flowDescription = new TypeDescription(FlowMetadataContainer.class);
            flowDescription.putListPropertyType("flows", FlowMetadata.class);
            constructor.addTypeDescription(flowDescription);
            Yaml yaml = new Yaml(constructor);
            Object flowAsObject = yaml.load(document);
            predefinedFlows = ((FlowMetadataContainer) flowAsObject).getFlows();
        } catch (Exception ex) {
            logger.info(ex);
        }

        return predefinedFlows;
    }
}
