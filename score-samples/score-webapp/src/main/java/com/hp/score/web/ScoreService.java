package com.hp.score.web;

import com.hp.score.api.Score;
import com.hp.score.api.TriggeringProperties;
import com.hp.score.events.EventBus;
import com.hp.score.events.ScoreEventListener;
import com.hp.score.samples.FlowMetadata;
import com.hp.score.samples.openstack.actions.InputBinding;
import com.hp.score.samples.utility.ReflectionUtility;
import com.hp.score.web.controller.ScoreController;
import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.hp.score.samples.openstack.OpenstackCommons.prepareExecutionContext;

/**
 * Date: 9/29/2014
 *
 * @author Bonczidai Levente
 */
public class ScoreService {
	@Autowired
	private Score score;

	@Autowired
	private EventBus eventBus;

	private static final String AVAILABLE_FLOWS_PATH = "/available_flows_metadata.csv";

	private ScoreController scoreController;
	private List<FlowMetadata> predefinedFlows = loadPredefinedFlowsMetadata();

	public void triggerWithBindings(String identifier, List<InputBinding> bindings) {
		try {
			score.trigger(getTriggeringPropertiesByIdentifier(identifier, bindings));
			scoreController.setFlowRunning(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void subscribe(ScoreEventListener eventHandler, Set<String> eventTypes) {
		eventBus.subscribe(eventHandler, eventTypes);
	}

	public List<FlowMetadata> getPredefinedFlowIdentifiers() {
		return predefinedFlows;
	}

	public List<InputBinding> getInputBindingsByIdentifier(String identifier) throws Exception {
		FlowMetadata flowMetadata = getFlowMetadataByIdentifier(identifier, predefinedFlows);
		Object returnValue = ReflectionUtility.invokeMethodByName(flowMetadata.getClassName(), flowMetadata.getInputBindingsMethodName());
		try {
			@SuppressWarnings("unchecked")
			List<InputBinding> bindings = (List<InputBinding>) returnValue;
			return bindings;
		}
		catch (ClassCastException ex) {
			throw new Exception("Exception occurred during input binding extraction");
		}
	}

	public void setScoreController(ScoreController scoreController) {
		this.scoreController = scoreController;
	}

	public void setFlowRunning(boolean flowRunning) {
		scoreController.setFlowRunning(flowRunning);
	}

	public void addTextOutput(String message) {
		scoreController.addTextOutput(message);
	}

	private FlowMetadata getFlowMetadataByIdentifier(String identifier, List<FlowMetadata> flowMetadataList) {
		for (FlowMetadata metadata : flowMetadataList) {
			if (metadata.getIdentifier().equals(identifier)) {
				return metadata;
			}
		}
		return null;
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

	private static List<FlowMetadata> loadPredefinedFlowsMetadata() {
		List<FlowMetadata> predefinedFlows = new ArrayList<>();
		try {
			InputStream inputStream = SpringBootApplication.class.getResourceAsStream(AVAILABLE_FLOWS_PATH);
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			String line;
			while ((line = reader.readLine()) != null) {
				String[] args = line.split(",");
				int nrArgs = 5;
				Validate.isTrue(args.length == nrArgs, "A flow should be described by " + String.valueOf(nrArgs) + " arguments");
				registerFlow(predefinedFlows, args[0], args[1], args[2], args[3], args[4]);
			}
			reader.close();
		} catch (Exception ex) {
			System.out.println("Exception occurred in flow registration");
		}
		return predefinedFlows;
	}

	private static void registerFlow(List<FlowMetadata> predefinedFlows, String identifier, String description, String className, String triggeringPropertiesMethodName, String inputBindingsMethodName) {
		FlowMetadata flowMetadata = new FlowMetadata(identifier, description, className, triggeringPropertiesMethodName, inputBindingsMethodName);
		predefinedFlows.add(flowMetadata);
	}
}
