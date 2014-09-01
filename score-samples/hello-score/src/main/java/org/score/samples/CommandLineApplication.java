package org.score.samples;

import com.hp.score.api.Score;
import com.hp.score.api.TriggeringProperties;
import com.hp.score.events.EventBus;
import com.hp.score.events.EventConstants;
import com.hp.score.events.ScoreEvent;
import com.hp.score.events.ScoreEventListener;
import org.apache.log4j.Logger;
import org.score.samples.openstack.actions.InputBinding;
import org.score.samples.openstack.actions.OOActionRunner;
import org.score.samples.utility.ReflectionUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.score.samples.openstack.OpenstackCommons.prepareExecutionContext;
import static org.score.samples.openstack.OpenstackCommons.readInput;
import static org.score.samples.openstack.OpenstackCommons.readPredefinedInput;
import static org.score.samples.utility.ReadInputUtility.readIntegerInput;
import static org.score.samples.utility.ReadInputUtility.readLine;

/**
 * Date: 8/28/2014
 *
 * @author Bonczidai Levente
 */
public class CommandLineApplication {
	private final static Logger logger = Logger.getLogger(CommandLineApplication.class);
	public static final String OPENSTACK_FLOWS_PACKAGE = "org.score.samples.openstack";

	private List<FlowMetadata> predefinedFlows;

	@Autowired
	private Score score;

	@Autowired
	private EventBus eventBus;

	public CommandLineApplication() {
		predefinedFlows = new ArrayList<>();
		registerPredefinedExecutionPlans();
	}

	private void registerPredefinedExecutionPlans() {
		registerFlow("display_message", "Simple display message flow", "org.score.samples.DisplayMessageFlow", "displayMessageFlow", "getInputBindings");
		registerFlow("create_server_open_stack", "Create server in OpenStack", OPENSTACK_FLOWS_PACKAGE + ".CreateServerFlow", "createServerFlow", "getInputBindings");
		registerFlow("list_servers_open_stack", "List servers in OpenStack", OPENSTACK_FLOWS_PACKAGE + ".ListServersFlow", "listServersFlow", "getInputBindings");
		registerFlow("delete_server_open_stack", "Delete server in OpenStack", OPENSTACK_FLOWS_PACKAGE + ".DeleteServerFlow", "deleteServerFlow", "getInputBindings");
		registerFlow("validate_server_open_stack", "Validate server exists in OpenStack", OPENSTACK_FLOWS_PACKAGE + ".ValidateServerExistsFlow", "validateServerExistsFlow", "getInputBindings");
		registerFlow("health_check_open_stack", "OpenStack health check", OPENSTACK_FLOWS_PACKAGE + ".OpenStackHealthCheckFlow", "openStackHealthCheckFlow", "getInputBindings");
	}

	public void registerFlow(String identifier, String description, String className, String triggeringPropertiesMethodName, String inputBindingsMethodName) {
		FlowMetadata flowMetadata = new FlowMetadata(identifier, description, className, triggeringPropertiesMethodName, inputBindingsMethodName);
		predefinedFlows.add(flowMetadata);
	}

	public static void main(String[] args) {
		CommandLineApplication app = loadApp();
		app.registerEventListeners();
		app.start();
	}

