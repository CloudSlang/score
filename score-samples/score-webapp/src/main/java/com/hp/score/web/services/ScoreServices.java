package com.hp.score.web.services;

import com.hp.score.api.Score;
import com.hp.score.api.TriggeringProperties;
import com.hp.score.events.EventBus;
import com.hp.score.events.ScoreEventListener;
import com.hp.score.samples.FlowMetadata;
import com.hp.score.samples.openstack.actions.InputBinding;
import com.hp.score.samples.utility.ReflectionUtility;
import com.hp.score.web.FlowMetadataContainer;
import com.hp.score.web.SpringBootApplication;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
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

    private static final String AVAILABLE_FLOWS_PATH = "/available_flows_metadata.yaml";
    private final static Logger logger = Logger.getLogger(ScoreServices.class);

    private static final List<FlowMetadata> predefinedFlows = loadPredefinedFlowsMetadata();

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

    private TriggeringProperties getTriggeringPropertiesByIdentifier(String identifier, List<InputBinding> bindings) throws Exception {
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

    private static List<FlowMetadata> loadPredefinedFlowsMetadata() {
        List<FlowMetadata> predefinedFlows = new ArrayList<>();

        try {
            InputStream inputStream = SpringBootApplication.class.getResourceAsStream(AVAILABLE_FLOWS_PATH);
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