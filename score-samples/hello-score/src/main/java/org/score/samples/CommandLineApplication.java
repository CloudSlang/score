package org.score.samples;

import com.hp.score.api.ExecutionPlan;
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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.score.samples.utility.ReadInputUtility.readInput;
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
	public static final String DISPLAY_EXECUTION_PLAN = "org.score.samples.DisplayMessageExecutionPlan";

	private List<ExecutionPlanMetadata> predefinedExecutionPlans;

	private ExecutionPlanMetadata externalExecutionPlan;

	@SuppressWarnings("unused")
	@Autowired
	private Score score;

	@SuppressWarnings("unused")
	@Autowired
	private EventBus eventBus;

	public CommandLineApplication() {
		predefinedExecutionPlans = new ArrayList<>();
		registerPredefinedExecutionPlans();
	}

	private void registerPredefinedExecutionPlans() {
		registerExecutionPlan("Create server in Openstack",
				OPENSTACK_APPLICATIONS,
				OPENSTACK_APPLICATIONS,
				"prepareCreateServerExecutionPlan",
				"prepareCreateServerExecutionContext");
		registerExecutionPlan("List servers in Openstack",
				OPENSTACK_APPLICATIONS,
				OPENSTACK_APPLICATIONS,
				"prepareListServerExecutionPlan",
				"prepareListServerExecutionContext");
		registerExecutionPlan("Display message",
				DISPLAY_EXECUTION_PLAN,
				DISPLAY_EXECUTION_PLAN,
				"createDisplayExecutionPlan",
				"createDisplayExecutionContext");
	}

	public void registerExecutionPlan(String name, String executionPlanClassPath, String executionContextClassPath, String executionPlanMethodName, String executionContextMethodName) {
		ExecutionPlanMetadata executionPlanMetadata = new ExecutionPlanMetadata(name, executionPlanClassPath, executionContextClassPath, executionPlanMethodName, executionContextMethodName);
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
			System.out.println("1 - Run Execution Plan");
			System.out.println("2 - Quit");

			System.out.println("Command: ");
			command = readLine(reader);

			try {
				switch (command) {
					case "1":
						displayRunOptions(reader);
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

	private void displayRunOptions(BufferedReader reader) {
		String command;
		command = "";
		while(!command.equals("3")) {
			System.out.println("Select command:");
			System.out.println("1 - Trigger predefined Execution Plan");
			System.out.println("2 - Trigger external Execution Plan");
			System.out.println("3 - Back");

			System.out.println("Command: ");
			command = readLine(reader);

			try {
				switch (command) {
					case "1":
						int executionPlanNumber = listPredefinedExecutionPlans(reader);
						runPredefinedExecutionPlan(executionPlanNumber);
						displayRerunOptionsPredefinedExecutionPlan(reader, executionPlanNumber);
						break;
					case "2":
						prepareAndRunExecutionPlan(reader);
						displayRerunOptionsCustomExecutionPlan(reader);
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

	private void runPredefinedExecutionPlan(int executionPlanNumber) throws Exception {
		ExecutionPlanMetadata executionPlanMetadata = predefinedExecutionPlans.get(executionPlanNumber);
		runExecutionPlan(executionPlanMetadata.getExecutionPlanClassPath(),
				executionPlanMetadata.getExecutionContextClassPath(),
				executionPlanMetadata.getExecutionPlanMethodName(),
				executionPlanMetadata.getExecutionContextMethodName());
	}

	private int listPredefinedExecutionPlans(BufferedReader reader) {
		System.out.println("Available execution plans:");
		for (ExecutionPlanMetadata executionPlanMetadata : predefinedExecutionPlans) {
			System.out.println(predefinedExecutionPlans.indexOf(executionPlanMetadata) + " - " + executionPlanMetadata.getName());
		}
		return readIntegerInput(reader, "Execution plan number");
	}

	private void displayRerunOptionsPredefinedExecutionPlan(BufferedReader reader, int nrExecutionPlan) {
		displayRerunOptions(reader, true, nrExecutionPlan);
	}

	private void displayRerunOptionsCustomExecutionPlan(BufferedReader reader) {
		displayRerunOptions(reader, false, 0);
	}

	private void displayRerunOptions(BufferedReader reader, boolean predefined, int nrExecutionPlan) {
		String command;
		command = "";
		while(!command.equals("2")) {
			System.out.println("Select command:");
			System.out.println("1 - Rerun Execution Plan");
			System.out.println("2 - Back");

			System.out.println("Command: ");
			command = readLine(reader);

			try {
				if (command.equals("1")) {
					if (predefined) {
						runPredefinedExecutionPlan(nrExecutionPlan);
					} else {
						runCustomExecutionPlan();
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	private void prepareAndRunExecutionPlan(BufferedReader reader) throws Exception {
		readReflectionData(reader);
		runCustomExecutionPlan();
	}

	private void runCustomExecutionPlan() throws Exception {
		runExecutionPlan(externalExecutionPlan.getExecutionPlanClassPath(),
				externalExecutionPlan.getExecutionContextClassPath(),
				externalExecutionPlan.getExecutionPlanMethodName(),
				externalExecutionPlan.getExecutionContextMethodName());
	}

	private void runExecutionPlan( String executionPlanClassPath, String executionContextClassPath, String executionPlanMethodName, String executionContextMethodName) throws Exception {
		ExecutionPlan executionPlan = prepareExecutionPlan(executionPlanClassPath, executionPlanMethodName);
		Map<String, Serializable> executionContext = prepareExecutionContext(executionContextClassPath, executionContextMethodName);
		triggerWithContext(executionPlan, executionContext);
	}

	private void readReflectionData(BufferedReader reader) {
		System.out.println("Specify method responsible for ExecutionPlan creation:");
		String executionPlanClassPath = readInput(reader, "Full path to class");
		String executionPlanMethodName = readInput(reader, "Method name");
		System.out.println("Specify method responsible for ExecutionContext initialization:");
		String executionContextClassPath = readInput(reader, "Full path to class");
		String executionContextMethodName = readInput(reader, "Method name");
		externalExecutionPlan = new ExecutionPlanMetadata("External Plan",
				executionPlanClassPath,
				executionContextClassPath,
				executionPlanMethodName,
				executionContextMethodName);
	}

	private ExecutionPlan prepareExecutionPlan(String className, String methodName) throws Exception {
		Object returnValue = ReflectionUtility.invokeMethodByName(className, methodName);
		if (returnValue instanceof ExecutionPlan) {
			return (ExecutionPlan) returnValue;
		} else {
			throw new Exception("Exception occurred during ExecutionPlan creation");
		}
	}

	@SuppressWarnings("unchecked")
	private Map<String, Serializable> prepareExecutionContext(String className, String methodName) throws Exception {
		Object returnValue = ReflectionUtility.invokeMethodByName(className, methodName);
		Map<String, Serializable> executionContext = (Map<String, Serializable>) returnValue;
		if (executionContext != null) {
			return executionContext;
		} else {
			throw new Exception("Exception occurred during ExecutionContext creation");
		}
	}

	private void triggerWithContext(ExecutionPlan executionPlan, Map<String, Serializable> executionContext) {
		TriggeringProperties triggeringProperties = TriggeringProperties.create(executionPlan);
		triggeringProperties.setContext(executionContext);
		triggeringProperties.setStartStep(0L);
		score.trigger(triggeringProperties);
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
		Exception exception = (Exception) event.getData();
		exception.printStackTrace();
	}

	private void logListenerEvent(ScoreEvent event) {
		logger.info("Event " + event.getEventType() + " occurred: " + event.getData());
	}

	private void logScoreListenerEvent(ScoreEvent event) {
		logger.info("Event " + event.getEventType() + " occurred");
	}

	private static class ExecutionPlanMetadata {
		private String name;
		private String executionPlanClassPath;
		private String executionContextClassPath;
		private String executionPlanMethodName;
		private String executionContextMethodName;

		private ExecutionPlanMetadata(String name, String executionPlanClassPath, String executionContextClassPath, String executionPlanMethodName, String executionContextMethodName) {
			this.name = name;
			this.executionPlanClassPath = executionPlanClassPath;
			this.executionContextClassPath = executionContextClassPath;
			this.executionPlanMethodName = executionPlanMethodName;
			this.executionContextMethodName = executionContextMethodName;
		}

		public String getName() {
			return name;
		}

		public String getExecutionPlanClassPath() {
			return executionPlanClassPath;
		}

		public String getExecutionContextClassPath() {
			return executionContextClassPath;
		}

		public String getExecutionPlanMethodName() {
			return executionPlanMethodName;
		}

		public String getExecutionContextMethodName() {
			return executionContextMethodName;
		}
	}
}