	private void start() {
		String command = "";
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

		while(!command.equals("2")) {
			System.out.println("Select command:");
			System.out.println("1 - Trigger flow");
			System.out.println("2 - Quit");

			System.out.println("Command: ");
			command = readLine(reader);

			try {
				switch (command) {
					case "1":
						displayAvailableFlows(reader);
						break;
					case "2":
						System.exit(0);
						break;
					default:
						System.out.println("Unknown command..");
						break;
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	private void displayAvailableFlows(BufferedReader reader) {
		int executionPlanNumber = listPredefinedFlows(reader);
		try {
			runPredefinedFlows(executionPlanNumber, reader);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void runPredefinedFlows(int executionPlanNumber, BufferedReader reader) throws Exception {
		FlowMetadata flowMetadata = predefinedFlows.get(executionPlanNumber);
		runFlow(flowMetadata.getClassName(), flowMetadata.getTriggeringPropertiesMethodName(),
				flowMetadata.getInputBindingsMethodName(), reader);
	}

	private int listPredefinedFlows(BufferedReader reader) {
		System.out.println("Available flows");
		for (FlowMetadata flowMetadata : predefinedFlows) {
			System.out.println(predefinedFlows.indexOf(flowMetadata) + " - " + flowMetadata.getDescription());
		}
		return readIntegerInput(reader, "Insert the flow number");
	}

	private void runFlow(String className, String triggeringPropertiesMethodName, String inputBindingMethodName, BufferedReader reader) throws Exception {
		List<InputBinding> bindings = prepareInputBindings(className, inputBindingMethodName);
		manageBindings(bindings, reader);
		TriggeringProperties triggeringProperties = prepareTriggeringProperties(className, triggeringPropertiesMethodName, bindings);
		score.trigger(triggeringProperties);
	}

	private void manageBindings(List<InputBinding> bindings, BufferedReader reader) {
		for (InputBinding inputBinding : bindings) {
			String input = null;
			boolean validValueEntered = false;
			while (!validValueEntered) {
				if (inputBinding.hasDefaultValue()) {
					input = readPredefinedInput(reader, inputBinding.getDescription(), inputBinding.getValue());
					validValueEntered = true;
				} else {
					input = readInput(reader, inputBinding.getDescription());
					validValueEntered = !input.isEmpty();
				}
			}
			//if input is empty use the default value already set, otherwise use input
			if (!input.isEmpty()) {
				inputBinding.setValue(input);
			}
		}
	}

	private List<InputBinding> prepareInputBindings(String className, String methodName) throws Exception {
		Object returnValue = ReflectionUtility.invokeMethodByName(className, methodName);
		try {
			@SuppressWarnings("unchecked")
			List<InputBinding> bindings = (List<InputBinding>) returnValue;
			return bindings;
		}
		catch (ClassCastException ex) {
			throw new Exception("Exception occurred during input binding extraction");
		}
	}

	private TriggeringProperties prepareTriggeringProperties(String className, String methodName, List<InputBinding> bindings) throws Exception {
		Object returnValue = ReflectionUtility.invokeMethodByName(className, methodName);
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

	private static CommandLineApplication loadApp() {
		ApplicationContext context = new ClassPathXmlApplicationContext("/META-INF/spring/commandLineApplicationContext.xml");
		@SuppressWarnings("all")
		CommandLineApplication app = context.getBean(CommandLineApplication.class);
		return app;
	}

	private void registerEventListeners() {
		//register listener for action runtime events
		Set<String> handlerTypes = new HashSet<>();
		handlerTypes.add(OOActionRunner.ACTION_RUNTIME_EVENT_TYPE);
		registerInfoEventListener(handlerTypes);

		//register listener for action exception events
		handlerTypes = new HashSet<>();
		handlerTypes.add(OOActionRunner.ACTION_EXCEPTION_EVENT_TYPE);
		registerExceptionEventListener(handlerTypes);

		// for closing the Application Context when score finishes execution
		registerScoreEventListener();
	}

	private void registerExceptionEventListener(Set<String> handlerTypes) {
		eventBus.subscribe(new ScoreEventListener() {
			@Override
			public void onEvent(ScoreEvent event) {
				logExceptionListenerEvent(event);
			}
		}, handlerTypes);
	}

	private void registerInfoEventListener(Set<String> handlerTypes) {
		eventBus.subscribe(new ScoreEventListener() {
			@Override
			public void onEvent(ScoreEvent event) {
				logListenerEvent(event);
			}
		}, handlerTypes);
	}

	private void registerScoreEventListener() {
		Set<String> handlerTypes = new HashSet<>();
		handlerTypes.add(EventConstants.SCORE_FINISHED_EVENT);
		handlerTypes.add(EventConstants.SCORE_ERROR_EVENT);
		handlerTypes.add(EventConstants.SCORE_FAILURE_EVENT);
		eventBus.subscribe(new ScoreEventListener() {
			@Override
			public void onEvent(ScoreEvent event) {
				if(event.getEventType().equals(EventConstants.SCORE_FINISHED_EVENT)){   //TODO - temp solution, till only end flow events send SCORE_FINISHED_EVENT (now also branch throw this event)
					@SuppressWarnings("all")
					Map<String,Serializable> data = (Map<String,Serializable>)event.getData();
					if ((Boolean)data.get(EventConstants.IS_BRANCH)) {
						return;
					}
				}
				logScoreListenerEvent(event);
			}
		}, handlerTypes);
	}

	private void logExceptionListenerEvent(ScoreEvent event) {
		logger.info("Event " + event.getEventType() + " occurred: " + event.getData());
	}

	private void logListenerEvent(ScoreEvent event) {
		logger.info("Event " + event.getEventType() + " occurred: " + event.getData());
	}

	private void logScoreListenerEvent(ScoreEvent event) {
		logger.info("Event " + event.getEventType() + " occurred");
	}

	private static class FlowMetadata {
		private String identifier;
		private String description;
		private String className;
		private String triggeringPropertiesMethodName;
		private String inputBindingsMethodName;

		private FlowMetadata(String identifier, String description, String className, String triggeringPropertiesMethodName, String inputBindingsMethodName) {
			this.identifier = identifier;
			this.description = description;
			this.className = className;
			this.triggeringPropertiesMethodName = triggeringPropertiesMethodName;
			this.inputBindingsMethodName = inputBindingsMethodName;
		}

		public String getDescription() {
			return description;
		}

		@SuppressWarnings("unused")
		public String getIdentifier() {
			return identifier;
		}

		public String getClassName() {
			return className;
		}

		public String getTriggeringPropertiesMethodName() {
			return triggeringPropertiesMethodName;
		}

		public String getInputBindingsMethodName() {
			return inputBindingsMethodName;
		}
	}
}
