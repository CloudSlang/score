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
package com.hp.score.web.services;

import com.hp.score.api.Score;
import com.hp.score.api.TriggeringProperties;
import com.hp.score.events.EventBus;
import com.hp.score.events.ScoreEventListener;
import com.hp.score.samples.FlowMetadata;
import com.hp.score.samples.FlowMetadataLoader;
import com.hp.score.samples.openstack.actions.InputBinding;
import com.hp.score.samples.utility.ReflectionUtility;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.*;

import static com.hp.score.samples.openstack.OpenstackCommons.prepareExecutionContext;

/**
 * Date: 9/29/2014
 *
 * @author Bonczidai Levente
 */
public final class ScoreServices {
    @Autowired
    private Score score;

    @Autowired
    private EventBus eventBus;

    private final static Logger logger = Logger.getLogger(ScoreServices.class);
    private final static String AVAILABLE_FLOWS_PATH = "/available_flows_metadata.yaml";

    private static final List<FlowMetadata> predefinedFlows =
            FlowMetadataLoader.loadPredefinedFlowsMetadata(AVAILABLE_FLOWS_PATH);

    public long triggerWithBindings(String identifierOrName, List<InputBinding> bindings) {
        long executionId = -1;
        try {
            executionId = score.trigger(getTriggeringPropertiesByIdentifierOrName(identifierOrName, bindings));
        } catch (Exception ex) {
            logger.error(ex);
        }
        return executionId;
    }

    public void subscribe(ScoreEventListener eventHandler, Set<String> eventTypes) {
        eventBus.subscribe(eventHandler, eventTypes);
    }

    public List<FlowMetadata> getPredefinedFlowsMetadata() {
        return predefinedFlows;
    }

    @SuppressWarnings("unchecked")
    public List<InputBinding> getInputBindingsByIdentifierOrName(String identifier) throws Exception {
        FlowMetadata flowMetadata = getFlowMetadataByIdentifierOrName(identifier, predefinedFlows);
        List<InputBinding> bindings;
        Object returnValue = ReflectionUtility.invokeMethodByName(flowMetadata.getClassName(), flowMetadata.getInputBindingsMethodName());
        try {
            bindings = (List<InputBinding>) returnValue;
        } catch (Exception ex) {
            logger.error(ex);
            bindings = new ArrayList<>();
        }
        return bindings;
    }

    private FlowMetadata getFlowMetadataByIdentifierOrName(String identifier, List<FlowMetadata> flowMetadataList) throws Exception {
        for (FlowMetadata metadata : flowMetadataList) {
            if (metadata.getIdentifier().equals(identifier) || metadata.getName().equals(identifier)) {
                return metadata;
            }
        }
        throw new Exception("Flow \"" + identifier + "\" not found");
    }

    private TriggeringProperties getTriggeringPropertiesByIdentifierOrName(String identifier, List<InputBinding> bindings) throws Exception {
        FlowMetadata flowMetadata = getFlowMetadataByIdentifierOrName(identifier, predefinedFlows);
        Object returnValue = ReflectionUtility.invokeMethodByName(flowMetadata.getClassName(), flowMetadata.getTriggeringPropertiesMethodName());
        if (returnValue instanceof TriggeringProperties) {
            TriggeringProperties triggeringProperties = (TriggeringProperties) returnValue;
            //merge the flow inputs with the initial context (flow may have default values in context)
            Map<String, Serializable> context = new HashMap<>();
            context.putAll(triggeringProperties.getContext());
            context.putAll(prepareExecutionContext(bindings));
            triggeringProperties.setContext(context);
            return triggeringProperties;
        } else {
            throw new Exception("Exception occurred during TriggeringProperties extraction");
        }
    }
}