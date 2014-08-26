package org.score.samples;

import com.hp.score.api.Score;
import com.hp.score.api.TriggeringProperties;
import com.hp.score.events.EventBus;
import com.hp.score.events.EventConstants;
import com.hp.score.events.ScoreEvent;
import com.hp.score.events.ScoreEventListener;
import org.apache.log4j.Logger;
import org.score.samples.openstack.actions.OOActionRunner;
import org.score.samples.utility.ReflectionUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.score.samples.utility.ReadInputUtility.readIntegerInput;
import static org.score.samples.utility.ReadInputUtility.readLine;

/**
 * Date: 8/12/2014
 *
 * @author Bonczidai Levente
 */
public class CommandLineApplication {
	private final static Logger logger = Logger.getLogger(CommandLineApplication.class);
	public static final String OPENSTACK_APPLICATIONS = "org.score.samples.OpenstackApplications";

	private List<ExecutionPlanMetadata> predefinedExecutionPlans;

	@Autowired
	private Score score;

	@Autowired
	private EventBus eventBus;

	public CommandLineApplication() {
		predefinedExecutionPlans = new ArrayList<>();
		registerPredefinedExecutionPlans();
	}

	private void registerPredefinedExecutionPlans() {
		registerExecutionPlan("Create server in OpenStack", OPENSTACK_APPLICATIONS, "createServersFlow");
		registerExecutionPlan("List servers in OpenStack", OPENSTACK_APPLICATIONS, "listServersFlowStandAlone");
		registerExecutionPlan("Delete server in OpenStack", OPENSTACK_APPLICATIONS, "deleteServerFlowStandAlone");
		registerExecutionPlan("Validate server exists in OpenStack", OPENSTACK_APPLICATIONS, "validateServerExistsStandAlone");
		registerExecutionPlan("OpenStack health check", OPENSTACK_APPLICATIONS, "openStackHealthCheck");
	}

	public void registerExecutionPlan(String name, String className, String methodName) {
		ExecutionPlanMetadata executionPlanMetadata = new ExecutionPlanMetadata(name, className, methodName);
		predefinedExecutionPlans.add(executionPlanMetadata);
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
			runPredefinedFlows(executionPlanNumber);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void runPredefinedFlows(int executionPlanNumber) throws Exception {
		ExecutionPlanMetadata executionPlanMetadata = predefinedExecutionPlans.get(executionPlanNumber);
		runFlow(executionPlanMetadata.getClassName(), executionPlanMetadata.getMethodName());
	}

	private int listPredefinedFlows(BufferedReader reader) {
		System.out.println("Available flows");
		for (ExecutionPlanMetadata executionPlanMetadata : predefinedExecutionPlans) {
			System.out.println(predefinedExecutionPlans.indexOf(executionPlanMetadata) + " - " + executionPlanMetadata.getName());
		}
		return readIntegerInput(reader, "Insert the flow number");
	}

	private void runFlow(String className, String methodName) throws Exception {
		TriggeringProperties triggeringProperties = prepareTriggeringProperties(className, methodName);
		score.trigger(triggeringProperties);
	}

	private TriggeringProperties prepareTriggeringProperties(String className, String methodName) throws Exception {
		Object returnValue = ReflectionUtility.invokeMethodByName(className, methodName);
		if (returnValue instanceof TriggeringProperties) {
			return (TriggeringProperties) returnValue;
		} else {
			throw new Exception("Exception occurred during TriggeringProperties creation");
		}
	}

	private static CommandLineApplication loadApp() {
		ApplicationContext context = new ClassPathXmlApplicationContext("/META-INF/spring/myCommandLineApplicationContext.xml");
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

	private static class ExecutionPlanMetadata {
		private String name;
		private String className;
		private String methodName;

		private ExecutionPlanMetadata(String name, String className, String methodName) {
			this.name = name;
			this.className = className;
			this.methodName = methodName;
		}

		public String getClassName() {
			return className;
		}

		public String getMethodName() {
			return methodName;
		}

		public String getName() {
			return name;
		}
	}
}
