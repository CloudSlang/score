/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package org.eclipse.score.web.services;

import org.eclipse.score.api.Score;
import org.eclipse.score.api.TriggeringProperties;
import org.eclipse.score.events.EventBus;
import org.eclipse.score.events.ScoreEventListener;
import org.eclipse.score.samples.FlowMetadata;
import org.eclipse.score.samples.FlowMetadataLoader;
import org.eclipse.score.samples.openstack.actions.InputBinding;
import org.eclipse.score.samples.utility.ReflectionUtility;

import org.eclipse.score.web.NotFoundException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.*;


import static org.eclipse.score.samples.openstack.OpenstackCommons.prepareExecutionContext;

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

    public long triggerWithBindings(String identifier, List<InputBinding> bindings) {
        long executionId = -1;
        try {
            executionId = score.trigger(getTriggeringPropertiesByIdentifier(identifier, bindings));
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
    public List<InputBinding> getInputBindingsByIdentifier(String identifier) throws Exception {
        FlowMetadata flowMetadata = getFlowMetadataByIdentifier(identifier, predefinedFlows);
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

    private FlowMetadata getFlowMetadataByIdentifier(String identifier, List<FlowMetadata> flowMetadataList) throws NotFoundException {
        for (FlowMetadata metadata : flowMetadataList) {
            if (metadata.getIdentifier().equals(identifier)) {
                return metadata;
            }
        }
        throw new NotFoundException("Flow \"" + identifier + "\" not found");
    }

    private TriggeringProperties getTriggeringPropertiesByIdentifier(String identifier, List<InputBinding> bindings) throws Exception {
        FlowMetadata flowMetadata = getFlowMetadataByIdentifier(identifier, predefinedFlows);
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